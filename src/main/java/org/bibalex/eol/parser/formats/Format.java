package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.Taxon;
import org.apache.log4j.Logger;



import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public abstract class Format {

//    protected static final String ancestorTaxonId = "placeholder";
    private final static String acceptedNodesStatuses[] = {"accepted", "accepted name", "preferred", "preferred name", "valid", "valid name", "provisionally accepted",
        "provisionally accepted name"};

    private static final Logger logger = Logger.getLogger(Format.class);
//    private static ArrayList<String> acceptedNodesStatuses = Arrays.asList(accepted);

    public abstract void handleLines(ArrayList<Taxon> nodes, boolean normalResource);

    public static boolean isSynonym(String taxonomicStatus){
        for(String acceptedNodeStatus : acceptedNodesStatuses){
            if(acceptedNodeStatus.equalsIgnoreCase(taxonomicStatus))
                return false;
        }
        if(taxonomicStatus != null)
            return true;
        return false;
    }

    protected int handleNonSynonymNode(String scientificName, String rank, String nodeId, int resourceId,
                                       int parentGeneratedNodeId, Neo4jHandler neo4jHandler, int pageId){
        int acceptedNodeGeneratedId = deleteFromOrphanSynonymsIfExist(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId, neo4jHandler, pageId);
        if (acceptedNodeGeneratedId > 0){
            logger.debug("Deleted from the orphan synonyms successfully");
        }else{
            logger.debug("Error when deleting from the orphan node");
        }
        return acceptedNodeGeneratedId;
    }

    private int createAcceptedNodeIfNotExist(String nodeId, String scientificName, String rank, int parentGeneratedNodeId,
                                               int resourceId, Neo4jHandler neo4jHandler, int pageId){
//        int generatedNodeId = neo4jHandler.getAcceptedNodeIfExist(nodeId, scientificName, resourceId);
//        if(generatedNodeId <= 0){
        int generatedNodeId = neo4jHandler.createAcceptedNode(resourceId, nodeId, scientificName,
                    rank, parentGeneratedNodeId, pageId);
//        }
        return generatedNodeId;
    }

    private int deleteFromOrphanSynonymsIfExist(String scientificName, String rank, String nodeId, int resourceId,
                                                    int parentGeneratedNodeId, Neo4jHandler neo4jHandler, int pageId){
        int generatedNodeId =0;
        SynonymNodeHandler synonymNodeHandler = SynonymNodeHandler.getSynonymNodeHandler(resourceId, neo4jHandler);
        if(synonymNodeHandler.orphanSynonyms.containsKey(nodeId)) {
            generatedNodeId = neo4jHandler.updateAcceptedNode(resourceId, nodeId, scientificName,
                    rank, parentGeneratedNodeId, pageId);
            logger.debug("The accepted node was mentioned before");
            synonymNodeHandler.orphanSynonyms.remove(nodeId);
//            ArrayList<Integer> synonyms = synonymNodeHandler.orphanSynonyms.get(nodeId);
//            for(Integer synonymGeneratedId : synonyms)
//                neo4jHandler.createRelationBetweenNodeAndSynonyms(generatedNodeId, synonymGeneratedId);
        }
        else{
            generatedNodeId = createAcceptedNodeIfNotExist(nodeId, scientificName, rank, parentGeneratedNodeId,
                    resourceId, neo4jHandler, pageId);
        }
        return generatedNodeId;
    }

//    protected boolean deleteTaxon(String nodeId, Neo4jHandler neo4jHandler, int resourceId){
//        System.out.println("deleteTaxon");
//        if (neo4jHandler.hasChildren(nodeId, resourceId)){
//            return true;
//        }
//        else{
//            deleteNodeHasNoChildren(nodeId, neo4jHandler, resourceId);
//        }
//        return false;
//    }

    private void deleteNodeHasNoChildren(String nodeId, Neo4jHandler neo4jHandler, int resourceId){
        System.out.println("delete taxon has no children");
        String parentID = "";
        //then delete from H-base
        if(!neo4jHandler.hasSibling(nodeId, resourceId) &&
                !neo4jHandler.nodeHasTaxonID(parentID, resourceId)){
            deleteNodeHasNoChildren(parentID, neo4jHandler, resourceId);

        }
    }

    public abstract int deleteTaxon(String nodeID, int resourceId, String scientificName);

//    public abstract boolean updateTaxon (String nodeId, int resourceId, String scientificName, String rank, String parentUsageId);
    public abstract void updateTaxon (Taxon taxon);

//    public void updateTaxon(String nodeID, int resourceId, Neo4jHandler neo4jHandler){
//        neo4jHandler.updateTaxon(nodeID, resourceId);
//    }


}
