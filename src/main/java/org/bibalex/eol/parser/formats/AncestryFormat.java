package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.models.AncestorNode;
import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public class AncestryFormat implements Format{

    int resourceId;
    Neo4jHandler neo4jHandler;
    private static final String ancestorTaxonId = "placeholder";

    public AncestryFormat(int resourceId){
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
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

    private int createIfNotExist(AncestorNode ancestor, String taxonId,
                                 ArrayList<AncestorNode> currentAncestry, int parentGeneratedId){
        return neo4jHandler.createIfNotExist(resourceId, ancestor.getScientificName(),
                ancestor.getRank(), taxonId, currentAncestry, parentGeneratedId);
    }

    private void createOriginalNode(){
        if()
    }
}
