package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.AncestorNode;
import org.bibalex.eol.parser.models.AncestryFormatNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public class AncestryFormat implements Format{

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = LoggerFactory.getLogger(AncestryFormat.class);

    public AncestryFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
    }

    @Override
    public void handleLines(ArrayList<AncestryFormatNode> nodes) {
        for(AncestryFormatNode node : nodes)
            handleLine(node);
    }

    private void handleLine(AncestryFormatNode node){

    }

    //This method assumes the sorting of the records according to the rank
    private void createAncestors(ArrayList<AncestorNode> ancestors){
        //This list will have the ancestors starting from the beginning till that node
        ArrayList<AncestorNode> currentNodeAncestors = new ArrayList<>();
        int parentGeneratedId = 0; //zero because Neo4j starts from one
        for(AncestorNode ancestor : ancestors){
            parentGeneratedId = createIfNotExist(ancestor, ancestorTaxonId, currentNodeAncestors);
            currentNodeAncestors.add(ancestor);
        }
    }

    private void createOriginalNode(String scientificName, String taxonomicStatus, int generatedNodeId, String acceptedNodeId,
                                    String rank, ArrayList<AncestorNode> ancestry, String nodeId){
        boolean success;
        SynonymNodeHandler synonymNodeHandler = new SynonymNodeHandler(resourceId, generatedNodeId);
        if(synonymNodeHandler.isSynonym(taxonomicStatus))
            success = synonymNodeHandler.handleSynonymNode(acceptedNodeId, rank);
        else
            success = handleNonSynonymNode(scientificName, rank, ancestry, nodeId, synonymNodeHandler);
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
    }

    private int createIfNotExist(AncestorNode ancestor, String taxonId,
                                 ArrayList<AncestorNode> currentAncestry){
        return neo4jHandler.createIfNotExistNode_ancestryFormat(resourceId, ancestor.getScientificName(),
                ancestor.getRank(), taxonId, currentAncestry);
    }

    private boolean handleNonSynonymNode(String scientificName, String rank, ArrayList<AncestorNode> ancestry,
                                         String nodeId, SynonymNodeHandler synonymNodeHandler){
        boolean success;
        int generatedNodeId = neo4jHandler.getNodeIfExist_ancestryFormat(scientificName, rank, ancestry);
        if(generatedNodeId > 0)
            success = neo4jHandler.updateNode_ancestryFormat(nodeId, generatedNodeId);
        else
            success = handleNonExistingNode(scientificName, rank, nodeId, ancestry, synonymNodeHandler);
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
        return success;
    }

    private boolean handleNonExistingNode(String scientificName, String rank, String nodeId,
                                          ArrayList<AncestorNode> currentAncestry,
                                          SynonymNodeHandler synonymNodeHandler){
        boolean success;
        //can be changed
        int generatedNodeId = neo4jHandler.createIfNotExistNode_ancestryFormat(resourceId, scientificName, rank,
                nodeId, currentAncestry);
        if(synonymNodeHandler.orphanSynonyms.containsKey(nodeId)){
            success = neo4jHandler.createRelationBetweenNodeAndSynonyms(generatedNodeId);
        }else
            success = true;
        return success;
    }
}
