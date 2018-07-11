package org.bibalex.eol.parser.handlers;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Amr Morad
 */
public class SynonymNodeHandler {

    private Neo4jHandler neo4jHandler;
    private int resourceId;
    public HashMap<String, ArrayList<Integer>> orphanSynonyms;
    private static final String placeholder = "placeholder";
    private static final Logger logger = Logger.getLogger(SynonymNodeHandler.class);
    private static SynonymNodeHandler synonymNodeHandler = null;

    public static SynonymNodeHandler getSynonymNodeHandler(int resourceId, Neo4jHandler neo4jHandler) {
        if(synonymNodeHandler !=null)
            return synonymNodeHandler;
        else{
            synonymNodeHandler = new SynonymNodeHandler(resourceId, neo4jHandler);
            return synonymNodeHandler;
        }
    }

    public static void setSynonymNodeHandler(){
        synonymNodeHandler = null;
    }

    private SynonymNodeHandler(int resourceId, Neo4jHandler neo4jHandler){
        this.neo4jHandler = neo4jHandler;
        this.resourceId = resourceId;
        this.orphanSynonyms = new HashMap<>();
    }

    public int handleSynonymNode(String nodeId, String scientificName, String rank, String acceptedNodeId){
        if(acceptedNodeId != null) {
            int acceptedNodeGeneratedId = neo4jHandler.getNodeIfExist(acceptedNodeId, resourceId);
            int synonymNodeGeneratedId;
            boolean acceptedNotExist = false;
            if (acceptedNodeGeneratedId <= 0) {
                acceptedNotExist = true;
                acceptedNodeGeneratedId = handleSynonym_acceptedNodeNotExist(acceptedNodeId);
            }
            synonymNodeGeneratedId = createSynonymIfNotExist(nodeId, scientificName, rank, acceptedNodeId,
                    acceptedNodeGeneratedId);
            if (acceptedNotExist) {
                ArrayList<Integer> synonyms = orphanSynonyms.get(acceptedNodeId);
                synonyms.add(synonymNodeGeneratedId);
                orphanSynonyms.put(acceptedNodeId, synonyms);
            }
            return synonymNodeGeneratedId;
        }
        else{
            logger.error("there isn't accepted node id");
            return -1;
        }
    }

    private int createSynonymIfNotExist(String nodeId, String scientificName, String rank, String acceptedNodeId,
                                        int acceptedNodeGeneratedId){
//        int generatedNodeId = neo4jHandler.getSynonymNodeIfExist(nodeId, scientificName, resourceId, acceptedNodeId,
//                acceptedNodeGeneratedId);
//        if(generatedNodeId <= 0){
            int generatedNodeId = neo4jHandler.createSynonymNode(resourceId, nodeId, scientificName, rank, acceptedNodeId,
                    acceptedNodeGeneratedId);
//        }
        return generatedNodeId;
    }

    private int handleSynonym_acceptedNodeNotExist(String acceptedNodeId){
        int acceptedNodeGeneratedId = neo4jHandler.createAcceptedNode(resourceId, acceptedNodeId, placeholder, "",
                0, 0);
        orphanSynonyms.put(acceptedNodeId, new ArrayList<>());
        return acceptedNodeGeneratedId;
    }

}
