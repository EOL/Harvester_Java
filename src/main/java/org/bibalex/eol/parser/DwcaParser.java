package org.bibalex.eol.parser;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.models.*;
import org.bibalex.eol.parser.utils.CommonTerms;
import org.bibalex.eol.parser.utils.TermURIs;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.record.Record;
import org.gbif.dwca.record.StarRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DwcaParser {

    Archive dwca;
    HashMap<String, Reference> referencesMap;
    HashMap<String, Agent> agentsMap;
    HashMap<String, MeasurementOrFact> measurementOrFactHashMap;
    //this is used to save the associations with target occurrence
    HashMap<String, Association> twoSidedAccoiationsMap;
    //this is used to save the associations without target occurrence
    HashMap<String, Association> oneSidedAccoiationsMap;
    private static final Logger logger = LoggerFactory.getLogger(DwcaParser.class);

    public DwcaParser(Archive dwca) {
        this.dwca = dwca;
        referencesMap = new HashMap<>();
        agentsMap = new HashMap<>();
        twoSidedAccoiationsMap = new HashMap<>();
        oneSidedAccoiationsMap = new HashMap<>();
        measurementOrFactHashMap = new HashMap<>();
        loadAllReferences();
        loadAllAgents();
        loadAllMeasurementOrFacts();
        loadAllAssociations();
    }

    private void loadAllReferences() {
        logger.debug("Loading all references with term: " + dwca.getExtension(CommonTerms.referenceTerm));
        if (dwca.getExtension(CommonTerms.referenceTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.referenceTerm).iterator(); it.hasNext(); ) {
                Reference reference = parseReference(it.next());
                logger.debug("Adding reference to the map with id: " + reference.getReferenceId());
                referencesMap.put(reference.getReferenceId(), reference);
            }
        }
    }

    private void loadAllAgents() {
        logger.debug("Loading all agents with term: " + dwca.getExtension(CommonTerms.agentTerm));
        if (dwca.getExtension(CommonTerms.agentTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.agentTerm).iterator(); it.hasNext(); ) {
                Agent agent = parseAgent(it.next());
                logger.debug("Adding Agent to the map with id: " + agent.getAgentId());
                agentsMap.put(agent.getAgentId(), agent);
            }
        }
    }

    private void loadAllMeasurementOrFacts() {
        logger.debug("Loading all measurement or facts with term: " + dwca.getExtension(DwcTerm.MeasurementOrFact));
        if (dwca.getExtension(DwcTerm.MeasurementOrFact) != null) {
            for (Iterator<Record> it = dwca.getExtension(DwcTerm.MeasurementOrFact).iterator(); it.hasNext(); ) {
                MeasurementOrFact measurementOrFact = parseMeasurementOrFact(it.next());
                logger.debug("Adding Measurement or fact to the map with id: " + measurementOrFact.getMeasurementId());
                measurementOrFactHashMap.put(measurementOrFact.getMeasurementId(), measurementOrFact);
            }
        }
    }

    private void loadAllAssociations() {
        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
                Association association = parseAssociation(it.next());
                if(association.getTargetOccurrenceId() != null && !association.getTargetOccurrenceId().isEmpty()){
                    twoSidedAccoiationsMap.put(association.getOccurrenceId(), association);
                }else{
                    oneSidedAccoiationsMap.put(association.getAssociationId(), association);
                }
            }
        }
    }

    public void prepareNodesRecord(int resourceId) {
        for (StarRecord rec : dwca) {
            NodeRecord tableRecord = new NodeRecord(rec.core().value(DwcTerm.scientificName),
                    rec.core().value(DwcTerm.taxonID), resourceId);

            Relation relation = parseTaxon(rec);
            if(relation != null)
                tableRecord.setRelation(relation);

            if (rec.hasExtension(GbifTerm.VernacularName)) {
                tableRecord.setVernaculars(parseVernacularNames(rec));
            }
            if (rec.hasExtension(CommonTerms.occurrenceTerm)) {
                tableRecord.setOccurrences(parseOccurrences(rec));
            }
            if (rec.hasExtension(CommonTerms.mediaTerm)) {
                tableRecord.setMedia(parseMedia(rec, tableRecord));
            }

            adjustReferences(tableRecord);
            adjustAgents(tableRecord);

            //call matching algorithm to get pageId

            //Send to HBASE
            callHBase(tableRecord);
            ////////
            printRecord(tableRecord);
            System.out.println();
        }
    }

    private void callHBase(NodeRecord nodeRecord){
        //Adjust the proxy, if any
        String uri = "http://172.16.0.99:80/hbase/api/addHEntry";//ResourceHandler.getPropertyValue("HBaseServer");
        RestTemplate restTemplate;
        if(!uri.equalsIgnoreCase("")) {
//            if(ResourceHandler.getPropertyValue("proxyExists").equalsIgnoreCase("true")) {
//                CredentialsProvider credsProvider = new BasicCredentialsProvider();
//                credsProvider.setCredentials(new AuthScope(proxyUrl, port), new UsernamePasswordCredentials(username, password));
//                HttpClientBuilder clientBuilder = HttpClientBuilder.create();
//                clientBuilder.useSystemProperties();
//                clientBuilder.setProxy(new HttpHost(proxyUrl, port));
//                clientBuilder.setDefaultCredentialsProvider(credsProvider);
//                clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
//                CloseableHttpClient client = clientBuilder.build();
//
//                //set the HTTP client
//                HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//                factory.setHttpClient(client);
//                restTemplate = new RestTemplate(factory);
//            }else
            restTemplate = new RestTemplate();

            //create the json converter
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
            list.add(converter);
            restTemplate.setMessageConverters(list);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            // Pass the object and the needed headers
            HttpEntity<NodeRecord> entity = new HttpEntity<NodeRecord>(nodeRecord, headers);

            System.out.println("=====================");
            System.out.println("=====================");
            System.out.println(entity.getBody());
            System.out.println("=====================");
            System.out.println("=====================");

            // Send the request as POST
            ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST, entity, HbaseResult.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println(response.getBody());
            } else {
                System.out.println("returned code(" + response.getStatusCode() + ")");
            }
        }else{
            System.out.println("Empty uri for hbase");
        }
    }

    private void adjustReferences(NodeRecord nodeRecord) {
        ArrayList<Reference> refs = nodeRecord.getReferences();
        ArrayList<String> refIds = new ArrayList<String>();

        if (refs != null) {
            for (Reference ref : refs)
                refIds.add(ref.getReferenceId());
        }

        if (nodeRecord.getRelation().getReferenceId() != null &&
                !refIds.contains(nodeRecord.getRelation().getReferenceId()))
            addReference(nodeRecord, referencesMap.get(nodeRecord.getRelation().getReferenceId()));

        if (nodeRecord.getMedia() != null) {
            for (Media media : nodeRecord.getMedia()) {
                if (media.getReferenceId() != null && !refIds.contains(media.getReferenceId()) &&
                        referencesMap.get(media.getReferenceId()) != null)
                    addReference(nodeRecord, referencesMap.get(media.getReferenceId()));
            }
        }

        if (nodeRecord.getAssociations() != null) {
            for (Association association : nodeRecord.getAssociations()) {
                if (association.getReferenceId() != null && !refIds.contains(association.getReferenceId()) &&
                        referencesMap.get(association.getReferenceId()) != null)
                    addReference(nodeRecord, referencesMap.get(association.getReferenceId()));
            }
        }

        if (nodeRecord.getMeasurementOrFacts() != null) {
            for (MeasurementOrFact measurementOrFact : nodeRecord.getMeasurementOrFacts()) {
                if (measurementOrFact.getReferenceId() != null && !refIds.contains(measurementOrFact.getReferenceId())
                        && referencesMap.get(measurementOrFact.getReferenceId()) != null)
                    addReference(nodeRecord, referencesMap.get(measurementOrFact.getReferenceId()));
            }
        }
    }

    private void addReference(NodeRecord nodeRecord, Reference ref) {
        ArrayList<Reference> refs = nodeRecord.getReferences();
        if (nodeRecord.getReferences() != null)
            refs.add(ref);
        else {
            refs = new ArrayList<Reference>();
            refs.add(ref);
            nodeRecord.setReferences(refs);
        }
    }

    private void adjustAgents(NodeRecord nodeRecord) {
        if (nodeRecord.getMedia() != null) {
            ArrayList<Agent> agents = nodeRecord.getAgents();
            ArrayList<String> agentsIds = new ArrayList<String>();

            if (agents != null) {
                for (Agent agent : agents)
                    agentsIds.add(agent.getAgentId());
            }

            for (Media media : nodeRecord.getMedia()) {
                if (media.getAgentId() != null && !media.getAgentId().equals("") &&
                        !agentsIds.contains(media.getAgentId()) &&
                        agentsMap.get(media.getAgentId()) != null) {
                    System.out.println("=========================");
                    System.out.println("=========================");
                    System.out.println(media.getAgentId());
                    System.out.println(agentsMap.get(media.getAgentId()));
                    addAgent(nodeRecord, agentsMap.get(media.getAgentId()));
                }
            }
        }
    }

    private void addAgent(NodeRecord nodeRecord, Agent agent) {
        ArrayList<Agent> agents = nodeRecord.getAgents();
        if (nodeRecord.getAgents() != null)
            agents.add(agent);
        else {
            agents = new ArrayList<Agent>();
            agents.add(agent);
            nodeRecord.setAgents(agents);
        }
    }

    private ArrayList<VernacularName> parseVernacularNames(StarRecord record) {
        ArrayList<VernacularName> vernaculars = new ArrayList<VernacularName>();
        for (Record extensionRecord : record.extension(GbifTerm.VernacularName)) {
//            if (extensionRecord.value()) {
            VernacularName vName = new VernacularName(extensionRecord.value(DwcTerm.vernacularName),
                    extensionRecord.value(TermFactory.instance().findTerm(TermURIs.sourceURI)),
                    extensionRecord.value(CommonTerms.languageTerm),
                    extensionRecord.value(DwcTerm.locality), extensionRecord.value(DwcTerm.countryCode),
                    extensionRecord.value(TermFactory.instance().findTerm(TermURIs.isPreferredNameURI)),
                    extensionRecord.value(DwcTerm.taxonRemarks));
            vernaculars.add(vName);
//            }
        }
        return vernaculars;
    }

    private Relation parseTaxon(StarRecord record) {
        if(!TaxonValidationFunctions.failedTaxa.contains(record.core().value(DwcTerm.taxonID))) {
            Relation relation = new Relation(record.core().value(DwcTerm.parentNameUsageID),
                    record.core().value(DwcTerm.kingdom), record.core().value(DwcTerm.phylum),
                    record.core().value(DwcTerm.class_), record.core().value(DwcTerm.order),
                    record.core().value(DwcTerm.family), record.core().value(DwcTerm.genus),
                    record.core().value(CommonTerms.referenceIDTerm));
//            ArrayList<Integer> pageIds = new ArrayList<Integer>();
//            pageIds.add(Integer.parseInt(record.core().value(CommonTerms.eolPageTerm)));
//            relation.setPageIds(pageIds);
            return relation;
        }
        return null;
    }

    private ArrayList<Occurrence> parseOccurrences(StarRecord record) {
        ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
        for (Record extensionRecord : record.extension(CommonTerms.occurrenceTerm)) {
            Occurrence occ = new Occurrence(extensionRecord.value(DwcTerm.occurrenceID),
                    extensionRecord.value(DwcTerm.eventID), extensionRecord.value(DwcTerm.institutionCode),
                    extensionRecord.value(DwcTerm.collectionCode), extensionRecord.value(DwcTerm.catalogNumber),
                    extensionRecord.value(DwcTerm.sex), extensionRecord.value(DwcTerm.lifeStage),
                    extensionRecord.value(DwcTerm.reproductiveCondition), extensionRecord.value(DwcTerm.behavior),
                    extensionRecord.value(DwcTerm.establishmentMeans), extensionRecord.value(DwcTerm.occurrenceRemarks),
                    extensionRecord.value(DwcTerm.individualCount), extensionRecord.value(DwcTerm.preparations),
                    extensionRecord.value(DwcTerm.fieldNotes), extensionRecord.value(DwcTerm.samplingProtocol),
                    extensionRecord.value(DwcTerm.samplingEffort), extensionRecord.value(DwcTerm.recordedBy),
                    extensionRecord.value(DwcTerm.identifiedBy), extensionRecord.value(DwcTerm.dateIdentified),
                    extensionRecord.value(DwcTerm.eventDate), extensionRecord.value(CommonTerms.modifiedDateTerm),
                    extensionRecord.value(DwcTerm.locality), extensionRecord.value(DwcTerm.decimalLatitude),
                    extensionRecord.value(DwcTerm.decimalLongitude), extensionRecord.value(DwcTerm.verbatimLatitude),
                    extensionRecord.value(DwcTerm.verbatimLongitude), extensionRecord.value(DwcTerm.verbatimElevation));
            occurrences.add(occ);
        }
        return occurrences;
    }

    private MeasurementOrFact parseMeasurementOrFact(Record record) {
        //Note: ReferenceId here is put with the other format for reference IDs (Check TODO in parseReferences).
        return new MeasurementOrFact(record.value(DwcTerm.measurementID),
                record.value(DwcTerm.occurrenceID),
                record.value(TermFactory.instance().findTerm(TermURIs.measurementOfTaxonURI)),
                record.value(CommonTerms.associationIDTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.parentMeasurementIDURI)),
                record.value(DwcTerm.measurementType), record.value(DwcTerm.measurementValue),
                record.value(DwcTerm.measurementUnit), record.value(DwcTerm.measurementAccuracy),
                record.value(TermFactory.instance().findTerm(TermURIs.statisticalMethodURI)),
                record.value(DwcTerm.measurementDeterminedDate), record.value(DwcTerm.measurementDeterminedBy),
                record.value(DwcTerm.measurementMethod), record.value(DwcTerm.measurementRemarks),
                record.value(CommonTerms.sourceTerm), record.value(CommonTerms.bibliographicCitationTerm),
                record.value(CommonTerms.contributorTerm), record.value(CommonTerms.referenceIDTerm));
    }

    private Association parseAssociation(Record record) {
        //Note: ReferenceId here is put with the other format for reference IDs (Check TODO in parseReferences).
        return new Association(record.value(CommonTerms.associationIDTerm),
                record.value(DwcTerm.occurrenceID),
                record.value(TermFactory.instance().findTerm(TermURIs.associationType)),
                record.value(TermFactory.instance().findTerm(TermURIs.targetOccurrenceID)),
                record.value(DwcTerm.measurementDeterminedDate), record.value(DwcTerm.measurementDeterminedBy),
                record.value(DwcTerm.measurementMethod), record.value(DwcTerm.measurementRemarks),
                record.value(CommonTerms.sourceTerm), record.value(CommonTerms.bibliographicCitationTerm),
                record.value(CommonTerms.contributorTerm), record.value(CommonTerms.referenceIDTerm));
    }

    private Reference parseReference(Record record) {
        //TODO: ReferenceId term can be replaced according to Jen & Katja's request.
        //TODO: localityOfPublisherURI may be removed upon Jen & Katja's request.
        return new Reference(record.value(CommonTerms.identifierTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.publicationTypeURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.fullReferenceURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.primaryTitleURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.titleURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pagesURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pageStartURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.pageEndURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.volumeURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.editionURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.publisherURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.authorsListURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.editorsListURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.createdURI)),
                record.value(CommonTerms.languageTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.uriURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.doiURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.localityOfPublisherURI)));
    }

    private Agent parseAgent(Record record) {
        return new Agent(record.value(CommonTerms.identifierTerm),
                record.value(TermFactory.instance().findTerm(TermURIs.agentNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentFirstNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentFamilyNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentRole)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentEmailURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentHomepageURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentLogoURLURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentProjectURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentOrganizationURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentAccountNameURI)),
                record.value(TermFactory.instance().findTerm(TermURIs.agentOpenIdURI)));
    }

    private ArrayList<Media> parseMedia(StarRecord record, NodeRecord rec) {
        ArrayList<Media> media = new ArrayList<Media>();
        for (Record extensionRecord : record.extension(CommonTerms.mediaTerm)) {
            if(!MediaValidationFunctions.failedMedia.contains
                    (extensionRecord.value(CommonTerms.identifierTerm))) {
                Media med = new Media(extensionRecord.value(CommonTerms.identifierTerm),
                        extensionRecord.value(CommonTerms.typeTerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaSubtypeURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaFormatURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaSubjectURI)),
                        extensionRecord.value(CommonTerms.titleTerm),
                        extensionRecord.value(CommonTerms.descriptionTerm),
                        extensionRecord.value(CommonTerms.accessURITerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.thumbnailUrlURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaFurtherInformationURLURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaDerivedFromURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaCreateDateURI)),
                        extensionRecord.value(CommonTerms.modifiedDateTerm),
                        extensionRecord.value(CommonTerms.languageTerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaRatingURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaAudienceURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.usageTermsURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaRightsURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaOwnerURI)),
                        extensionRecord.value(CommonTerms.bibliographicCitationTerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.publisherURI)),
                        extensionRecord.value(CommonTerms.contributorTerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaCreatorURI)),
                        extensionRecord.value(CommonTerms.agentIDTerm),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLocationCreatedURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaSpatialURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLatURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLonURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaPosURI)),
                        extensionRecord.value(CommonTerms.referenceIDTerm));
                media.add(med);
            }
        }
        return media;
    }

    private void printRecord(NodeRecord nodeRecord) {

        System.out.print("===================================================");
        System.out.print("===================================================");
        System.out.println("-------------scientific name---------------");
        System.out.println(nodeRecord.getScientificName());
        System.out.println(" " + nodeRecord.getTaxonId());
        System.out.println("-------------Media---------------");
        if (nodeRecord.getMedia() != null && nodeRecord.getMedia().size() > 0)
            System.out.println(nodeRecord.getMedia().size() + "\n" + nodeRecord.getMedia().get(0).
                    getMediaId() + " " + nodeRecord.getMedia().get(0).getType());

        System.out.println("-------------Occ---------------");

        if (nodeRecord.getOccurrences() != null && nodeRecord.getOccurrences().size() > 0)
            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getOccurrences().
                    get(0).getSex() + " " + nodeRecord.getOccurrences().get(0).getBehavior());

        System.out.println("----------------Vernaculars---------------");

        if (nodeRecord.getVernaculars() != null && nodeRecord.getVernaculars().size() > 0)
            System.out.println(nodeRecord.getVernaculars().size() + "\n" + nodeRecord.getVernaculars().
                    get(0).getName() + " " + nodeRecord.getVernaculars().get(0).getSource());

        System.out.println("----------------Measu------------------ ");

        if (nodeRecord.getMeasurementOrFacts() != null && nodeRecord.getMeasurementOrFacts().size() > 0)
            System.out.println(nodeRecord.getMeasurementOrFacts().size() + "\n" + nodeRecord.getMeasurementOrFacts().
                    get(0).getMeasurementId() + " " + nodeRecord.getMeasurementOrFacts().get(0).getContributor());

        System.out.println("------------------Assoc-------------------- ");

        if (nodeRecord.getAssociations() != null && nodeRecord.getAssociations().size() > 0)
            System.out.println(nodeRecord.getAssociations().size() + "\n" + nodeRecord.getAssociations().
                    get(0).getAssociationId() + " " + nodeRecord.getAssociations().get(0).getContributor());

        System.out.println("------------------agents------------------");
        if (nodeRecord.getAgents() != null && nodeRecord.getAgents().size() > 0)
            System.out.println(nodeRecord.getAgents().size() + "\n" + nodeRecord.getAgents().get(0).getAgentId());

        System.out.println("---------------------refs----------------");

        if (nodeRecord.getReferences() != null && nodeRecord.getReferences().size() > 0)
            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getReferences().get(0).
                    getDoi() + " " + nodeRecord.getReferences().get(0).getFullReference());
        System.out.print("===================================================");
        System.out.print("===================================================");
    }

    public static void main(String[] args) {
        Archive dwcArchive = null;
        ResourceHandler.initialize("configs.properties");
        String path = "/home/ba/EOL_Recources/4.tar.gz";
        try {
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
        } catch (Exception e) {
            System.out.println("Failure");
        }
        DwcaParser dwcaP = new DwcaParser(dwcArchive);
        dwcaP.prepareNodesRecord(1);
//        dwcaP.callHBase(new NodeRecord("name", "1", 1));


    }
}
