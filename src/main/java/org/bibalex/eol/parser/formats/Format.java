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
                                       int parentGeneratedNodeId, Neo4jHandler neo4jHandler){
        int acceptedNodeGeneratedId = createAcceptedNodeIfNotExist(nodeId, scientificName, rank, parentGeneratedNodeId,
                resourceId, neo4jHandler);
        if (deleteFromOrphanSynonymsIfExist(nodeId, acceptedNodeGeneratedId, neo4jHandler, resourceId)){
            logger.debug("Deleted from the orphan synonyms successfully");
        }else{
            logger.debug("Error when deleting from the orphan node");
        }
        return acceptedNodeGeneratedId;
    }

    private int createAcceptedNodeIfNotExist(String nodeId, String scientificName, String rank, int parentGeneratedNodeId,
                                               int resourceId, Neo4jHandler neo4jHandler){
//        int generatedNodeId = neo4jHandler.getAcceptedNodeIfExist(nodeId, scientificName, resourceId);
//        if(generatedNodeId <= 0){
        int generatedNodeId = neo4jHandler.createAcceptedNode(resourceId, nodeId, scientificName,
                    rank, parentGeneratedNodeId);
//        }
        return generatedNodeId;
    }

    private boolean deleteFromOrphanSynonymsIfExist(String nodeId, int generatedNodeId, Neo4jHandler neo4jHandler,
                                                    int resourceId){
        SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, neo4jHandler);
        if(synonymNodeHandler.orphanSynonyms.containsKey(nodeId)) {
            logger.debug("The accepted node was mentioned before");
            ArrayList<Integer> synonyms = synonymNodeHandler.orphanSynonyms.get(nodeId);
            for(Integer synonymGeneratedId : synonyms)
                neo4jHandler.createRelationBetweenNodeAndSynonyms(generatedNodeId, synonymGeneratedId);
        }
        return true;
    }

    protected boolean deleteTaxon(String nodeId, Neo4jHandler neo4jHandler, int resourceId){
        System.out.println("deleteTaxon");
        if (neo4jHandler.hasChildren(nodeId, resourceId)){
            return true;
        }
        else{
            deleteNodeHasNoChildren(nodeId, neo4jHandler, resourceId);
        }
        return false;
    }

    private void deleteNodeHasNoChildren(String nodeId, Neo4jHandler neo4jHandler, int resourceId){
        System.out.println("delete taxon has no children");
        String parentID = neo4jHandler.deleteNode(nodeId, resourceId);
        //then delete from H-base
        if(!neo4jHandler.hasSibling(nodeId, resourceId) &&
                !neo4jHandler.nodeHasTaxonID(parentID, resourceId)){
            deleteNodeHasNoChildren(parentID, neo4jHandler, resourceId);

        }
    }

    public abstract void deleteFromTaxonFile(String nodeID);

    public void updateTaxon(String nodeID, int resourceId, Neo4jHandler neo4jHandler){
        neo4jHandler.updateTaxon(nodeID, resourceId);
    }


}
