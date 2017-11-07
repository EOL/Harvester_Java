package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.handler.PropertiesHandler;
import org.bibalex.eol.parser.models.Node;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    RestClientHandler restClientHandler;

    public Neo4jHandler(){
        restClientHandler = new RestClientHandler();
    }

    /**
     * Methods for ancestry format
     */
    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                int parentGeneratedId){
        //TODO call the neo4j and return the id
        return 1;
    }

    /**
     * Methods for parent format
     */
    public int createParentWithPlaceholder(int resourceId, String parentUsageId){
        //TODO call neo4j and create the parent with placeholder
        return 1;
    }

    /**
     * General Methods
     */
    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        return true;
    }

    public int getNodeIfExist(String nodeId, int resourceId){
        //TODO call Neo4j to get the node using nodeId and resourceId
        return 1;
    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedNodeId);

        String k = restClientHandler.doConnection(PropertiesHandler.getProperty("addEntryHBase"), node);
        System.out.println("===============================");
        System.out.println(k);
        System.out.println("===============================");
        return 1;
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                  String acceptedNodeId, int acceptedNodeGeneratedId){
        //TODO call neo4j and return the id of the generated node
        return 1;
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
                                     acceptedGeneratedId){
        //TODO call neo4j and return the id of the generated node
        return 1;
    }

}
