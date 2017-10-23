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
        int parentGeneratedNodeId = neo4jHandler.getParentNodeIfExist_parentFormat(parentUsageId, resourceId);
        if(parentGeneratedNodeId > 0){
            logger.debug("parent exists");
        }else{
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            neo4jHandler.createIfNotExistNode_parentFormat(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private boolean createOriginalNode(String nodeId, String scientificName, String rank, String taxonomicStatus,
                                       int parentGeneratedNodeId){
        boolean success;
        int generatedNodeId = createNodeIfNotExist(nodeId, scientificName, rank);
        SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, generatedNodeId);
        if(synonymNodeHandler.isSynonym(taxonomicStatus))
            success = synonymNodeHandler.handleSynonymNode(acceptedNodeId, rank);
        else
            success = handleNonSynonymNode(scientificName, rank, generatedNodeId, nodeId, synonymNodeHandler);
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
        return success;
    }

    private int createNodeIfNotExist(String nodeId, String scientificName, String rank){
        int generatedNodeId = neo4jHandler.getNodeIfExist_parentFormat(nodeId, scientificName, resourceId);
        if(generatedNodeId <= 0){
            generatedNodeId = neo4jHandler.createIfNotExistNode_parentFormat(resourceId,
                    nodeId, scientificName, rank);
        }
        return generatedNodeId;
    }

    private boolean handleNonSynonymNode(String scientificName, String rank, int generatedNodeId,
                                         String nodeId, SynonymNodeHandler synonymNodeHandler){
        boolean success = neo4jHandler.updateNode_parentFormat(generatedNodeId, nodeId, scientificName, rank);
        if(missingParents.contains())

    }


}
