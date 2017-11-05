package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.AncestorNode;
import org.bibalex.eol.parser.models.Taxon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public class AncestryFormat extends Format{

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = LoggerFactory.getLogger(AncestryFormat.class);

    public AncestryFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
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
        ArrayList<AncestorNode> currentAncestry = adjustNodeAncestry(node);
        int lastNodeGeneratedId = createAncestors(currentAncestry);
        int originalGeneratedNodeId = createOriginalNode(node.getScientificName(), node.getTaxonomicStatus(), node.getAcceptedNodeId(),
                node.getTaxonRank(), lastNodeGeneratedId, node.getIdentifier());
        if(originalGeneratedNodeId > 0){
            logger.debug("Successfully created the original node");
            return true;
        }else{
            logger.debug("failure in creation of original node");
            return false;
        }
    }

    private ArrayList<AncestorNode> adjustNodeAncestry(Taxon taxon){
        ArrayList<AncestorNode> result = new ArrayList<>();
        if(taxon.getKingdom() != null && taxon.getKingdom() != "")
            result.add(new AncestorNode("kingdom", taxon.getKingdom()));
        if(taxon.getPhylum() != null && taxon.getPhylum() != "")
            result.add(new AncestorNode("phylum", taxon.getPhylum()));
        if(taxon.getClass_() != null && taxon.getClass_() != "")
            result.add(new AncestorNode("class", taxon.getClass_()));
        if(taxon.getOrder() != null && taxon.getOrder() != "")
            result.add(new AncestorNode("order", taxon.getOrder()));
        if(taxon.getFamily() != null && taxon.getFamily() != "")
            result.add(new AncestorNode("family", taxon.getFamily()));
        if(taxon.getGenus() != null && taxon.getGenus() != "")
            result.add(new AncestorNode("genus", taxon.getGenus()));
        return result;
    }

    //This method assumes the sorting of the records according to the rank
    private int createAncestors(ArrayList<AncestorNode> ancestors){
        int parentGeneratedId = 0; //zero because Neo4j starts from one
        for(AncestorNode ancestor : ancestors){
            parentGeneratedId = createAncestorIfNotExist(ancestor, ancestorTaxonId, parentGeneratedId);
        }
        //return the last created one
        return parentGeneratedId;
    }

    private int createAncestorIfNotExist(AncestorNode ancestor, String taxonId, int parentGeneratedId){
        return neo4jHandler.createAncestorIfNotExist(resourceId, ancestor.getScientificName(),
                ancestor.getRank(), taxonId, parentGeneratedId);
    }

    private int createOriginalNode(String scientificName, String taxonomicStatus, String acceptedNodeId, String rank,
                                    int parentGeneratedNodeId, String nodeId){
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

    public void updateScientificName(String newScientificName, String oldScientificName, String rank, String ancestry){
        String nodeID = neo4jHandler.getNodeByRank(newScientificName, rank, ancestry);

        if(Integer.parseInt(nodeID) <0){
            nodeID = neo4jHandler.getNodeByRank(oldScientificName, rank, ancestry);
            neo4jHandler.updateScientificName(nodeID, newScientificName);
            //update in h-base
        }
    }

    public void updateRank(String scientificName, String oldRank, String newRank, String ancestry){
        String nodeID = neo4jHandler.getNodeByRank(scientificName, oldRank, ancestry);
        neo4jHandler.updateRank(nodeID, newRank);
    }

    public void updateAncestry(String scientificName, String rank, String oldAnsetry, String newAncestry){
        String nodeID = neo4jHandler.getNodeByRank(scientificName, rank, oldAnsetry);
        neo4jHandler.createBranch(nodeID, newAncestry);
        //deleteFromTaxonFile(nodeID);
    }
}
