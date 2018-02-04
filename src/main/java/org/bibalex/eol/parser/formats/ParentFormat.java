package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.Taxon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Amr Morad
 */

public class ParentFormat extends Format {

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final Logger logger = LoggerFactory.getLogger(ParentFormat.class);
    private HashSet<String> missingParents;

    public ParentFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
        this.missingParents = new HashSet<>();
    }

    public void handleLines(ArrayList<Taxon> nodes) {
        System.out.println("start handling");
        for(Taxon node : nodes) {
            if(handleLine(node)){
                logger.debug("Handling line with taxon id: " + node.getIdentifier() + " is successful");
                System.out.println("Handling line with taxon id: " + node.getIdentifier() + " is successful");
            }else {
                logger.debug("Error in handling line with taxon id: " + node.getIdentifier());
                System.out.println("Error in handling line with taxon id: " + node.getIdentifier());
            }
        }
    }

    private boolean handleLine(Taxon node){
        int parentGeneratedNodeId = createParentIfNotExist(node.getParentTaxonId());
        int originalGeneratedNodeId = createOriginalNode(node.getIdentifier(), node.getScientificName(),
                node.getTaxonRank(), node.getTaxonomicStatus(), node.getAcceptedNodeId(), parentGeneratedNodeId);
        if(originalGeneratedNodeId > 0){
            logger.debug("Successfully created the original node");
            System.out.println("Successfully created the original node");
            return true;
        }else{
            logger.debug("failure in creation of original node");
            System.out.println("failure in creation of original node");
            return false;
        }
    }

    private int createParentIfNotExist(String parentUsageId){
        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(parentUsageId, resourceId);
        System.out.println("In create parent: " + parentGeneratedNodeId);
        if(parentGeneratedNodeId > 0){
            logger.debug("parent exists");
        }else{
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            parentGeneratedNodeId = neo4jHandler.createParentWithPlaceholder(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private int createOriginalNode(String nodeId, String scientificName, String rank, String taxonomicStatus,
                                       String acceptedNodeId, int parentGeneratedNodeId){
        deleteFromMissingParentsIfExist(nodeId);
        int generatedNodeId;
        if(isSynonym(taxonomicStatus)){
            logger.debug("The node is synonym");
            SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, neo4jHandler);
            generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, acceptedNodeId);
        }else{
            logger.debug("The node is not synonym");
            generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                    neo4jHandler);
        }
        return generatedNodeId;
    }

    private void deleteFromMissingParentsIfExist(String nodeId){
        if(missingParents.contains(nodeId))
            missingParents.remove(nodeId);
    }
}