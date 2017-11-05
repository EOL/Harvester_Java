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
        for(Taxon node : nodes) {
            if(handleLine(node)){
                logger.debug("Handling line with taxon id: " + node.getIdentifier() + " is successful");
            }else {
                logger.debug("Error in handling line with taxon id: " + node.getIdentifier());
            }
        }
    }

    private boolean handleLine(Taxon node){
        int parentGeneratedNodeId = createParentIfNotExist(node.getParentTaxonId());
        int originalGeneratedNodeId = createOriginalNode(node.getIdentifier(), node.getScientificName(),
                node.getTaxonRank(), node.getTaxonomicStatus(), node.getAcceptedNodeId(), parentGeneratedNodeId);
        if(originalGeneratedNodeId > 0){
            logger.debug("Successfully created the original node");
            return true;
        }else{
            logger.debug("failure in creation of original node");
            return false;
        }
    }

    private int createParentIfNotExist(String parentUsageId){
        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(parentUsageId, resourceId);
        if(parentGeneratedNodeId > 0){
            logger.debug("parent exists");
        }else{
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            neo4jHandler.createParentWithPlaceholder(resourceId, parentUsageId);
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

    public void updateScientificName(String newScientificName, String oldScientificName, String ancestry){
        String taxonID = neo4jHandler.getNodeByAncestry(oldScientificName, ancestry);

        if(Integer.parseInt(taxonID) <0){
            String nodeID = neo4jHandler.getNodebyTaxonID(taxonID);
            neo4jHandler.updateScientificName(nodeID, newScientificName);
            //update in h-base
        }
    }

    public void updateRank(String nodeID, String newRank){
        neo4jHandler.updateRank(nodeID, newRank);
    }

    public void updateAncestry(String scientificName, String rank, String newParentGeneratedNodeId, String taxonID){
        int nodeID = neo4jHandler.createAcceptedNode(resourceId, taxonID, scientificName, rank, null);

        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(newParentGeneratedNodeId, resourceId);
        if(parentGeneratedNodeId > 0){
            neo4jHandler.addLinkBetweenParentAndNode(parentGeneratedNodeId, Integer.toString(nodeID));
        }
        else{
            missingParents.add(newParentGeneratedNodeId);
        }

        if(//!neo4jHandler.nodeHasTaxonID(newParentGeneratedNodeId)){
            //neo4jHandler.deleteNode(newParentGeneratedNodeId);
        }

    }
}