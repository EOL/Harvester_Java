package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.AncestorNode;
import org.bibalex.eol.parser.models.Node;
import org.bibalex.eol.parser.models.Taxon;
import org.apache.log4j.Logger;


import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public class AncestryFormat extends Format{

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = Logger.getLogger(AncestryFormat.class);


    public AncestryFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
    }


    public void handleLines(ArrayList<Taxon> nodes, boolean normalResource) {
        for(Taxon node : nodes) {
            if(handleLine(node, normalResource)){
                logger.debug("Handling line with taxon id: " + node.getIdentifier() + " is successful");
            }else {
                logger.debug("Error in handling line with taxon id: " + node.getIdentifier());
            }
        }
    }

    private boolean handleLine(Taxon node, boolean normalResource){
        ArrayList<AncestorNode> currentAncestry = adjustNodeAncestry(node);
        int lastNodeGeneratedId = createAncestors(currentAncestry);
        int originalGeneratedNodeId=0;
        if(node.getPageEolId() != null)
            originalGeneratedNodeId = createOriginalNode(node.getScientificName(), node.getTaxonomicStatus(), node.getAcceptedNodeId(),
                node.getTaxonRank(), lastNodeGeneratedId, node.getIdentifier(), normalResource, node.getParentTaxonId(), Integer.parseInt(node.getPageEolId()));
        else
            originalGeneratedNodeId = createOriginalNode(node.getScientificName(), node.getTaxonomicStatus(), node.getAcceptedNodeId(),
                    node.getTaxonRank(), lastNodeGeneratedId, node.getIdentifier(), normalResource, node.getParentTaxonId(), 0);
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
                ancestor.getRank(), taxonId, parentGeneratedId, 0);
    }

    private int createOriginalNode(String scientificName, String taxonomicStatus, String acceptedNodeId, String rank,
                                    int parentGeneratedNodeId, String nodeId, boolean normalResource, String parentUsageId, int pageId){
        int generatedNodeId;
        if(normalResource) {
            if (acceptedNodeId != null && !acceptedNodeId.equalsIgnoreCase(nodeId)) {
                logger.debug("The node is synonym");
                SynonymNodeHandler synonymNodeHandler = SynonymNodeHandler.getSynonymNodeHandler (resourceId, neo4jHandler);
                generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, acceptedNodeId);
            }
            else {
                logger.debug("The node is not synonym");
                generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                        neo4jHandler, pageId);
            }
        }
        else {
            if (isSynonym(taxonomicStatus)) {
                // as it synonym and we don't have acceptedNameUsageID so we send parentUsageId instead of acceptedNameUsageId
                logger.debug("The node is synonym");
                System.out.println("The node is synonym");
                SynonymNodeHandler synonymNodeHandler = SynonymNodeHandler.getSynonymNodeHandler(resourceId, neo4jHandler);
                generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, parentUsageId);
            } else {
                logger.debug("The node is not synonym");
                generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                        neo4jHandler, pageId);
            }
        }
        return generatedNodeId;
    }

    @Override
    public int deleteTaxon(String nodeID, int resourceId, String scientificName){
        int generatedNodeId = neo4jHandler.deleteNodeAncestryFormat(nodeID, scientificName, resourceId);
        return generatedNodeId;
    }

    @Override
    public void updateTaxon(Taxon taxon) {
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<AncestorNode> ancestorNodes = adjustNodeAncestry(taxon);
        for(AncestorNode ancestorNode : ancestorNodes){
            nodes.add(new Node(resourceId, ancestorTaxonId,
                    ancestorNode.getScientificName(), ancestorNode.getRank(), 0, 0));
        }
        neo4jHandler.updateTaxonAncestoryFormat(taxon.getIdentifier(), resourceId, taxon.getScientificName(), taxon.getTaxonRank(), taxon.getParentTaxonId(), nodes);
    }

    public void updateScientificName(String newScientificName, String oldScientificName, String rank, String ancestry){
        String nodeID = neo4jHandler.getNodeByRank(newScientificName, rank, ancestry, resourceId);

        if(!(nodeID != null)){
            nodeID = neo4jHandler.getNodeByRank(oldScientificName, rank, ancestry, resourceId);
            neo4jHandler.updateScientificName(nodeID, newScientificName, resourceId);
            //update in h-base
        }
    }

    public void updateRank(String scientificName, String oldRank, String newRank, String ancestry){
        String nodeID = neo4jHandler.getNodeByRank(scientificName, oldRank, ancestry, resourceId);
        neo4jHandler.updateRank(nodeID, newRank, resourceId);
    }

    public void updateAncestry(String scientificName, String rank, String oldAnsetry, String newAncestry){
        String nodeID = neo4jHandler.getNodeByRank(scientificName, rank, oldAnsetry, resourceId);
        neo4jHandler.createBranch(nodeID, newAncestry, resourceId);
        deleteTaxon(nodeID, 1,"");
    }
}
