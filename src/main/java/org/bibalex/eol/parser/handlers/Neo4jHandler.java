package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.parser.models.AncestorNode;

import java.util.ArrayList;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    public int createIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                ArrayList<AncestorNode> currentAncestry, int parentGeneratedId){
        //TODO call the neo4j and return the id
        return 1;
    }

    public boolean searchAcceptedNode(String nodeId){
        //TODO call neo4j and return true if it exists; false otherwise
    }
}
