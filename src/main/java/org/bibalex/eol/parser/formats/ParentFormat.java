package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.Taxon;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Amr Morad
 */

public class ParentFormat extends Format {

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final Logger logger = Logger.getLogger(ParentFormat.class);
    private HashSet<String> missingParents;

    public ParentFormat(int resourceId) {
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
        this.missingParents = new HashSet<>();
    }

    public void handleLines(ArrayList<Taxon> nodes, boolean normalResource) {
        System.out.println("start handling");
        for (Taxon node : nodes) {
            if (handleLine(node, normalResource)) {
                logger.debug("Handling line with taxon id: " + node.getIdentifier() + " is successful");
                System.out.println("Handling line with taxon id: " + node.getIdentifier() + " is successful");
            } else {
                logger.debug("Error in handling line with taxon id: " + node.getIdentifier());
                System.out.println("Error in handling line with taxon id: " + node.getIdentifier());
            }
        }
    }

    private boolean handleLine(Taxon node, boolean normalResource) {
//        int parentGeneratedNodeId = createParentIfNotExist(node.getParentTaxonId());
        int originalGeneratedNodeId = createOriginalNode(node.getIdentifier(), node.getScientificName(),
                node.getTaxonRank(), node.getTaxonomicStatus(), node.getAcceptedNodeId(), normalResource, node.getParentTaxonId());
        if (originalGeneratedNodeId > 0) {
            logger.debug("Successfully created the original node");
            System.out.println("Successfully created the original node");
            return true;
        } else {
            logger.debug("failure in creation of original node");
            System.out.println("failure in creation of original node");
            return false;
        }
    }

    private int createParentIfNotExist(String parentUsageId) {
        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(parentUsageId, resourceId);
        System.out.println("In create parent: " + parentGeneratedNodeId);
        if (parentGeneratedNodeId > 0) {
            logger.debug("parent exists");
        } else {
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            parentGeneratedNodeId = neo4jHandler.createParentWithPlaceholder(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private int createOriginalNode(String nodeId, String scientificName, String rank, String taxonomicStatus,
                                   String acceptedNodeId, boolean normalResource, String parentUsageId) {
        int generatedNodeId;
        if (deleteFromMissingParentsIfExist(nodeId)) {
            int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
            generatedNodeId = handleParentExists(nodeId, scientificName, rank, resourceId, parentGeneratedNodeId, neo4jHandler);
            return generatedNodeId;
        } else {
            if (normalResource) {
                int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
                if (acceptedNodeId != null && !acceptedNodeId.equalsIgnoreCase(nodeId)) {
                    //synonym node
                    logger.debug("The node is synonym");
                    SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, neo4jHandler);
                    generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, acceptedNodeId);
                } else {
                    //accepted node
                    logger.debug("The node is not synonym");
                    generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                            neo4jHandler);
                }
            } else {
                if (isSynonym(taxonomicStatus)) {
                    // as it synonym and we don't have acceptedNameUsageID so we send parentUsageId instead of acceptedNameUsageId
                    logger.debug("The node is synonym");
                    SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, neo4jHandler);
                    generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, parentUsageId);
                } else {
                    int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
                    logger.debug("The node is not synonym");
                    generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                            neo4jHandler);
                }
            }

            return generatedNodeId;
        }
    }

    private int handleParentExists(String nodeId, String scientificName, String rank, int resourceId, int parentGeneratedNodeId, Neo4jHandler neo4jHandler) {
        int generatedAutoId = neo4jHandler.updateParent(resourceId, nodeId, scientificName,
                rank, parentGeneratedNodeId);
        return generatedAutoId;
    }

    private boolean deleteFromMissingParentsIfExist(String nodeId) {
        if (missingParents.contains(nodeId)) {
            missingParents.remove(nodeId);
            return true;
        }
        return false;
    }

    @Override
    public int deleteTaxon(String nodeID, int resourceId, String scientificName) {
        System.out.println("deleteFromTaxonFile");
        int generatedNodeId= neo4jHandler.deleteNodeParentFormat(nodeID, scientificName, resourceId);
        return generatedNodeId;
    }

    @Override
    public boolean updateTaxon(String nodeId, int resourceId, String scientificName, String rank, String parentUsageId) {
        return neo4jHandler.updateTaxonParent(nodeId, resourceId, scientificName, rank, parentUsageId);
    }

    public void updateScientificName(String newScientificName, String oldScientificName, String ancestry) {
        String taxonID = neo4jHandler.getNodeByAncestry(newScientificName, ancestry, resourceId);

        if (!(taxonID != null)) {
            String nodeID = neo4jHandler.getNodebyTaxonID(taxonID, resourceId);
            neo4jHandler.updateScientificName(nodeID, newScientificName, resourceId);
            //update in h-base
        }
    }

    public void updateRank(String nodeID, String newRank) {
        neo4jHandler.updateRank(nodeID, newRank, resourceId);
    }

    public void updateAncestry(String scientificName, String rank, String newParentGeneratedNodeId, String taxonID) {
        int generatedNodeID = neo4jHandler.createAcceptedNode(resourceId, taxonID, scientificName, rank, -1);

        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(newParentGeneratedNodeId, resourceId);
        if (parentGeneratedNodeId > 0) {
            neo4jHandler.addLinkBetweenParentAndNode(parentGeneratedNodeId, Integer.toString(generatedNodeID), resourceId);
        } else {
            missingParents.add(newParentGeneratedNodeId);
        }

        if (!neo4jHandler.nodeHasTaxonID(newParentGeneratedNodeId, resourceId)) {
            neo4jHandler.deleteNodeWithGeneratedID(newParentGeneratedNodeId);
        }

    }
}