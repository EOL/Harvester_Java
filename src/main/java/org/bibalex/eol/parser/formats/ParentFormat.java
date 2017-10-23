package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Amr Morad
 */

public class ParentFormat implements Format {

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = LoggerFactory.getLogger(ParentFormat.class);
    private HashSet<String> missingParents;

    public ParentFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
        this.missingParents = new HashSet<>();
    }

    private int createParent(String parentUsageId){
        int parentGeneratedNodeId = neo4jHandler.getNode(parentUsageId, resourceId);
        if(parentGeneratedNodeId > 0){
            logger.debug("parent exists");
        }else{
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            neo4jHandler.createIfNotExist_node_parentFormat(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private boolean createOriginalNode(){
        boolean success;
        SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, generatedNodeId);
        if(synonymNodeHandler.isSynonym(taxonomicStatus))
            success = synonymNodeHandler.handleSynonymNode(acceptedNodeId, rank);
        else
            success = handleNonSynonymNode(scientificName, rank, parentGeneratedNodeId, nodeId, synonymNodeHandler);
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
        return success;
    }

    private boolean handleNonSynonymNode(String scientificName, String rank, int parentGeneratedNodeId,
                                         SynonymNodeHandler synonymNodeHandler){
        

    }


}
