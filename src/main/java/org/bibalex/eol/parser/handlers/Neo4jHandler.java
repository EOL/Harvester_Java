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
    public int createIfNotExistNode_ancestryFormat(int resourceId, String scientificName, String rank, String taxonId,
                                ArrayList<AncestorNode> currentAncestry){
        //TODO call the neo4j and return the id
        return 1;
    }

    // This returns either the generated node id if exists or -1 if not exist
    public int getNodeIfExist_ancestryFormat(String scientificName, String rank, ArrayList<AncestorNode> ancestry){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public boolean updateNode_ancestryFormat(String nodeId, int generatedNodeId){
        //TODO call neo4j and update the value of the nodeid
        return true;
    }

    /**
     * Methods for parent format
     */
    public int getParentNodeIfExist_parentFormat(String parentUsageId, int resourceId){
        //TODO call Neo4j to get the node using parentUsageId and resourceId
        return 1;
    }

    public int getNodeIfExist_parentFormat(String nodeId, String scientificName, int resourceId){
        //TODO call Neo4j to get the node using parentUsageId and resourceId
        return 1;
    }

    public int createIfNotExistNode_parentFormat(int resourceId, String taxonId, String scientificName, String rank){
        //TODO call the neo4j and return the id
        return 1;
    }

    public boolean updateNode_parentFormat(int generatedNodeId, String nodeId, String scirntificName, String rank){
        //TODO call neoj and update the node in parent format
        return true;
    }

    /**
     * General Methods
     */
    public boolean searchAcceptedNode(String nodeId){
        //TODO call neo4j and return true if it exists; false otherwise
        return true;
    }

    public int createNode_synonym(int resourceId, String taxonId, String rank){
        //TODO call neo4j and return the generated id
        return 1;
    }

    public boolean addSynonymRelationship(int currentNodeGeneratedId, int acceptedNodeGeneratedId){
        //TODO call neo4j to add the relationship between the nodes
        return true;
    }

    public boolean createRelationBetweenNodeAndSynonyms(int generatedNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        return true;
    }
}
