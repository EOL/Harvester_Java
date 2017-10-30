package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.parser.models.AncestorNode;

import java.util.ArrayList;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    /**
     * Methods for ancestry format
     */
    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                int parentGeneratedId){
        //TODO call the neo4j and return the id
        return 1;
    }

    // This returns either the generated node id if exists or -1 if not exist
    public int getNodeIfExist_ancestryFormat(String scientificName, String rank, ArrayList<AncestorNode> ancestry){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

//    public boolean updateNode_ancestryFormat(String nodeId, int generatedNodeId){
//        //TODO call neo4j and update the value of the nodeid
//        return true;
//    }

    /**
     * Methods for parent format
     */

//    public int getNodeIfExist_parentFormat(String nodeId, String scientificName, int resourceId){
//        //TODO call Neo4j to get the node using parentUsageId and resourceId
//        return 1;
//    }

//    public int createIfNotExistNode_parentFormat(int resourceId, String taxonId, String scientificName, String rank,
//                                                 int parentGeneratedNodeId){
//        //TODO call the neo4j and return the id
//        return 1;
//    }

    public int createParentWithPlaceholder(int resourceId, String parentUsageId){
        //TODO call neo4j and create the parent with placeholder
        return 1;
    }

//    public boolean updateNode_parentFormat(int generatedNodeId, String nodeId, String scirntificName, String rank){
//        //TODO call neoj and update the node in parent format
//        return true;
//    }

    /**
     * General Methods
     */
//    public int searchAcceptedNode(String nodeId){
//        //TODO call neo4j and return true if it exists; false otherwise
//        return 1;
//    }
//
//    public int createNode_synonym(int resourceId, String taxonId, String rank){
//        //TODO call neo4j and return the generated id
//        return 1;
//    }
//
//    public boolean addSynonymRelationship(int currentNodeGeneratedId, int acceptedNodeGeneratedId){
//        //TODO call neo4j to add the relationship between the nodes
//        return true;
//    }

    public boolean createRelationBetweenNodeAndSynonyms(int generatedNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        return true;
    }

    public boolean updateNode(int generatedNodeId, String nodeId, String scirntificName, String rank){
        //TODO call neoj and update the node in parent format
        return true;
    }

    public int getNodeIfExist(String nodeId, int resourceId){
        //TODO call Neo4j to get the node using parentUsageId and resourceId
        return 1;
    }

    public int getAcceptedNodeIfExist(String scientificName, String rank, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId){
        //TODO call the neo4j and return the id
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
