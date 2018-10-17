package org.bibalex.eol.parser;

import com.bibalex.taxonmatcher.controllers.RunTaxonMatching;
import com.sun.javafx.collections.MappingChange;
import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.handler.MetaHandler;
import org.bibalex.eol.handler.ScriptsHandler;
import org.bibalex.eol.harvester.StorageLayerClient;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.formats.AncestryFormat;
import org.bibalex.eol.parser.formats.Format;
import org.bibalex.eol.parser.formats.ParentFormat;
import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.RestClientHandler;
import org.bibalex.eol.parser.models.*;
import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.utils.Constants;
import org.bibalex.eol.utils.TermURIs;
import org.bibalex.eol.validator.DwcaValidator;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;
import org.gbif.dwca.record.StarRecord;
import org.apache.log4j.Logger;
import org.gbif.dwca.record.StarRecordImpl;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Condition;

public class DwcaParser {

    Archive dwca;
    HashMap<String, Reference> referencesMap;
    HashMap<String, Agent> agentsMap;
    HashMap<String, ArrayList<MeasurementOrFact>> measurementOrFactHashMap;
    //this is used to save the associations with target occurrence
    HashMap<String, Association> twoSidedAccoiationsMap;
    //this is used to save the associations without target occurrence
    HashMap<String, Association> oneSidedAccoiationsMap;
    HashMap<String, Association> associationHashMap;
    private static final Logger logger = Logger.getLogger(DwcaParser.class);
    private int resourceID;
    private Map<String, Map<String, String>> actionFiles;
    int batchSize = 1000;
    private boolean newResource;
    public static final ArrayList<String> expectedMediaFormat = new ArrayList<>();
    private HashMap<String, Integer> deletedTaxons = new HashMap<>();
    private EntityManager entityManager;

    public DwcaParser(Archive dwca, boolean newResource, EntityManager entityManager) {
        this.dwca = dwca;
        referencesMap = new HashMap<>();
        agentsMap = new HashMap<>();
        twoSidedAccoiationsMap = new HashMap<>();
        oneSidedAccoiationsMap = new HashMap<>();
        measurementOrFactHashMap = new HashMap<>();
        associationHashMap = new HashMap<>();
        loadAllReferences();
        loadAllAgents();
        loadAllMeasurementOrFacts();
        loadAllAssociations();
        loadAllAssociationsINOneMap();
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

    private void loadAllMeasurementOrFacts() {
        logger.debug("Loading all measurement or facts with term: " + dwca.getExtension(DwcTerm.MeasurementOrFact));
        if (dwca.getExtension(DwcTerm.MeasurementOrFact) != null) {
            for (Iterator<Record> it = dwca.getExtension(DwcTerm.MeasurementOrFact).iterator(); it.hasNext(); ) {

                MeasurementOrFact measurementOrFact = parseMeasurementOrFact(it.next());
                logger.debug("Adding Measurement or fact to the map with id: " + measurementOrFact.getMeasurementId());
                ArrayList<MeasurementOrFact> measurementOrFacts = measurementOrFactHashMap.get(measurementOrFact.getOccurrenceId());
                if (!(measurementOrFacts != null))
                    measurementOrFacts = new ArrayList<>();
                measurementOrFacts.add(measurementOrFact);
                measurementOrFactHashMap.put(measurementOrFact.getOccurrenceId(), measurementOrFacts);
            }
        }
    }

    private void loadAllAssociations() {
        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
                Association association = parseAssociation(it.next());
                if (association.getTargetOccurrenceId() != null && !association.getTargetOccurrenceId().isEmpty()) {
                    twoSidedAccoiationsMap.put(association.getOccurrenceId(), association);
                } else {
                    oneSidedAccoiationsMap.put(association.getAssociationId(), association);
                }
            }
        }
    }

    private void loadAllAssociationsINOneMap() {
        logger.debug("Loading all associations with term: " + dwca.getExtension(CommonTerms.associationTerm));
        if (dwca.getExtension(CommonTerms.associationTerm) != null) {
            for (Iterator<Record> it = dwca.getExtension(CommonTerms.associationTerm).iterator(); it.hasNext(); ) {
                Association association = parseAssociation(it.next());
                logger.debug("Adding association to the map with id: " + association.getAssociationId());
                associationHashMap.put(association.getAssociationId(), association);
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


        ScriptsHandler scriptsHandler = new ScriptsHandler();

        final Path fullPath = Paths.get(dwca.getCore().getLocationFile().getPath());
        final Path base = Paths.get("/", "san");
        System.out.println("full " + fullPath);
        System.out.println("base " + base);
        final Path relativePath = base.relativize(fullPath);
        System.out.println("relative " + relativePath);

//        scriptsHandler.runNeo4jInit();

        String page_id_col;


        if (dwca.getCore().hasTerm(CommonTerms.eolPageTerm))
            page_id_col = String.valueOf(termsSorted.indexOf(CommonTerms.eolPageTerm));
        else
            page_id_col = "-1";
        scriptsHandler.runPreProc(fullPath.toString(), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName) + 1), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank) + 1));
        scriptsHandler.runGenerateIds(fullPath.toString());
        scriptsHandler.runLoadNodes(relativePath.toString(), String.valueOf(resourceId), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.scientificName)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonRank)),
                String.valueOf(termsSorted.indexOf((Object) CommonTerms.generatedAutoIdTerm)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)), this.dwca.getCore().getIgnoreHeaderLines() == 1 ? "true" : "false", page_id_col);
        scriptsHandler.runLoadRelations(relativePath.toString(), String.valueOf(resourceId), String.valueOf(termsSorted.indexOf((Object) DwcTerm.taxonID)), String.valueOf(termsSorted.indexOf((Object) DwcTerm.parentNameUsageID)));

        parseRecords(resourceId, neo4jHandler);

    }

    public void parseRecords(int resourceId, Neo4jHandler neo4jHandler) {
        //neo4j
//        buildGraph(resourceId, starRecords);

//        Taxon Matching
        if (resourceId != Integer.valueOf(PropertiesHandler.getProperty("DWHId"))) {
            RunTaxonMatching runTaxonMatching = new RunTaxonMatching();
            runTaxonMatching.RunTaxonMatching(resourceID);
        }

        //Mysql
        Map<String, String> actions = actionFiles.get(getNameOfActionFile(dwca.getCore().getLocation()));
        int i = 0, count = 0;
        ArrayList<NodeRecord> records = new ArrayList<>();
        for (StarRecord rec : dwca) {
            if(count %10000 ==0 && count!=0){
                InsertNodeRecordsToMysql(records);
                records.clear();
                count =0;
            }
            count++;
            int generatedNodeId = Integer.valueOf(rec.core().value(CommonTerms.generatedAutoIdTerm));
            i++;
//            int generatedNodeId =i;
            System.out.println(rec.core().value(DwcTerm.taxonID)+" count"+count);
            NodeRecord tableRecord = new NodeRecord(
                    generatedNodeId + "", resourceId);

            Taxon taxon = parseTaxon(rec, generatedNodeId);

            if (taxon != null)
                tableRecord.setTaxon(taxon);

            if (rec.hasExtension(GbifTerm.VernacularName)) {
                tableRecord.setVernaculars(parseVernacularNames(rec));
            }
            if (rec.hasExtension(CommonTerms.occurrenceTerm)) {
                tableRecord.setOccurrences(parseOccurrences(rec));
                tableRecord.setMeasurementOrFacts(parseMeasurementOrFactOfTaxon(rec));
                tableRecord.setAssociations(parseAssociationOfTaxon(tableRecord));
            }
            if (rec.hasExtension(CommonTerms.mediaTerm)) {
                System.out.println("==============>  parse media");
                tableRecord.setMedia(parseMedia(rec, tableRecord));
            }

            System.out.println("before adjust refe");
            adjustReferences(tableRecord);
//            checkActionFiles(rec, actions, tableRecord);
            records.add(tableRecord);
        }
        InsertNodeRecordsToMysql(records);
//        RestClientHandler restClientHandler=new RestClientHandler();
//        restClientHandler.loadFilesToMysql(PropertiesHandler.getProperty("loadFilesToMysql"));

    }

    private void InsertNodeRecordsToMysql(ArrayList<NodeRecord> records) {
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.insertNodeRecordsToMysql(PropertiesHandler.getProperty("addEntriesMysql"), records);
//        printRecord(tableRecord);
        System.out.println();
    }

    private ArrayList<Association> parseAssociationOfTaxon(NodeRecord tableRecord) {
        ArrayList<Association> associations = new ArrayList<>();
        for (MeasurementOrFact measurementOrFact : tableRecord.getMeasurementOrFacts()) {
            if (measurementOrFact.getAssociationId() != null) {
                associations.add(associationHashMap.get(measurementOrFact.getAssociationId()));
            }
        }
        return associations;
    }

    private ArrayList<MeasurementOrFact> parseMeasurementOrFactOfTaxon(StarRecord rec) {
        ArrayList<MeasurementOrFact> measurementOrFacts = new ArrayList<>();
        for (Record record : rec.extension(CommonTerms.occurrenceTerm)) {
            ArrayList<MeasurementOrFact> measurementOrFactsOfOcc = measurementOrFactHashMap.get(record.value(DwcTerm.occurrenceID));
            if (measurementOrFactsOfOcc != null)
                measurementOrFacts.addAll(measurementOrFactsOfOcc);
        }
        return measurementOrFacts;
    }

//    private void buildGraph(int resourceId, ArrayList<StarRecord> starRecords) {
//        Map<String, String> actions = actionFiles.get(dwca.getCore().getLocation() + "_action");
//        System.out.println("BUILD");
//        ArrayList<Taxon> taxaList = new ArrayList<>();
//        int i = 0;
//        boolean parentFormat = checkParentFormat();
//        logger.debug("parent format: " + parentFormat);
//        System.out.println("parent format: " + parentFormat);
//        Format format = parentFormat ? new ParentFormat(resourceId) : new AncestryFormat(resourceId);
//        boolean normalResource = dwca.getCore().hasTerm(DwcTerm.acceptedNameUsageID);
//        for (StarRecord rec : starRecords) {
//            logger.debug("for loop i is: " + i);
//            System.out.println("for loop i is: " + i);
//            if (i >= batchSize) {
//                logger.debug("create batch: " + parentFormat);
//                System.out.println("create batch: " + parentFormat);
//                format.handleLines(taxaList, normalResource);
//                i = 0;
//                taxaList = new ArrayList<>();
//            } else {
//                if (actions != null) {
//                    String taxonID = rec.core().value(DwcTerm.taxonID);
//                    String action = actions.get(taxonID);
//
//                    if (action != null) {
//                        if (action.equalsIgnoreCase(Constants.INSERT)) {
//                            System.out.println("insert that action is insert");
//                            i++;
//                            taxaList.add(parseTaxon(rec, -1));
//                        } else if (action.equalsIgnoreCase(Constants.UPDATE)) {
//                            format.updateTaxon(parseTaxon(rec, -1));
//                        } else if (action.equalsIgnoreCase(Constants.DELETE)) {
//                            int generatedNodeId = format.deleteTaxon(taxonID, resourceId, rec.core().value(DwcTerm.scientificName));
//                            deletedTaxons.put(taxonID, generatedNodeId);
//                        }
//                    } else {
//                        System.out.println("insert from else of action is null");
//                        i++;
//                        taxaList.add(parseTaxon(rec, -1));
//                    }
//                } /*else {
//                    System.out.println("insert from else of actions is null");
//                    i++;
//                    taxaList.add(parseTaxon(rec));
//                }*/
//                if (newResource) {
//                    System.out.println("insert new resource");
//                    i++;
//                    taxaList.add(parseTaxon(rec, -1));
//                }
//
//            }
//        }
//        format.handleLines(taxaList, normalResource);
//    }

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
//                    updateTaxonInHBase(tableRecord);
//                } else if (action.equalsIgnoreCase(Constants.DELETE)) {
//                    deleteTaxonInHBase(tableRecord);
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
        printRecord(tableRecord);
        System.out.println();
    }


    private void updateTaxonInHBase(NodeRecord tableRecord) {
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.doConnection(PropertiesHandler.getProperty("updateEntryHBase"), tableRecord);
        printRecord(tableRecord);
        System.out.println();
    }

    private void deleteTaxonInHBase(NodeRecord tableRecord) {
        tableRecord.setGeneratedNodeId(deletedTaxons.get(tableRecord.getTaxon().getIdentifier()) + "");
        RestClientHandler restClientHandler = new RestClientHandler();
        restClientHandler.deleteTaxon(PropertiesHandler.getProperty("deleteEntryHBase"), tableRecord);
        printRecord(tableRecord);
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

    private void adjustReferences(NodeRecord nodeRecord) {
        ArrayList<Reference> refs = nodeRecord.getReferences();
        ArrayList<String> refIds = new ArrayList<String>();

        if (refs != null) {
            for (Reference ref : refs)
                refIds.add(ref.getReferenceId());
        }

        if (nodeRecord.getTaxon().getReferenceId() != null) {
            String[] references = nodeRecord.getTaxon().getReferenceId().split(";");
            for (String referenceId : references) {
                Reference reference = referencesMap.get(referenceId);
                if (reference != null && !refIds.contains(referenceId)) {
                    String action = checkIfReferencesChanged(referenceId);
                    reference.setDeltaStatus(action);
                    addReference(nodeRecord, reference);
                }
            }
        }

        if (nodeRecord.getMedia() != null) {
            for (Media media : nodeRecord.getMedia()) {
                if (media.getReferenceId() != null) {
                    String[] references = media.getReferenceId().split(";");
                    for (String referenceId : references) {
                        Reference reference = referencesMap.get(referenceId);
                        if (reference != null && !refIds.contains(referenceId)) {
                            String action = checkIfReferencesChanged(referenceId);
                            reference.setDeltaStatus(action);
                            addReference(nodeRecord, reference);
                        }
                    }
                }
            }
        }

        if (nodeRecord.getAssociations() != null) {
            for (Association association : nodeRecord.getAssociations()) {
                if (association.getReferenceId() != null) {
                    String[] references = association.getReferenceId().split(";");
                    for (String reference : references) {
                        if (referencesMap.get(reference) != null && !refIds.contains(reference))
                            addReference(nodeRecord, referencesMap.get(reference));
                    }
                }
            }
        }

        if (nodeRecord.getMeasurementOrFacts() != null) {
            for (MeasurementOrFact measurementOrFact : nodeRecord.getMeasurementOrFacts()) {
                if (measurementOrFact.getReferenceId() != null && !refIds.contains(measurementOrFact.getReferenceId())) {
                    String[] references = measurementOrFact.getReferenceId().split(";");
                    for (String reference : references) {
                        if (referencesMap.get(reference) != null && !refIds.contains(reference))
                            addReference(nodeRecord, referencesMap.get(reference));
                    }
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

    private Taxon parseTaxon(StarRecord record, int generatedNodeId) {
        Map<String, String> actions = actionFiles.get(dwca.getCore().getLocation() + "_action");
        String taxonID = record.core().value(DwcTerm.taxonID);
        String action = "";
        if (actions != null && actions.get(taxonID) != null)
            action = actions.get(taxonID);
        else
            action = "I";
        System.out.println("taxon " + action);
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
                record.core().value(TermFactory.instance().findTerm(TermURIs.eolIdAnnotations)),
                action, record.core().value(TermFactory.instance().findTerm(TermURIs.landmark))
        );

        if (resourceID != Integer.valueOf(PropertiesHandler.getProperty("DWHId")) && generatedNodeId != -1) {
            Neo4jHandler neo4jHandler = new Neo4jHandler();
            int pageId = neo4jHandler.getPageIdOfNode(generatedNodeId);
            if (pageId != 0)
                taxonData.setPageEolId(String.valueOf(pageId));
        }
        System.out.println("taxon ------>" + taxonData.getIdentifier());
        return taxonData;
    }

    private ArrayList<Occurrence> parseOccurrences(StarRecord record) {
        ArrayList<Occurrence> occurrences = new ArrayList<Occurrence>();
        for (Record extensionRecord : record.extension(CommonTerms.occurrenceTerm)) {
            String action = checkIfOccurrencesChanged(extensionRecord);
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
                    extensionRecord.value(DwcTerm.verbatimLongitude), extensionRecord.value(DwcTerm.verbatimElevation),
                    action);
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
            String storageLayerPath = "", storageLayerThumbnailPath = "";

            if (extensionRecord.value(CommonTerms.accessURITerm) != null) {
                storageLayerPath = getMediaPath(extensionRecord.value(CommonTerms.accessURITerm));
            }
            if (extensionRecord.value(TermFactory.instance().findTerm(TermURIs.thumbnailUrlURI)) != null) {
                storageLayerThumbnailPath = getMediaPath(extensionRecord.value(TermFactory.instance().findTerm(TermURIs.thumbnailUrlURI)));
            }

            String action = checkIfMediaChanged(extensionRecord);

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
                    extensionRecord.value(CommonTerms.referenceIDTerm),
                    storageLayerPath, storageLayerThumbnailPath, action);
            med.setAgents(adjustAgents(extensionRecord.value(CommonTerms.agentIDTerm)));
            media.add(med);
        }
        return media;
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

//    private boolean checkParentFormat() {
//        ArrayList<Term> ancestryTerms = new ArrayList<>();
//        ancestryTerms.add(CommonTerms.kingdomTerm);
//        ancestryTerms.add(CommonTerms.phylumTerm);
//        ancestryTerms.add(CommonTerms.classTerm);
//        ancestryTerms.add(CommonTerms.orderTerm);
//        ancestryTerms.add(CommonTerms.familyTerm);
//        ancestryTerms.add(CommonTerms.genusTerm);
//
//        for (Term term : ancestryTerms) {
//            if (dwca.getCore().hasTerm(term)) {
//                return false;
//            }
//        }
//        return true;
//    }

    private void printRecord(NodeRecord nodeRecord) {

        System.out.print("===================================================");
        System.out.print("===================================================");
        System.out.println("-------------scientific name---------------");
        System.out.println(nodeRecord.getTaxon().getScientificName());
//        System.out.println(" " + nodeRecord.getTaxonId());
        System.out.println("-------------Media---------------");
        if (nodeRecord.getMedia() != null && nodeRecord.getMedia().size() > 0)
            System.out.println(nodeRecord.getMedia().size() + "\n" + nodeRecord.getMedia().get(0).
                    getMediaId() + " " + nodeRecord.getMedia().get(0).getType());

        System.out.println("-------------Occ---------------");

        if (nodeRecord.getOccurrences() != null && nodeRecord.getOccurrences().size() > 0)
//            System.out.println(nodeRecord.getReferences().size() + "\n" + nodeRecord.getOccurrences().
//                    get(0).getSex() + " " + nodeRecord.getOccurrences().get(0).getBehavior());

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

    private String getNameOfActionFile(String title) {
        return title + "_action";
    }

    public static void main(String[] args) throws IOException {

        DwcaValidator validator = null;
        MetaHandler metaHandler =new MetaHandler();

        String path="/home/ba/eol_resources/femorale.zip";
        try {
            validator = new DwcaValidator("configs.properties");
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            Archive dwcArchive=null;
            try {
                dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
            }catch (Exception e){
                System.out.println("folder need to editing to be readable by library");
                metaHandler.adjustMetaFileToBeReadableByLibrary(extractToFolder.getPath());
                dwcArchive = ArchiveFactory.openArchive(extractToFolder);
            }
            System.out.println("call validationnnnnnnnnnnnnn");
            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
            String validArchivePath= FilenameUtils.removeExtension(path)+".out_valid";
            metaHandler.addGeneratedAutoId(validArchivePath);
        } catch (Exception e) {
            e.printStackTrace();
        }


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
////        DwcaParser dwcaP = new DwcaParser(dwcArchive, false, null);
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
////        dwcaP.callHBase(new NodeRecord("name", "1", 1));


    }
}