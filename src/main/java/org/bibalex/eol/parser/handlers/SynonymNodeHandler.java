package org.bibalex.eol.parser.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by Amr Morad
 */
public class SynonymNodeHandler {

    private Neo4jHandler neo4jHandler;
    private int resourceId;
    private int currentNodeGeneratedId;
    public HashMap<String, Integer> orphanSynonyms;
    private static final Logger logger = LoggerFactory.getLogger(SynonymNodeHandler.class);

    public SynonymNodeHandler(int resourceId, int currentNodeGeneratedId){
        this.neo4jHandler = new Neo4jHandler();
        this.resourceId = resourceId;
        this.currentNodeGeneratedId = currentNodeGeneratedId;
        this.orphanSynonyms = new HashMap<>();
    }

    public boolean isSynonym(String taxonomicStatus){
        //TODO implement isSynonym method
        return true;
    }

    public boolean handleSynonymNode(String acceptedNodeId, String rank){
        boolean success;
        boolean existsBefore = neo4jHandler.searchAcceptedNode(acceptedNodeId);
        if(existsBefore){
            logger.debug("Accepted Node exists before");
            success = handleSynonym_parentExist(acceptedNodeId, rank);
        }else{
            logger.debug("Accepted Node does not exist before");
            success = handleSynonym_parentNotExist(acceptedNodeId, rank);
        }
        if(success)
            logger.debug("created the node in Neo4j and add the relationship successfully");
        else
            logger.debug("Failure in the creation of the node in Neo4j OR adding the relationship");
        return success;
    }

    private boolean handleSynonym_parentExist(String acceptedNodeId, String rank){
        int acceptedNodeGeneratedId = neo4jHandler.createNode_synonym(resourceId, acceptedNodeId, rank);
        return neo4jHandler.addSynonymRelationship(currentNodeGeneratedId, acceptedNodeGeneratedId);
    }

    private boolean handleSynonym_parentNotExist(String acceptedNodeId, String rank){
        int acceptedNodeGeneratedId = neo4jHandler.createNode_synonym(resourceId, acceptedNodeId, rank);
        orphanSynonyms.put(acceptedNodeId, acceptedNodeGeneratedId);
        return true;
    }

}
