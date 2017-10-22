package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.parser.models.AncestorNode;

import java.util.ArrayList;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    public int createIfNotExist_node(int resourceId, String scientificName, String rank, String taxonId,
                                ArrayList<AncestorNode> currentAncestry, int parentGeneratedId){
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
}
