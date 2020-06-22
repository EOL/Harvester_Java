package org.bibalex.eol.parser;

import com.bibalex.taxonmatcher.controllers.RunTaxonMatching;
import org.bibalex.eol.handler.ScriptsHandler;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.RestClientHandler;
import org.bibalex.eol.parser.models.*;
import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.utils.Constants;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwc.extensions.Extension;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;
import org.gbif.dwca.record.StarRecord;
import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DwcaParser {

    private static final Logger logger = Logger.getLogger(DwcaParser.class);
    Archive dwca;
    private int resourceID;
    private boolean newResource;
    HashMap<String, Reference> referencesMap;
    HashMap<String, Agent> agentsMap;
    private List<Trait> traits;
    private List<Metadata> metadata;
    private List<NodeRecord> nodes;
    private List media;
    private List articles;
    private HashMap<String, TraitTaxon> occurrenceHashMap;
    private HashMap<String, ArrayList<String>> occurrenceTraitsMapping;
    private HashMap<String, ArrayList<Metadata>>  occurrenceMetadataMapping;
    private Map<String, Map<String, String>> actionFiles;
    int batchSize = 1000;
    
    public static final ArrayList<String> expectedMediaFormat = new ArrayList<>();
    private HashMap<String, Integer> deletedTaxons = new HashMap<>();
    private EntityManager entityManager;

    public DwcaParser(Archive dwca, boolean newResource, EntityManager entityManager) {
        this.dwca = dwca;
        //TODO
        referencesMap = new HashMap<>();
        agentsMap = new HashMap<>();
        loadAllReferences();
        loadAllAgents();
        nodes = new ArrayList<NodeRecord>();
        traits = new ArrayList<Trait>();
        metadata = new ArrayList<Metadata>();
        media = new ArrayList();
        articles = new ArrayList();
        actionFiles = ActionFiles.loadActionFiles(dwca);
        this.newResource = newResource;
        this.entityManager = entityManager;
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

    public void prepareNodesRecord(int resourceId) {
        this.resourceID = resourceId;
        deletedTaxons.clear();
        Neo4jHandler neo4jHandler = new Neo4jHandler();
        List<ArchiveField> fieldsSorted = dwca.getCore().getFieldsSorted();
        ArrayList<Term> termsSorted = new ArrayList<Term>();
        for (ArchiveField archiveField : fieldsSorted) {
            termsSorted.add(archiveField.getTerm());
        }
        boolean parent_format=checkParentFormat();
        runScripts(resourceId, termsSorted, parent_format);
        System.out.println("TRAITS parsing starts");
        loadAllOccurrences();
        System.out.println("TOTAL " + occurrenceHashMap.size());
        for (String s: occurrenceHashMap.keySet()) {
          System.out.print("OCC "+ s);
        }
        parseRecords(resourceId, neo4jHandler);
        parseTraits();
        System.out.println("Finish traits");
        parseOccurrenceMetadata();
        System.out.println("Finish metadata");
        printTraits(traits, metadata);
        System.out.println("Total number of traits: " + traits.size());
         System.out.println("Total number of metadata: " + metadata.size());
    }

    private boolean checkParentFormat() {
        ArrayList<Term> ancestryTerms = new ArrayList<>();
        ancestryTerms.add(CommonTerms.kingdomTerm);
        ancestryTerms.add(CommonTerms.phylumTerm);
        ancestryTerms.add(CommonTerms.classTerm);
        ancestryTerms.add(CommonTerms.orderTerm);
        ancestryTerms.add(CommonTerms.familyTerm);
        ancestryTerms.add(CommonTerms.genusTerm);

        for (Term term : ancestryTerms) {
            if (dwca.getCore().hasTerm(term)) {
                return false;
            }
        }
        return true;
    }

    public void runScripts(int resourceId, ArrayList<Term> termsSorted, boolean parent_format){
        ScriptsHandler scriptsHandler = new ScriptsHandler();
        final Path fullPath = Paths.get(dwca.getCore().getLocationFile().getPath());
        final Path base = Paths.get("/", "san");
        System.out.println("full " + fullPath);
        System.out.println("base " + base);
        final Path relativePath = base.relativize(fullPath);
        System.out.println("relative " + relativePath);

//        final Path fullPath= Paths.get("/home/ba/neo4j-community-3.3.1/import/taxa.txt");
//        final Path relativePath=Paths.get("taxa.txt");

//        scriptsHandler.runNeo4jInit();

        if(parent_format)
            scriptsHandler.runPreProc(fullPath.toString(), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank) + 1));
        scriptsHandler.runGenerateIds(fullPath.toString(),  String.valueOf(termsSorted.indexOf(DwcTerm.acceptedNameUsageID)+1), String.valueOf(termsSorted.indexOf(DwcTerm.taxonomicStatus)+1), String.valueOf(termsSorted.indexOf(DwcTerm.parentNameUsageID)+1), String.valueOf(termsSorted.indexOf(DwcTerm.taxonID)+1),
                this.dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false",  this.dwca.getCore().getFieldsTerminatedBy());

        if(parent_format) {
            scriptsHandler.runLoadNodesParentFormat(relativePath.toString(), String.valueOf(resourceId), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)),
                    String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank)),
                    String.valueOf(termsSorted.indexOf((Object) CommonTerms.generatedAutoIdTerm)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)), 
                    this.dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false", String.valueOf(termsSorted.indexOf(CommonTerms.eolPageTerm)),
                    String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+1), String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+2));
            scriptsHandler.runLoadRelationsParentFormat(relativePath.toString(), String.valueOf(resourceId), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)),
                    String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)), String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+2));
        } else {
            String ancestors_and_ranks = getAncestorsInResource(termsSorted);
            String [] ancestors_and_ranks_arr = ancestors_and_ranks.split("\t");
            String ancestors = ancestors_and_ranks_arr[0];
            String ranks = ancestors_and_ranks_arr[1];

            System.out.println(ancestors);
            System.out.println(ranks);

            scriptsHandler.runLoadNodesAncestryFormat(relativePath.toString(), String.valueOf(resourceId), ancestors,ranks,
                    String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)),
                    String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank)),
                    String.valueOf(termsSorted.indexOf(CommonTerms.eolPageTerm)), String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+1),
                    String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+2),this.dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false");

//            scriptsHandler.runLoadRelationsAncestryFormat(relativePath.toString(), String.valueOf(resourceId), ancestors,
//                    String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf(CommonTerms.generatedAutoIdTerm)+2));
        }
    }

    public String getAncestorsInResource(ArrayList<Term> termsSorted){
        String ancestors ="[";
        String ranks ="[";
        ArrayList<String> ancestors_list = getAllAncestors();
        for(String term : ancestors_list) {
            if(this.dwca.getCore().hasTerm(term)) {
                ancestors += termsSorted.indexOf(TermFactory.instance().findTerm(term)) + ",";
                String [] uri = term.split("/");
                ranks += "\""+uri[uri.length-1]+"\",";
            }
        }
        ancestors = ancestors.substring(0,ancestors.length()-1)+"]";
        ranks = ranks.substring(0, ranks.length()-1)+"]";
        return ancestors+"\t"+ranks;
    }

    public  ArrayList<String> getAllAncestors(){
        ArrayList<String> ancestors_list = new ArrayList<>();
        ancestors_list.addAll(addPrefixes("domain"));
        ancestors_list.addAll(addPrefixes("kingdom"));
        ancestors_list.addAll(addPrefixes("phylum"));
        ancestors_list.addAll(addPrefixes("class"));
        ancestors_list.addAll(addPrefixes("cohort"));
        ancestors_list.addAll(addPrefixes("division"));
        ancestors_list.addAll(addPrefixes("order"));
        ancestors_list.addAll(addPrefixes("family"));
        ancestors_list.addAll(addPrefixes("genus"));
        ancestors_list.addAll(addPrefixes("species"));
        ancestors_list.addAll(addPrefixes("variety"));
        ancestors_list.addAll(addPrefixes("form"));
        return ancestors_list;
    }

    public ArrayList<String> addPrefixes(String rank){
        String [] prefixes = {"mege","super","epi","_group","","sub","infra","subter"};
        String uri = PropertiesHandler.getProperty("ranksURI");
        ArrayList<String> rank_with_prefixes = new ArrayList<>();
        for(int i=0; i< prefixes.length;i++) {
            if(prefixes[i].startsWith("_"))
                rank_with_prefixes.add(uri+rank+prefixes[i]);
            else
                rank_with_prefixes.add(uri+prefixes[i]+rank);
        }
        return rank_with_prefixes;
    }

    private void loadAllOccurrences(){
        logger.debug("Loading all occurrences with term: " + dwca.getExtension(CommonTerms.occurrenceTerm));
        if (dwca.getExtension(CommonTerms.occurrenceTerm) != null) {
            occurrenceHashMap = new HashMap<>();
            occurrenceTraitsMapping = new HashMap<>();
            occurrenceMetadataMapping = new HashMap<>();
            for (StarRecord starRecord : dwca) {
                List<Record> occurrences = starRecord.extension(CommonTerms.occurrenceTerm);
                for(Record record: occurrences) {
                    String occurrence_id = record.value(DwcTerm.occurrenceID);
                    logger.debug("Adding occurrence to the map with id: " + occurrence_id);
                    occurrenceHashMap.put(occurrence_id,  new TraitTaxon(starRecord.core().
                            value(TermFactory.instance().findTerm(TermURIs.taxonID_URI)),starRecord.core().
                            value(DwcTerm.scientificName)));
                    String lifestage = record.value(DwcTerm.lifeStage);
                    String sex = record.value(DwcTerm.sex);
                    if(sex != null || lifestage != null)
                    {
                        Metadata metadatum = new Metadata();
                        metadatum.setLifestage(lifestage);
                        metadatum.setSex(sex);
                        occurrenceMetadataMapping.computeIfAbsent(occurrence_id, key -> new ArrayList<>()).
                                add(metadatum);
                    }
                }
            }
        }
    }

    public void parseRecords(int resourceId, Neo4jHandler neo4jHandler) {

//        Taxon Matching
//        if (resourceId != Integer.valueOf(PropertiesHandler.getProperty("DWHId"))) {
//            RunTaxonMatching runTaxonMatching = new RunTaxonMatching();
//            runTaxonMatching.RunTaxonMatching(resourceID);
//        }
        //TODO:Start here to send to mongodb
        //addTimeOfHarvestingToMysql(true);
        //Mysql
        Map<String, String> actions = actionFiles.get(getNameOfActionFile(dwca.getCore().getLocation()));
        int i = 0, count = 0;

        for (StarRecord rec : dwca) {
          if(count %10000 ==0 && count!=0){
               // TODO: Menna uncomment this
                //insertNodeRecordsToMysql(records);
              //  nodes.clear();
                count =0;
           }
           count++;
            // TODO: Menna uncomment this
        //    int generatedNodeId = Integer.valueOf(rec.core().value(CommonTerms.generatedAutoIdTerm));
           i++;
           int generatedNodeId =i;
           System.out.println(rec.core().value(DwcTerm.taxonID)+" count"+count);
           NodeRecord node = parseTaxon(rec, generatedNodeId);
            
            // no need for vernaculars to have taxonId they are embbeded
           if (rec.hasExtension(GbifTerm.VernacularName))
              node.setVernaculars(parseVernacularNames(rec));
           nodes.add(node);
           if (rec.hasExtension(CommonTerms.mediaTerm))
              parseMedia(rec, media, articles);

            System.out.println("before adjust refe");
            //adjustReferences(tableRecord);
//            checkActionFiles(rec, actions, tableRecord);
            //records.add(tableRecord);

        }
        sendNodestoMongodB(nodes);
         // TODO: Menna uncomment this
        //insertNodeRecordsToMysql(records);
        //insertPlaceholderNodesToMysql();
        //addTimeOfHarvestingToMysql(false);
    }

    private void sendNodestoMongodB(List<NodeRecord> nodes){
        System.out.print("Sending Node");
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.sendNodestoMongodB(PropertiesHandler.getProperty("addNodes"),nodes);
    }


    private void addTimeOfHarvestingToMysql(boolean start){
        RestClientHandler restClientHandler = new RestClientHandler();
        if (start)
            restClientHandler.addTimeOfResourceMysql(PropertiesHandler.getProperty("addStartTimeOfResourceMysql"));
        else
            restClientHandler.addTimeOfResourceMysql(PropertiesHandler.getProperty("addEndTimeOfResourceMysql"));

    }

    private NodeRecord parseTaxon(StarRecord record, int generatedNodeId) {
        Map<String, String> actions = actionFiles.get(dwca.getCore().getLocation() + "_action");
        String taxonID = record.core().value(DwcTerm.taxonID);
        String action = "";
        if (actions != null && actions.get(taxonID) != null)
            action = actions.get(taxonID);
        else
            action = "I";
        System.out.println("taxon " + action);
        NodeRecord taxonData = new NodeRecord(record.core().value(DwcTerm.taxonID), resourceID, String.valueOf(generatedNodeId),
                record.core().value(DwcTerm.acceptedNameUsageID), record.core().value(DwcTerm.taxonRank),
                record.core().value(DwcTerm.scientificName), record.core().value(DwcTerm.taxonomicStatus),
                record.core().value(TermFactory.instance().findTerm(TermURIs.landmark)), record.core().value(DwcTerm.taxonRemarks),
                action
        );

        if (resourceID != Integer.valueOf(PropertiesHandler.getProperty("DWHId")) && generatedNodeId != -1) {
            //todo: Menna uncomment and check this
//            Neo4jHandler neo4jHandler = new Neo4jHandler();
//            int pageId = neo4jHandler.getPageIdOfNode(generatedNodeId);
//            if (pageId != 0)
//                taxonData.setpageId(pageId);
        }
        //todo: remove nest line and uncomment the above if condition
        taxonData.setPageId(3);
        System.out.println("taxon ------>" + taxonData.getNodeId());
        return taxonData;
    }

    private ArrayList<VernacularName> parseVernacularNames(StarRecord record) {
        ArrayList<VernacularName> vernaculars = new ArrayList<VernacularName>();
        for (Record extensionRecord : record.extension(GbifTerm.VernacularName)) {
//            if (extensionRecord.value()) {
            String action = checkIfVernacularChanged(extensionRecord);
            VernacularName vName = new VernacularName(extensionRecord.value(DwcTerm.vernacularName),
                    extensionRecord.value(TermFactory.instance().findTerm(TermURIs.sourceURI)),
                    extensionRecord.value(CommonTerms.languageTerm),
                    extensionRecord.value(DwcTerm.locality), extensionRecord.value(DwcTerm.countryCode),
                    extensionRecord.value(TermFactory.instance().findTerm(TermURIs.isPreferredNameURI)),
                    extensionRecord.value(DwcTerm.taxonRemarks), action);
            vernaculars.add(vName);
//            }
        }
        return vernaculars;
    }

    private  void parseMedia(StarRecord record, List media, List articles) {
        System.out.println("Begin "+ record.extension(CommonTerms.mediaTerm).size());
        for (Record extensionRecord : record.extension(CommonTerms.mediaTerm)) {
            String storageLayerPath = "", storageLayerThumbnailPath = "";

            if (extensionRecord.value(CommonTerms.accessURITerm) != null) {
                storageLayerPath = getMediaPath(extensionRecord.value(CommonTerms.accessURITerm));
            }
            if (extensionRecord.value(TermFactory.instance().findTerm(TermURIs.thumbnailUrlURI)) != null) {
                storageLayerThumbnailPath = getMediaPath(extensionRecord.value(TermFactory.instance().findTerm(TermURIs.thumbnailUrlURI)));
            }

            String action = checkIfMediaChanged(extensionRecord);
            if(extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaFormatURI)).equals("text/html"))
            {
                Article article = new Article(record.core().value(DwcTerm.taxonID),
                        extensionRecord.value(CommonTerms.identifierTerm),
                        record.core().value(DwcTerm.resourceID),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaSubjectURI)),
                        extensionRecord.value(CommonTerms.titleTerm),
                        extensionRecord.value(CommonTerms.descriptionTerm),
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
                        extensionRecord.value(CommonTerms.referenceIDTerm), action,
                        storageLayerPath, storageLayerThumbnailPath,
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.cvTermURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaFormatURI)) );
                article.setAgents(adjustAgents(extensionRecord.value(CommonTerms.agentIDTerm)));
                articles.add(article);
            }

            else
            {
                Media medium = new Media(record.core().value(DwcTerm.taxonID), extensionRecord.value(CommonTerms.identifierTerm),
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
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLocationCreatedURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaSpatialURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLatURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaLonURI)),
                        extensionRecord.value(TermFactory.instance().findTerm(TermURIs.mediaPosURI)),
                        extensionRecord.value(CommonTerms.referenceIDTerm),
                        storageLayerPath, storageLayerThumbnailPath, action);
                medium.setAgents(adjustAgents(extensionRecord.value(CommonTerms.agentIDTerm)));
                media.add(medium);
            }

        }

    }
    private void insertNodeRecordsToMysql(ArrayList<NodeRecord> records) {
//   // TODO: Menna uncomment this
// int media_count=0;
//        int vernaculars_count=0;
//        for(NodeRecord record : records){
//            if(record.getMedia()!=null)
//                media_count += record.getMedia().size();
//            if(record.getVernaculars() !=null)
//                vernaculars_count += record.getVernaculars().size();
//        }
//        System.out.println("media count: "+media_count);
//        System.out.println("vernaculars count: "+vernaculars_count);
//        RestClientHandler restClientHandler = new RestClientHandler();
//        restClientHandler.insertNodeRecordsToMysql(PropertiesHandler.getProperty("addEntriesMysql"), records);
////        printRecord(tableRecord);
//        System.out.println();
    }

    private void insertPlaceholderNodesToMysql(){
        Neo4jHandler neo4jHandler = new Neo4jHandler();
        ArrayList<Node> placeholderNodes = neo4jHandler.getPlaceholderNodes(this.resourceID);
        if(placeholderNodes.size() !=0) {
            ArrayList<NodeRecord> records = new ArrayList<>();
            for (int i=0; i<placeholderNodes.size(); i++) {
                NodeRecord tableRecord = new NodeRecord(
                        placeholderNodes.get(i).getGeneratedNodeId() + "", this.resourceID);

                Taxon taxon = new Taxon(placeholderNodes.get(i).getNodeId(), placeholderNodes.get(i).getScientificName(), placeholderNodes.get(i).getRank(), String.valueOf(placeholderNodes.get(i).getPageId()));
                //TODOtableRecord.setTaxon(taxon);
                records.add(tableRecord);
            }
            insertNodeRecordsToMysql(records);
        }
    }

//    private ArrayList<Association> parseAssociationOfTaxon(StarRecord rec) {
//        ArrayList<Association> associations = new ArrayList<>();
//        for (Record record : rec.extension(CommonTerms.occurrenceTerm)) {
//            ArrayList<Association> associationOfOcc = occurrencesAssociationsHashMap.get(record.value(DwcTerm.occurrenceID));
//            if (associationOfOcc != null)
//                associations.addAll(associationOfOcc);
//        }
//        return associations;
//    }

    private void parseTraits() {
        HashMap<String,String> traitMapping = new HashMap<String,String>();
        HashMap<String,ArrayList<Metadata>> traitMetaMapping = new HashMap<String,ArrayList<Metadata>>();
        ArchiveFile measurementFile = dwca.getExtension(DwcTerm.MeasurementOrFact);
        ArchiveFile associationFile =  dwca.getExtension(CommonTerms.associationTerm);
        logger.debug("Loading all traits: " + measurementFile + "and " + associationFile);

        if (measurementFile != null)
        {
            parseMeasurementFile(measurementFile, traitMapping, traitMetaMapping ,
                    occurrenceTraitsMapping,occurrenceMetadataMapping);

        }
        if (associationFile != null)
        {
            parseAssociationFile(associationFile, traitMapping, occurrenceTraitsMapping);
        }
    }

    private void parseMeasurementFile(ArchiveFile measurementFile,HashMap<String,String> traitMapping,
                                      HashMap<String,ArrayList<Metadata>> traitMetaMapping,
                                      HashMap<String, ArrayList<String>>occurrenceTraitsMapping,
                                      HashMap<String, ArrayList<Metadata>> occurrenceMetadataMapping)
    {
        for (Iterator<Record> it = measurementFile.iterator(); it.hasNext();)
        {
            Record traitRecord = it.next();
            if(traitRecord.value(TermFactory.instance().findTerm(TermURIs.measurementOfTaxonURI)) != null &&
                    traitRecord.value(TermFactory.instance().findTerm(TermURIs.measurementOfTaxonURI))
                            .equalsIgnoreCase("true"))
            {
                String resourceTraitId = traitRecord.value(DwcTerm.measurementID);
                Trait trait = createTrait(traitRecord);
                if (trait == null ){
                    continue;
                }
                trait.setMeasurementType(traitRecord.value(DwcTerm.measurementType));
                traitMapping.put(resourceTraitId, trait.getTraitId());
                occurrenceTraitsMapping.computeIfAbsent(traitRecord.value(DwcTerm.occurrenceID), k -> new ArrayList<>())
                        .add(trait.getTraitId());


                traits.add(trait);
                if(traitMetaMapping.containsKey(resourceTraitId))
                {
                    String generatedTraitId = traitMapping.get(resourceTraitId);
                    getMetaData(resourceTraitId, generatedTraitId , traitMetaMapping);
                }

            }
            else if (traitRecord.value(TermFactory.instance().findTerm(TermURIs.parentMeasurementIDURI)).isEmpty())
            {
                Metadata metadatum = createMetadata(traitRecord);
                occurrenceMetadataMapping.computeIfAbsent(traitRecord.value(DwcTerm.occurrenceID), k -> new ArrayList<>())
                        .add(metadatum);

            }
            else
            {
                String parentResTraitId = traitRecord.value(TermFactory.instance().findTerm(TermURIs.parentMeasurementIDURI));
                Metadata metadatum =  createMetadata(traitRecord);
                if(traitMapping.containsKey(parentResTraitId))
                {
                    metadatum.setTraitId(traitMapping.get(parentResTraitId));
                    metadata.add(metadatum);
                }
                else
                {
                    traitMetaMapping.computeIfAbsent(parentResTraitId,k -> new ArrayList<>()).add(metadatum);
                }
            }
        }

    }

    private void parseAssociationFile(ArchiveFile associationFile,HashMap<String,String> traitMapping,
                                      HashMap<String, ArrayList<String>>occurrenceTraitsMapping) {
        for (Iterator<Record> it = associationFile.iterator(); it.hasNext(); ) {
            Record traitRecord = it.next();
            String resourceTraitId = "A" + traitRecord.value(CommonTerms.associationIDTerm);
            Trait trait = createTrait(traitRecord);
            trait.setMeasurementType(traitRecord.value(TermFactory.instance().findTerm(TermURIs.associationType)));
            TraitTaxon targetTaxon = occurrenceHashMap.get(traitRecord.value(TermFactory.instance().
                    findTerm(TermURIs.targetOccurrenceID)));
            trait.setTargetTaxonId(targetTaxon.getTaxonId());
            trait.setTargetScientificName(targetTaxon.getScientificName());
            traitMapping.put(resourceTraitId, trait.getTraitId());
            occurrenceTraitsMapping.computeIfAbsent(traitRecord.value(DwcTerm.occurrenceID), k -> new ArrayList<>())
                    .add(trait.getTraitId());
            traits.add(trait);

        }
    }
    private Trait createTrait(Record record) {
        //THis trait count is just for testing but will be added in neo4j just like generated auto Id
        Trait.traitCount = Trait.traitCount + 1 ;
        String traitId = Integer.toString(Trait.traitCount);
        TraitTaxon sourceTaxon = occurrenceHashMap.get(record.value(DwcTerm.occurrenceID));
        if(sourceTaxon != null){
            String taxonId = sourceTaxon.getTaxonId();
            String scientificName = sourceTaxon.getScientificName();
            String bibliographicCitation = record.value(CommonTerms.bibliographicCitationTerm);
            String measurementUnit = record.value(DwcTerm.measurementUnit);
            String measurementValue = record.value(DwcTerm.measurementValue);
            String measurement = measurementUnit != null ? record.value(DwcTerm.measurementValue): " ";
            // Todo: maybe now no need for literal as publishing will make the classification of the value
            String literal = " ";
            String normalizedMeasurementValue = "";
            String normalizedMeasurementUnit = "";
            String statisticalMethod = record.value(TermFactory.instance().findTerm(TermURIs.statisticalMethodURI));
            String source = record.value(CommonTerms.sourceTerm);
            String referenceId = record.value(CommonTerms.referenceIDTerm);
            String lifestage = record.value(DwcTerm.lifeStage);
            String sex = record.value(DwcTerm.sex);

            return new Trait(traitId, resourceID, taxonId,bibliographicCitation,measurementUnit,
                    normalizedMeasurementValue, normalizedMeasurementUnit,statisticalMethod,
                    source, referenceId, scientificName, measurementValue, measurement, literal , lifestage,
                    sex);
        }
        return null;

    }


    private void getMetaData(String parentResTraitId, String generatedTraitId,
                             HashMap<String,ArrayList<Metadata>> traitMetaMapping )
    {
        ArrayList<Metadata> metadataList = traitMetaMapping.get(parentResTraitId);
        for (Metadata metadatum:metadataList) {
            metadatum.setTraitId(generatedTraitId);
            metadata.add(metadatum);
        }
    }

    private Metadata createMetadata(Record record)
    {
        String measurementType = record.value(DwcTerm.measurementType);
        String measurementUnit = record.value(DwcTerm.measurementUnit);
        String measurementValue = record.value(DwcTerm.measurementValue);
        String measurement = measurementUnit != null ? record.value(DwcTerm.measurementValue): " ";
        // Todo: maybe now no need for literal as publishing will make the classification of the value
        String literal= " ";
        String statisticalMethod = record.value(TermFactory.instance().findTerm(TermURIs.statisticalMethodURI));
        String source = record.value(CommonTerms.sourceTerm);
        String lifestage = record.value(DwcTerm.lifeStage);
        String sex = record.value(DwcTerm.sex);

        return new Metadata(resourceID, measurementType, measurementUnit,
                statisticalMethod, source, measurementValue, measurement, literal, lifestage,
                sex);
    }

    private void parseOccurrenceMetadata()
    {
        for (HashMap.Entry<String, ArrayList<Metadata>> entry : occurrenceMetadataMapping.entrySet())
        {
            String occurrenceId = entry.getKey();
            ArrayList<Metadata> genMetadata = entry.getValue();
            ArrayList<String> traitsId = occurrenceTraitsMapping.get(occurrenceId);
            for (Metadata genMetadatum: genMetadata)
            {
                for(String traitId: traitsId)
                {
                    genMetadatum.setTraitId(traitId);
                    metadata.add(genMetadatum);
                }

            }
        }

    }

//    private void loadAllAssociations() {
//        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
//        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
//            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
//                Association association = parseAssociation(it.next());
//                if (association.getTargetOccurrenceId() != null && !association.getTargetOccurrenceId().isEmpty()) {
//                    twoSidedAccoiationsMap.put(association.getOccurrenceId(), association);
//                } else {
//                    oneSidedAccoiationsMap.put(association.getAssociationId(), association);
//                }
//            }
//        }
//    }

//    private void loadAllAssociationsINOneMap() {
//        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
//        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
//            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
//                Association association = parseAssociation(it.next());
//                logger.debug("Adding association to the map with id: " + association.getAssociationId());
//                associationHashMap.put(association.getAssociationId(), association);
//            }
//        }
//    }

    
//    private void loadAssociationsByOccurrences(){
//        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
//        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
//            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
//                Association association = parseAssociation(it.next());
//                logger.debug("Adding association to the map with id: " + association.getAssociationId());
//                ArrayList<Association> associations = occurrencesAssociationsHashMap.get(association.getOccurrenceId());
//                if(associations ==null)
//                    associations = new ArrayList<>();
//                associations.add(association);
//                occurrencesAssociationsHashMap.put(association.getOccurrenceId(),associations);
//
//            }
//        }





    public void printTraits(List<Trait> traits, List <Metadata> metadata)
    {
        System.out.println("Total number of traits: " + traits);
        for (Trait t:traits) {
            System.out.println(t.toString() );
        }
        System.out.println("Total number of metadata: " + metadata);
        for(Metadata m : metadata)
        {
            System.out.println(m.toString());
        }
    }









    // private ArrayList<MeasurementOrFact> parseMeasurementOrFactOfTaxon(StarRecord rec) {
    //     ArrayList<MeasurementOrFact> measurementOrFacts = new ArrayList<>();
    //     for (Record record : rec.extension(CommonTerms.occurrenceTerm)) {
    //         ArrayList<MeasurementOrFact> measurementOrFactsOfOcc = measurementOrFactHashMap.get(record.value(DwcTerm.occurrenceID));
    //         if (measurementOrFactsOfOcc != null)
    //             measurementOrFacts.addAll(measurementOrFactsOfOcc);
    //     }
    //     return measurementOrFacts;
    // }

//    private Map<String, String> parseTargetOccurrenceIdsOfTaxon(NodeRecord rec){
//        Map<String,String> targetOccurrenceIds = new HashMap<>();
//        // TODO: Menna uncomment this
//        ArrayList<Integer> generated_node_ids=new ArrayList<>();
//        ArrayList<Association> associations = rec.getAssociations();
//
//        for(int i=0; i<associations.size(); i++){
//            Association association =associations.get(i);
//            String targetOccurrence = association.getTargetOccurrenceId();
//            String generated_node_id = String.valueOf(occurrenceHashMap.get(targetOccurrence));
//            generated_node_ids.add(Integer.valueOf(generated_node_id));
//        }
//
//        Neo4jHandler neo4jHandler = new Neo4jHandler();
//        ArrayList<Integer> page_ids = neo4jHandler.getPageIdsOfNodes(generated_node_ids);
//
//        for(int i=0; i<associations.size(); i++){
//            Association association =associations.get(i);
//            String targetOccurrence = association.getTargetOccurrenceId();
//            if(page_ids.get(i)!=-1)
//                targetOccurrenceIds.put(targetOccurrence,String.valueOf(page_ids.get(i)));
//        }
//        return targetOccurrenceIds;
    //}

    private void checkActionFiles(StarRecord rec, Map<String, String> actions, NodeRecord tableRecord) {
        if (actions != null) {
            String taxonID = rec.core().value(DwcTerm.taxonID);
            String action = actions.get(taxonID);

            if (action != null) {
                if (action.equalsIgnoreCase(Constants.INSERT)) {
                    System.out.println("insert that action is insert");
                    insertTaxonToMysql(tableRecord);
                }
//                } else if (action.equalsIgnoreCase(Constants.UPDATE) || action.equalsIgnoreCase(Constants.UNCHANGED)) {
//                    //update
//                } else if (action.equalsIgnoreCase(Constants.DELETE)) {
//                    //delete
//                }
            } else {
                System.out.println("insert from else of action is null");
                insertTaxonToMysql(tableRecord);
            }
        } /*else {
            System.out.println("insert from else of actions is null");
            insertTaxonToMysql(tableRecord);
        }*/
        if (newResource) {
            System.out.println("new resource");
            insertTaxonToMysql(tableRecord);
        }
    }


    private void insertTaxonToMysql(NodeRecord tableRecord) {
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.doConnection(PropertiesHandler.getProperty("addEntryMysql"), tableRecord);
        //printRecord(tableRecord);
        System.out.println();
    }


    private String checkIfOccurrencesChanged(Record extensionRecord) {
        ArchiveFile occurrencesFile = dwca.getExtension(CommonTerms.occurrenceTerm);
        String action = "";
        Map<String, String> actions = actionFiles.get(occurrencesFile.getLocation() + "_action");
        if (actions != null && actions.get(extensionRecord.value(CommonTerms.occurrenceID)) != null)
            action = actions.get(extensionRecord.value(CommonTerms.occurrenceID));
        else
            action = "I";
        System.out.println("occ " + action);
        return action;

    }

    private String checkIfMediaChanged(Record extensionRecord) {
        ArchiveFile mediaFile = dwca.getExtension(CommonTerms.mediaTerm);
        String action = "";
        Map<String, String> actions = actionFiles.get(mediaFile.getLocation() + "_action");
        if (actions != null && actions.get(extensionRecord.value(CommonTerms.identifierTerm)) != null) {
            action = actions.get(extensionRecord.value(CommonTerms.identifierTerm));
        } else {
            action = "I";
        }
        System.out.println("media " + action);
        return action;
    }


    private String checkIfAgentsChanged(String agentId) {
        ArchiveFile agentFile = dwca.getExtension(CommonTerms.agentTerm);
        String action = "";
        if (agentFile != null) {
            Map<String, String> actions = actionFiles.get(agentFile.getLocation() + "_action");
            System.out.println(actions);
            if (actions != null && actions.get(agentId) != null) {
                action = actions.get(agentId);
                System.out.println("agent action found");
            } else {
                action = "I";
            }
            System.out.println("agent " + action);
        }
        return action;
    }


    private String checkIfReferencesChanged(String referenceId) {
        ArchiveFile referenceFile = dwca.getExtension(CommonTerms.referenceTerm);
        String action = "";
        if (referenceFile != null) {
            Map<String, String> actions = actionFiles.get(referenceFile.getLocation() + "_action");
            System.out.println(actions);
            if (actions != null && actions.get(referenceId) != null) {
                action = actions.get(referenceId);
                System.out.println("reference action found");
            } else {
                action = "I";
            }
            System.out.println("refe " + action);
        }
        return action;
    }

    private String checkIfVernacularChanged(Record extensionRecord) {
        ArchiveFile vernacularFile = dwca.getExtension(GbifTerm.VernacularName);
        String action = "";
        if (vernacularFile != null) {
            Map<String, String> actions = actionFiles.get(vernacularFile.getLocation() + "_action");
            if (actions != null) {
                System.out.println(actions);

                String name = extensionRecord.value(DwcTerm.vernacularName);
                String language = extensionRecord.value(CommonTerms.languageTerm);
                String key = name + Constants.SEPARATOR + language;

                System.out.println(key);
                if (actions.get(key) != null) {
                    System.out.println("vernacular action found");
                    action = actions.get(key);
                } else {
                    System.out.println("vernacular action not found");
                    action = "I";
                }
            } else {
                action = "I";
            }
            System.out.println("vernacular " + action);
        }
        return action;
    }

//    private void adjustReferences(List<NodeRecord> taxa,List<Media> media) {
//        // TODO: Menna uncomment this and check
//        //ArrayList<Reference> refs = nodeRecord.getReferences();
//        ArrayList<String> refIds = new ArrayList<String>();
//
////        if (refs != null) {
////            for (Reference ref : refs)
////                refIds.add(ref.getReferenceId());
////        }
//     for (NodeRecord taxon : taxa) {
//         if (taxon.getTaxon().getReferenceId() != null) {
//             String[] references = taxon.getTaxon().getReferenceId().split(";");
//             for (String referenceId : references) {
//                 Reference reference = referencesMap.get(referenceId);
//                 if (reference != null && !refIds.contains(referenceId)) {
//                     String action = checkIfReferencesChanged(referenceId);
//                     reference.setDeltaStatus(action);
//                     //TODO: Menna edit below function
//                     addReference(taxon, reference);
//                 }
//             }
//         }
//     }
////
//        //if (nodeRecord.getMedia() != null) {
//            for (Media medium : media) {
//                if (medium.getReferenceId() != null) {
//                    String[] references = medium.getReferenceId().split(";");
//                    for (String referenceId : references) {
//                        Reference reference = referencesMap.get(referenceId);
//                        if (reference != null && !refIds.contains(referenceId)) {
//                            String action = checkIfReferencesChanged(referenceId);
//                            reference.setDeltaStatus(action);
//                            //TODO: Menna edit below function
//                            addReference(nodeRecord, reference);
//                        }
//                    }
//                }
//            }
//        }
    //TODO: Menna check and uncomment
//
//        if (nodeRecord.getAssociations() != null) {
//            for (Association association : nodeRecord.getAssociations()) {
//                if (association.getReferenceId() != null) {
//                    String[] references = association.getReferenceId().split(";");
//                    for (String reference : references) {
//                        if (referencesMap.get(reference) != null && !refIds.contains(reference))
//                            addReference(nodeRecord, referencesMap.get(reference));
//                    }
//                }
//            }
//        }
//
//        if (nodeRecord.getMeasurementOrFacts() != null) {
//            for (MeasurementOrFact measurementOrFact : nodeRecord.getMeasurementOrFacts()) {
//                if (measurementOrFact.getReferenceId() != null && !refIds.contains(measurementOrFact.getReferenceId())) {
//                    String[] references = measurementOrFact.getReferenceId().split(";");
//                    for (String reference : references) {
//                        if (referencesMap.get(reference) != null && !refIds.contains(reference))
//                            addReference(nodeRecord, referencesMap.get(reference));
//                    }
//                }
//            }
//        }
//    }
//
    private void addReference(NodeRecord nodeRecord, Reference ref) {
//        ArrayList<Reference> refs = nodeRecord.getReferences();
//        if (nodeRecord.getReferences() != null) {
//            if (!refs.contains(ref))
//                refs.add(ref);
//        }
//        else {
//            refs = new ArrayList<Reference>();
//            refs.add(ref);
//            nodeRecord.setReferences(refs);
//        }
    }

    private ArrayList<Agent> adjustAgents(String agents) {
        ArrayList<Agent> tempAgents = new ArrayList<>();
        if (agents != null && agents != "") {
            String[] agentIds = agents.split(";");
            for (String agentId : agentIds) {
                Agent agent = agentsMap.get(agentId);
                if (agent != null) {
                    String action = checkIfAgentsChanged(agentId);
                    agent.setDeltaStatus(action);
                    tempAgents.add(agent);
                }
            }
        }
        return tempAgents;
    }





    // private ArrayList<Occurrence> parseOccurrences(StarRecord record) {
    //     ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
    //     for (Record extensionRecord : record.extension(CommonTerms.occurrenceTerm)) {
    //         String action = checkIfOccurrencesChanged(extensionRecord);
    //         Occurrence occ = new Occurrence(extensionRecord.value(DwcTerm.occurrenceID),
    //                 extensionRecord.value(DwcTerm.eventID), extensionRecord.value(DwcTerm.institutionCode),
    //                 extensionRecord.value(DwcTerm.collectionCode), extensionRecord.value(DwcTerm.catalogNumber),
    //                 extensionRecord.value(DwcTerm.sex), extensionRecord.value(DwcTerm.lifeStage),
    //                 extensionRecord.value(DwcTerm.reproductiveCondition), extensionRecord.value(DwcTerm.behavior),
    //                 extensionRecord.value(DwcTerm.establishmentMeans), extensionRecord.value(DwcTerm.occurrenceRemarks),
    //                 extensionRecord.value(DwcTerm.individualCount), extensionRecord.value(DwcTerm.preparations),
    //                 extensionRecord.value(DwcTerm.fieldNotes), extensionRecord.value(DwcTerm.samplingProtocol),
    //                 extensionRecord.value(DwcTerm.samplingEffort), extensionRecord.value(DwcTerm.recordedBy),
    //                 extensionRecord.value(DwcTerm.identifiedBy), extensionRecord.value(DwcTerm.dateIdentified),
    //                 extensionRecord.value(DwcTerm.eventDate), extensionRecord.value(CommonTerms.modifiedDateTerm),
    //                 extensionRecord.value(DwcTerm.locality), extensionRecord.value(DwcTerm.decimalLatitude),
    //                 extensionRecord.value(DwcTerm.decimalLongitude), extensionRecord.value(DwcTerm.verbatimLatitude),
    //                 extensionRecord.value(DwcTerm.verbatimLongitude), extensionRecord.value(DwcTerm.verbatimElevation),
    //                 action);
    //         occurrences.add(occ);
    //     }
    //     return occurrences;

    // private Association parseAssociation(Record record) {
    //     //Note: ReferenceId here is put with the other format for reference IDs (Check TODO in parseReferences).
    //     return new Association(record.value(CommonTerms.associationIDTerm),
    //             record.value(DwcTerm.occurrenceID),
    //             record.value(TermFactory.instance().findTerm(TermURIs.associationType)),
    //             record.value(TermFactory.instance().findTerm(TermURIs.targetOccurrenceID)),
    //             record.value(DwcTerm.measurementDeterminedDate), record.value(DwcTerm.measurementDeterminedBy),
    //             record.value(DwcTerm.measurementMethod), record.value(DwcTerm.measurementRemarks),
    //             record.value(CommonTerms.sourceTerm), record.value(CommonTerms.bibliographicCitationTerm),
    //             record.value(CommonTerms.contributorTerm), record.value(CommonTerms.referenceIDTerm));
    // }

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



    public String getMediaPath(String URL) {
        String[] files = URL.split("/");
        return PropertiesHandler.getProperty("storage.layer.media.path") + String.valueOf(resourceID) + "/media/" + files[files.length - 1];
    }

//    public String getMediaPath(int resourceID, ArrayList<String> mediaFiles) {
//        try {
//            StorageLayerClient client = new StorageLayerClient();
//            ArrayList<ArrayList<String>> mediaFileType = new ArrayList<ArrayList<String>>();
//            ArrayList<String> mediaTypeArray = new ArrayList<>();
//            for (int i = 0; i < mediaFiles.size(); i++) {
//                mediaTypeArray.add(mediaFiles.get(i));
//                mediaTypeArray.add(expectedMediaFormat.get(i));
//                mediaFileType.add(mediaTypeArray);
//            }
//
//            ArrayList<String> SLPaths = client.downloadMedia(String.valueOf(resourceID), mediaFileType);
//            return convertArrayListToString(SLPaths);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

//    private String convertArrayListToString(ArrayList<String> SLPaths) {
//        String path = "";
//        for (int i = 0; i < SLPaths.size(); i++) {
//            path += SLPaths.get(i);
//            if ((i + 1) != SLPaths.size())
//                path += ",";
//        }
//        return path;
//    }



    private void printRecord(NodeRecord nodeRecord, List<Media> media , List<Article> articles) {

        System.out.print("===================================================");
        System.out.print("===================================================");
        System.out.println("-------------scientific name---------------");
        System.out.println(nodeRecord.getScientificName());
//        System.out.println(" " + nodeRecord.getTaxonId());

        // if (!articles.isEmpty()) {
        //System.out.println("-------------Media---------------");
//            List<Article> a = (List)media.get(1);
//            List<Media> m = (List) media.get(0);
        for (Article art : articles) {
            System.out.println("-------------Articles---------------");
            System.out.println(" Printing ART" + art.getMediaId());
        }
        System.out.println(" Printing ART size " + articles.size());
        //}
        //if (!media.isEmpty()){
        for(Media med: media)
        {
            System.out.println("-------------Other---------------");
            System.out.println(" Printing MED"+ med.getMediaId());
        }
        System.out.println(" Printing MED size"+ media.size());
        //}
        //System.out.println("-------------Occ---------------");

//        if (nodeRecord.getOccurrences() != null && nodeRecord.getOccurrences().size() > 0)
////            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getOccurrences().
//                    get(0).getSex() + " " + nodeRecord.getOccurrences().get(0).getBehavior());

        System.out.println("----------------Vernaculars---------------");

        if (nodeRecord.getVernaculars() != null && nodeRecord.getVernaculars().size() > 0)
        {
            System.out.println(nodeRecord.getVernaculars().size() + "\n" + nodeRecord.getVernaculars().
                    get(0).getName() + " " + nodeRecord.getVernaculars().get(0).getSource());
        }
        // System.out.println("----------------Measu------------------ ");

//        if (nodeRecord.getMeasurementOrFacts() != null && nodeRecord.getMeasurementOrFacts().size() > 0)
//            System.out.println(nodeRecord.getMeasurementOrFacts().size() + "\n" + nodeRecord.getMeasurementOrFacts().
//                    get(0).getMeasurementId() + " " + nodeRecord.getMeasurementOrFacts().get(0).getContributor());
//
//        System.out.println("------------------Assoc-------------------- ");
//
//        if (nodeRecord.getAssociations() != null && nodeRecord.getAssociations().size() > 0)
//            System.out.println(nodeRecord.getAssociations().size() + "\n" + nodeRecord.getAssociations().
//                    get(0).getAssociationId() + " " + nodeRecord.getAssociations().get(0).getContributor());
//
//        System.out.println("------------------agents------------------");
//        if (nodeRecord.getMedia() != null && nodeRecord.getMedia().size() > 0 && nodeRecord.getMedia().get(0) != null &&
//                nodeRecord.getMedia().get(0).getAgents() != null && nodeRecord.getMedia().get(0).getAgents().size() > 0)
//            System.out.println(nodeRecord.getMedia().get(0).getAgents().size() + "\n" +
//                    nodeRecord.getMedia().get(0).getAgents().get(0).getAgentId());
//
//        System.out.println("---------------------refs----------------");
//
//        if (nodeRecord.getReferences() != null && nodeRecord.getReferences().size() > 0)
//            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getReferences().get(0).
//                    getDoi() + " " + nodeRecord.getReferences().get(0).getFullReference());
        System.out.print("===================================================");
        System.out.print("===================================================");
    }

    private String getNameOfActionFile(String title) {
        return title + "_action";
    }

    public static void main(String[] args) throws IOException {

//        DwcaValidator validator = null;
//        MetaHandler metaHandler =new MetaHandler();
//
//        String path="/home/ba/eol_resources/femorale.zip";
//        try {
//            validator = new DwcaValidator("configs.properties");
//            File myArchiveFile = new File(path);
//            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
//            Archive dwcArchive=null;
//            try {
//                dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
//            }catch (Exception e){
//                System.out.println("folder need to editing to be readable by library");
//                metaHandler.adjustMetaFileToBeReadableByLibrary(extractToFolder.getPath());
//                dwcArchive = ArchiveFactory.openArchive(extractToFolder);
//            }
//            System.out.println("call validationnnnnnnnnnnnnn");
//            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
//            String validArchivePath= FilenameUtils.removeExtension(path)+".out_valid";
//            metaHandler.addGeneratedAutoId(validArchivePath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //todo: Menna uncomment next line
//        PropertiesHandler.initializeProperties();
//        Archive dwca = ArchiveFactory.openArchive(new File("/home/ba/test/asscoiations_edit_2.out"));
//        List<ArchiveField> fieldsSorted = dwca.getCore().getFieldsSorted();
//        ArrayList<Term> termsSorted = new ArrayList<Term>();
//        for (ArchiveField archiveField : fieldsSorted) {
//            termsSorted.add(archiveField.getTerm());
//        }

//
//        ScriptsHandlcallParr scriptsHandler = new ScriptsHandler();
//
//        String fullPath = "/home/ba/neo4j-community-3.3.1/import/taxa.txt";
//        String relativePath= "taxa.txt";
//
////        scriptsHandler.runNeo4jInit();
//        scriptsHandler.runPreProc(fullPath.toString(), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank) + 1));
//        scriptsHandler.runGenerateIds(fullPath.toString(),  String.valueOf(termsSorted.indexOf(DwcTerm.acceptedNameUsageID)+1), String.valueOf(termsSorted.indexOf(DwcTerm.taxonomicStatus)+1), String.valueOf(termsSorted.indexOf(DwcTerm.parentNameUsageID)+1),
//                String.valueOf(termsSorted.indexOf(DwcTerm.taxonID)+1), dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false",  dwca.getCore().getFieldsTerminatedBy());
//        scriptsHandler.runLoadNodesParentFormat(relativePath.toString(), String.valueOf(555), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank)),
//                String.valueOf(16), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)), dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false", String.valueOf(termsSorted.indexOf(CommonTerms.eolPageTerm)));
//        scriptsHandler.runLoadRelationsParentFormat(relativePath.toString(), String.valueOf(555), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)));



//        Archive dwcArchive = null;
//        PropertiesHandler.initializeProperties();
////        String path = "/home/ba/EOL_Recources/EOL_dynamic_hierarchyV1Revised.tar.gz";
////        String path = "/home/ba/EOL_Recources/4.tar.gz";
////        String path = "/home/ba/EOL_Recources/DH_min.tar.gz";
////        String path = "/home/ba/EOL_Recources/DH_tiny.tar.gz";
//        String path = "/home/ba/eol_resources/femorale.zip";
//        File extractToFolder=null;
//        MetaHandler metaHandler=new MetaHandler();
//
//        try {
////            DwcaValidator validator = new DwcaValidator("configs.properties");
//            File myArchiveFile = new File(path);
//            extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
//            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
////            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
//        } catch (Exception e) {
//            System.out.println(e);
//            metaHandler.adjustMetaFileToBeReadableByLibrary(extractToFolder.getPath());
//            dwcArchive = ArchiveFactory.openArchive(extractToFolder);
//        }
//        System.out.println("done");
//        DwcaParser dwcaP = new DwcaParser(dwcArchive, false, null);
//////        dwcaP.prepareNodesRecord(5555);
////        ArchiveFile core = dwcArchive.getCore();
////        int count = -1, i = 0, line = 0;
////        for (Record rec : core) {
////            i++;
////            if (rec.value(CommonTerms.eolPageTerm) != null) {
////                int page_id = Integer.valueOf(rec.value(CommonTerms.eolPageTerm));
////                if(page_id == 311544) {
////                    System.out.println(rec.value(DwcTerm.scientificName));
//////                if (page_id > count) {
//////                    count = page_id;
//////                    line = i;
//////                }
////                    break;
////                }
////            }
////        }
////        System.out.println(count + " " + line);
////        dwcaP.prepareNodesRecord(346);
//
////        ArrayList<String> urls = new ArrayList<String>();
//////        urls.add("https://download.quranicaudio.com/quran/abdullaah_3awwaad_al-juhaynee/033.mp3");
//////        urls.add("https://download.quranicaudio.com/quran/abdullaah_3awwaad_al-juhaynee/031.mp3");
////        urls.add("https://www.bibalex.org/en/Attachments/Highlights/Cropped/1600x400/2018012915070314586_eternity.jpg");
////        urls.add("https://www.bibalex.org/en/Attachments/Highlights/Cropped/1600x400/201802041000371225_1600x400.jpg");
////        String paths = dwcaP.getMediaPath(5, urls );
////        System.out.println(paths);
//        Archive dwca = ArchiveFactory.openArchive(new File("/home/ba/test/asscoiations.out"));
//        DwcaParser dwcaParser=new DwcaParser(dwca,false,null);
//        for(StarRecord rec:dwca){
//            System.out.println("here");
//            String sn=rec.core().value(DwcTerm.scientificName);
//            System.out.println(sn);
//        }

//        List<ArchiveField> fieldsSorted = dwca.getCore().getFieldsSorted();
//        ArrayList<Term> termsSorted = new ArrayList<Term>();
//        for (ArchiveField archiveField : fieldsSorted) {
//            termsSorted.add(archiveField.getTerm());
//        }
//        dwcaParser.parseRecords(55555, null);
//
//        dwcaParser.runScripts(4444,termsSorted, true);

//        String ranks=dwcaParser.getAncestorsInResource(termsSorted);
//        System.out.println(ranks);
        //todo: Menna check what is this doing and uncomment
//        Media media=new Media("","","","gf/dg+f","","","","","","","","","","",
//                "","","","","","","","","","","","",""
//                ,"","","","","","");
        System.out.println("hena");

    }
}