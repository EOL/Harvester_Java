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
        Node node = new Node(parentUsageId, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createParentPlaceholder"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
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
        Node node = new Node(resourceId, nodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getNeo4jNode"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        Node node = new Node(nodeId, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getAcceptedNode"), node);
        System.out.println("===============================");
        System.out.println("The accepted node is: " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedNodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNeo4jNode"), node);
        System.out.println("===============================");
        System.out.println("A node is created with id " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
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
