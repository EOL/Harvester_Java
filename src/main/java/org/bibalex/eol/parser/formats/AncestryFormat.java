package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.AncestorNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Amr Morad
 */
public class AncestryFormat implements Format{

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private SynonymNodeHandler synonymNodeHandler;
    private static final String ancestorTaxonId = "placeholder";
    private static final Logger logger = LoggerFactory.getLogger(AncestryFormat.class);


    public AncestryFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
        this.synonymNodeHandler = new SynonymNodeHandler();
    }

    @Override
    public void handleLine() {

    }

    //This method assumes the sorting of the records according to the rank
    private void createAncestors(ArrayList<AncestorNode> ancestors){
        //This list will have the ancestors starting from the beginning till that node
        ArrayList<AncestorNode> currentNodeAncestors = new ArrayList<>();
        int parentGeneratedId = 0; //zero because Neo4j starts from one
        for(AncestorNode ancestor : ancestors){
            parentGeneratedId = createIfNotExist(ancestor, ancestorTaxonId, currentNodeAncestors, parentGeneratedId);
            currentNodeAncestors.add(ancestor);
        }
    }

    private void createOriginalNode(String taxonomicStatus){
        boolean success;
        if(synonymNodeHandler.isSynonym(taxonomicStatus))
            success = synonymNodeHandler.handleSynonymNode();
        else
            success = handleNonSynonymNode();
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
    }

    private int createIfNotExist(AncestorNode ancestor, String taxonId,
                                 ArrayList<AncestorNode> currentAncestry, int parentGeneratedId){
        return neo4jHandler.createIfNotExist_node(resourceId, ancestor.getScientificName(),
                ancestor.getRank(), taxonId, currentAncestry, parentGeneratedId);
    }

    private boolean handleNonSynonymNode(String scientificName, String rank, ArrayList<AncestorNode> ancestry,
                                         String nodeId){
        boolean success;
        int generatedNodeId = neo4jHandler.getNodeIfExist(scientificName, rank, ancestry);
        if(generatedNodeId > 0)
            success = neo4jHandler.updateNode(nodeId, generatedNodeId);
        else
            success = handleNonExistingNode();
        if(success)
            logger.debug("created original node successfully");
        else
            logger.debug("Failure in the creation of the original node");
        return success;
    }

    private boolean handleNonExistingNode(String scientificName, String rank, String taxonId,
                                          ArrayList<AncestorNode> currentAncestry, int parentGeneratedId){
        //can be changed
        int generatedNodeId = neo4jHandler.createIfNotExist_node(resourceId, scientificName, rank,
                taxonId, currentAncestry, parentGeneratedId);

    }
}
