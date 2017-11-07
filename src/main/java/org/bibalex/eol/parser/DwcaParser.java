package org.bibalex.eol.parser;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.handler.PropertiesHandler;
import org.bibalex.eol.parser.formats.AncestryFormat;
import org.bibalex.eol.parser.formats.Format;
import org.bibalex.eol.parser.formats.ParentFormat;
import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.RestClientHandler;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    int batchSize = 1000;

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

        Neo4jHandler neo4jHandler = new Neo4jHandler();
        int generatedNodeId;

//        buildGraph(resourceId);

        for (StarRecord rec : dwca) {

            generatedNodeId = neo4jHandler.getNodeIfExist
                    (rec.core().value(DwcTerm.taxonID), resourceId);
            NodeRecord tableRecord = new NodeRecord(rec.core().value(DwcTerm.taxonID),
                    generatedNodeId + "", resourceId);

            Taxon taxon = parseTaxon(rec);
            if(taxon != null)
                tableRecord.setTaxon(taxon);

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

            //Send to HBASE
            callHBaseToCreate(tableRecord);
            ////////
            printRecord(tableRecord);
            System.out.println();
        }
    }

    private void buildGraph(int resourceId){
        System.out.println("BUILD");
        ArrayList<Taxon> taxaList = new ArrayList<>();
        int i = 0;
        boolean parentFormat = dwca.getCore().hasTerm(DwcTerm.parentNameUsageID);
        logger.debug("parent format: " + parentFormat);
        Format format = parentFormat ? new ParentFormat(resourceId) : new AncestryFormat(resourceId);

        for (StarRecord rec : dwca) {
            logger.debug("for loop i is: " + i);
            if (i >= batchSize){
                logger.debug("create batch: " + parentFormat);
                format.handleLines(taxaList);
                i = 0;
                taxaList = new ArrayList<>();
            }else{
                i++;
                taxaList.add(parseTaxon(rec));
            }
        }
        format.handleLines(taxaList);
    }

    private void callHBaseToCreate(NodeRecord nodeRecord){
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.doConnection(PropertiesHandler.getProperty("addEntryHBase"), nodeRecord);
    }

    private void adjustReferences(NodeRecord nodeRecord) {
        ArrayList<Reference> refs = nodeRecord.getReferences();
        ArrayList<String> refIds = new ArrayList<String>();

        if (refs != null) {
            for (Reference ref : refs)
                refIds.add(ref.getReferenceId());
        }

        if (nodeRecord.getTaxon().getReferenceId() != null &&
                !refIds.contains(nodeRecord.getTaxon().getReferenceId())) {
            String[] references = nodeRecord.getTaxon().getReferenceId().split(";");
            for(String reference : references)
                addReference(nodeRecord, referencesMap.get(reference));
        }

        if (nodeRecord.getMedia() != null) {
            for (Media media : nodeRecord.getMedia()) {
                if (media.getReferenceId() != null && !refIds.contains(media.getReferenceId()) &&
                        referencesMap.get(media.getReferenceId()) != null){
                    String[] references = media.getReferenceId().split(";");
                    for(String reference : references)
                        addReference(nodeRecord, referencesMap.get(reference));
                }
            }
        }

        if (nodeRecord.getAssociations() != null) {
            for (Association association : nodeRecord.getAssociations()) {
                if (association.getReferenceId() != null && !refIds.contains(association.getReferenceId()) &&
                        referencesMap.get(association.getReferenceId()) != null){
                    String[] references = association.getReferenceId().split(";");
                    for(String reference : references)
                        addReference(nodeRecord, referencesMap.get(reference));
                }
            }
        }

        if (nodeRecord.getMeasurementOrFacts() != null) {
            for (MeasurementOrFact measurementOrFact : nodeRecord.getMeasurementOrFacts()) {
                if (measurementOrFact.getReferenceId() != null && !refIds.contains(measurementOrFact.getReferenceId())
                        && referencesMap.get(measurementOrFact.getReferenceId()) != null){
                    String[] references = measurementOrFact.getReferenceId().split(";");
                    for(String reference : references)
                        addReference(nodeRecord, referencesMap.get(reference));
                }
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

    private ArrayList<Agent> adjustAgents(String agents) {
        ArrayList<Agent> tempAgents = new ArrayList<>();
        if(agents != null && agents != "") {
            String[] agentIds = agents.split(";");
            for (String agentId : agentIds) {
                tempAgents.add(agentsMap.get(agentId));
            }
        }
        return tempAgents;
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

    private Taxon parseTaxon(StarRecord record) {
        Taxon taxonData = new Taxon(record.core().value(DwcTerm.taxonID), record.core().value(DwcTerm.scientificName),
                record.core().value(DwcTerm.parentNameUsageID), record.core().value(DwcTerm.kingdom),
                record.core().value(DwcTerm.phylum), record.core().value(DwcTerm.class_),
                record.core().value(DwcTerm.order), record.core().value(DwcTerm.family),
                record.core().value(DwcTerm.genus), record.core().value(DwcTerm.taxonRank),
                record.core().value(CommonTerms.furtherInformationURL), record.core().value(DwcTerm.taxonomicStatus),
                record.core().value(DwcTerm.taxonRemarks), record.core().value(DwcTerm.namePublishedIn),
                record.core().value(CommonTerms.referenceIDTerm), record.core().value(CommonTerms.eolPageTerm),
                record.core().value(DwcTerm.acceptedNameUsageID), record.core().value(CommonTerms.sourceTerm),
                record.core().value(TermFactory.instance().findTerm(TermURIs.canonicalNameURL)),
                record.core().value(TermFactory.instance().findTerm(TermURIs.scientificNameAuthorship)),
                record.core().value(TermFactory.instance().findTerm(TermURIs.scientificNameID)),
                record.core().value(TermFactory.instance().findTerm(TermURIs.datasetID)),
                record.core().value(TermFactory.instance().findTerm(TermURIs.eolIdAnnotations))
                );
        return taxonData;
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
            med.setAgents(adjustAgents(extensionRecord.value(CommonTerms.agentIDTerm)));
            media.add(med);
        }
        return media;
    }

    private void printRecord(NodeRecord nodeRecord) {

        System.out.print("===================================================");
        System.out.print("===================================================");
        System.out.println("-------------scientific name---------------");
        System.out.println(nodeRecord.getTaxon().getScientificName());
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
        if (nodeRecord.getMedia() != null && nodeRecord.getMedia().size() > 0 && nodeRecord.getMedia().get(0) != null &&
            nodeRecord.getMedia().get(0).getAgents() != null && nodeRecord.getMedia().get(0).getAgents().size() > 0)
            System.out.println(nodeRecord.getMedia().get(0).getAgents().size() + "\n" +
                    nodeRecord.getMedia().get(0).getAgents().get(0).getAgentId());

        System.out.println("---------------------refs----------------");

        if (nodeRecord.getReferences() != null && nodeRecord.getReferences().size() > 0)
            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getReferences().get(0).
                    getDoi() + " " + nodeRecord.getReferences().get(0).getFullReference());
        System.out.print("===================================================");
        System.out.print("===================================================");
    }

    public static void main(String[] args) throws IOException {
        Archive dwcArchive = null;
        PropertiesHandler.initializeProperties();
        String path = "/home/ba/EOL_Recources/EOL_dynamic_hierarchyV1Revised.tar.gz";
        try {
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
        } catch (Exception e) {
            System.out.println("Failure");
        }
        DwcaParser dwcaP = new DwcaParser(dwcArchive);
        dwcaP.prepareNodesRecord(2);
//        dwcaP.callHBase(new NodeRecord("name", "1", 1));


    }
}
