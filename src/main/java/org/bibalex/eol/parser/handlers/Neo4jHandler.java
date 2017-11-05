package org.bibalex.eol.parser.handlers;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    /**
     * Methods for ancestry format
     */
    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                int parentGeneratedId){
        //TODO call the neo4j and return the id
        return 1;
    }

    /**
     * Methods for parent format
     */
    public int createParentWithPlaceholder(int resourceId, String parentUsageId){
        //TODO call neo4j and create the parent with placeholder
        return 1;
    }

    /**
     * General Methods
     */
    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        return true;
    }

    public int getNodeIfExist(String nodeId, int resourceId){
        //TODO call Neo4j to get the node using parentUsageId and resourceId
        return 1;
    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        return 1;
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId){
        //TODO call the neo4j and return the id
        return 1;
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                  String acceptedNodeId, int acceptedNodeGeneratedId){
        //TODO call neo4j and return the id of the generated node
        return 1;
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
                                     acceptedGeneratedId){
        //TODO call neo4j and return the id of the generated node
        return 1;
    }

    public String getNodeByRank(String newScientificName, String rank, String ancestry) {
        //TODO search for this node if found return its id else return negative number
        return "-1";
    }

    public void updateScientificName(String nodeID, String newScientificName) {
        //TODO update scientific name of this node
    }

    public void updateRank(String nodeID, String newRank) {
        //TODO update rank of this node
    }

    public void createBranch(String nodeID, String newAncestry) {
        //TODO create a new branch with new ancestry
    }

    public String getNodeByAncestry(String newScientificName, String ancestry) {
        //TODO search for this node if found return its taxon_id else return negative number
        return "-1";
    }

    public String getNodebyTaxonID(String taxonID) {
        //TODO search fro node by only taxon_id
        return "1";
    }

    public void addLinkBetweenParentAndNode(int parentGeneratedNodeId, String nodeID) {
       //TODO link between parent and node
    }
}
