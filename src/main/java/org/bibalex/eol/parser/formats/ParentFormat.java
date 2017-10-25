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

    @Override
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
        int parentGeneratedNodeId = createParent(node.getParentTaxonId());
        return createOriginalNode(node.getIdentifier(), node.getScientificName(), node.getTaxonRank(),
                node.getTaxonomicStatus(), node.getParentTaxonId(), parentGeneratedNodeId);
    }

    private int createParent(String parentUsageId){
        int parentGeneratedNodeId = neo4jHandler.getParentNodeIfExist_parentFormat(parentUsageId, resourceId);
        if(parentGeneratedNodeId > 0){
            logger.debug("parent exists");
        }else{
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            neo4jHandler.createParentWithPlaceholder(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private boolean createOriginalNode(String nodeId, String scientificName, String rank, String taxonomicStatus,
                                       String parentNameUsageId, int parentGeneratedNodeId){
        deleteFromMissingParentsIfNeeded(nodeId);
        int generatedNodeId = createNodeIfNotExist(nodeId, scientificName, rank, parentGeneratedNodeId);
        boolean success = handleBeingSynonym(taxonomicStatus, rank, generatedNodeId, nodeId, scientificName,
                parentNameUsageId);
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
        return success;
    }

    private void deleteFromMissingParentsIfNeeded(String nodeId){
        if(missingParents.contains(nodeId))
            missingParents.remove(nodeId);
    }

    private int createNodeIfNotExist(String nodeId, String scientificName, String rank, int parentGeneratedNodeId){
        int generatedNodeId = neo4jHandler.getNodeIfExist_parentFormat(nodeId, scientificName, resourceId);
        if(generatedNodeId <= 0){
            generatedNodeId = neo4jHandler.createIfNotExistNode_parentFormat(resourceId,
                    nodeId, scientificName, rank, parentGeneratedNodeId);
        }
        return generatedNodeId;
    }

    private boolean handleBeingSynonym(String taxonomicStatus, String rank, int generatedNodeId,
                                       String nodeId, String scientificName, String parentNameUsageId){
        SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, generatedNodeId);
        return synonymNodeHandler.isSynonym(taxonomicStatus) ? synonymNodeHandler.handleSynonymNode(parentNameUsageId,
                rank) : handleNonSynonymNode(scientificName, rank, generatedNodeId, nodeId, synonymNodeHandler);
    }

    private boolean handleNonSynonymNode(String scientificName, String rank, int generatedNodeId,
                                         String nodeId, SynonymNodeHandler synonymNodeHandler){
        boolean success = neo4jHandler.updateNode_parentFormat(generatedNodeId, nodeId, scientificName, rank);
        if(success){
            success = deleteFromOrphanSynonymsIfExist(synonymNodeHandler, nodeId, generatedNodeId);
        }
        return success;
    }

    private boolean deleteFromOrphanSynonymsIfExist(SynonymNodeHandler synonymNodeHandler, String nodeId,
                                                    int generatedNodeId){
        if(synonymNodeHandler.orphanSynonyms.containsKey(nodeId))
            return neo4jHandler.createRelationBetweenNodeAndSynonyms(generatedNodeId);
        return true;
    }
}