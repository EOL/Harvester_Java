package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.parser.models.AncestorNode;

import java.util.ArrayList;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    public int createIfNotExist_node_ancestryFormat(int resourceId, String scientificName, String rank, String taxonId,
                                ArrayList<AncestorNode> currentAncestry){
        //TODO call the neo4j and return the id
        return 1;
    }

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

    // This returns either the generated node id if exists or -1 if not exist
    public int getNodeIfExist(String scientificName, String rank, ArrayList<AncestorNode> ancestry){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public boolean updateNode(String nodeId, int generatedNodeId){
        //TODO call neo4j and update the value of the nodeid
        return true;
    }

    public boolean createRelationBetweenNodeAndSynonyms(int generatedNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        return true;
    }

    public int getNode(String parentUsageId, int resourceId){
        //TODO call Neo4j to get the node using parentUsageId and resourceId
        return 1;
    }

    public int createIfNotExist_node_parentFormat(int resourceId, String taxonId){
        //TODO call the neo4j and return the id
        return 1;
    }
}
