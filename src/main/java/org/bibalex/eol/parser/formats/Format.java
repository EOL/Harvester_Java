package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.Taxon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public abstract class Format {

//    protected static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = LoggerFactory.getLogger(Format.class);

    public abstract void handleLines(ArrayList<Taxon> nodes);

    public static boolean isSynonym(String taxonomicStatus){
        //TODO implement isSynonym method
        return true;
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
        int generatedNodeId = neo4jHandler.getAcceptedNodeIfExist(nodeId, scientificName, resourceId);
        if(generatedNodeId <= 0){
            generatedNodeId = neo4jHandler.createAcceptedNode(resourceId, nodeId, scientificName,
                    rank, parentGeneratedNodeId);
        }
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


}
