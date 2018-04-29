package org.bibalex.eol.parser.handlers;

import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.models.Node;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    RestClientHandler restClientHandler;

    public Neo4jHandler(){
        restClientHandler = new RestClientHandler();
    }

    /**
     * Methods for ancestry format
     */
    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                int parentGeneratedId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createAncestor"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    /**
     * Methods for parent format
     */
    public int createParentWithPlaceholder(int resourceId, String parentUsageId){
        //TODO call neo4j and create the parent with placeholder
        Node node = new Node(parentUsageId, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createParentPlaceholder"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    /**
     * General Methods
     */
    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        Node node = new Node(synonymNodeId, acceptedNodeGeneratedId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createRelationBetweenNodeAndSynonym"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Boolean.valueOf(response);
    }

    public int getNodeIfExist(String nodeId, int resourceId){
        //TODO call Neo4j to get the node using nodeId and resourceId
        System.out.println("getNodeIfExist");
        Node node = new Node(resourceId, nodeId);
        System.out.println(resourceId+ " "+ nodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getNeo4jNode"), node);
        System.out.println("===============================");
        System.out.println("The node is " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        Node node = new Node(nodeId, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getAcceptedNode"), node);
        System.out.println("===============================");
        System.out.println("The accepted node is: " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedNodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNeo4jNode"), node);
        System.out.println("===============================");
        System.out.println("A node is created with id " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                  String acceptedNodeId, int acceptedNodeGeneratedId){
        //TODO call neo4j and return the id of the generated node
        Node node = new Node(resourceId, nodeId, scientificName, rank, acceptedNodeId, acceptedNodeGeneratedId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNeo4jNode"), node);
        System.out.println("===============================");
        System.out.println("A node is created with id " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
                                     acceptedGeneratedId){
        //TODO call neo4j and return the id of the generated node
        Node node = new Node(resourceId, nodeId, scientificName, acceptedGeneratedId, acceptedNodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getSynonymNode"), node);
        System.out.println("===============================");
        System.out.println("A node is created with id " + response);
        System.out.println("===============================");
        return Integer.parseInt(response);
    }

    public boolean hasChildren (String nodeId, int resourceId){
        //TODO check if node has childern or not
        return false;
    }

    public boolean hasSibling (String nodeID, int resourceId){
        //TODO check if node has siblings
        return true;
    }

    public boolean nodeHasTaxonID(String nodeId, int resourceId) {
        //TODO check if node has taxon_id or not
        return true;
    }

    public int deleteNodeParentFormat (String nodeID, String scientificName, int resourceId){
        //TODO delete node and return its parent id not generated parent id
        Node node = new Node(nodeID, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("deleteNodeParentFormat"), node);
        System.out.println("===============================");
        System.out.println("A node is deleted that has id " + response);
        System.out.println("===============================");
        return Integer.valueOf(response);
    }

    public int deleteNodeAncestryFormat (String nodeID, String scientificName, int resourceId){
        //TODO delete node and return its parent id not generated parent id
        Node node = new Node(nodeID, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("deleteNodeAncestryFormat"), node);
        System.out.println("===============================");
        System.out.println("A node is deleted that has id " + response);
        System.out.println("===============================");
        return Integer.valueOf(response);
    }

    public void deleteNodeWithGeneratedID (String generatedNodeID){
        //TODO delete node that has this generatedNodeID
    }

    public void markNodeAsPlaceholder(String nodeID, int resourceId) {
        //TODO mark this node as placeholder
    }

    public void updateTaxon(String nodeID, int resourceId){
        //TODO update taxon
    }

    public String getNodeByRank(String newScientificName, String rank, String ancestry, int resourceId) {
        //TODO search for this node if found return its resource id else return null
        return "-1";
    }

    public void updateScientificName(String nodeID, String newScientificName, int resourceId) {
        //TODO update scientific name of this node
    }

    public void updateRank(String nodeID, String newRank, int resourceId) {
        //TODO update rank of this node
    }

    public void createBranch(String nodeID, String newAncestry, int resourceId) {
        //TODO create a new branch with new ancestry
    }

    public String getNodeByAncestry(String newScientificName, String ancestry, int resourceId) {
        //TODO search for this node if found return its taxon_id else return null
        return "-1";
    }

    public String getNodebyTaxonID(String taxonID, int resourceId) {
        //TODO search for node by only taxon_id
        return "1";
    }

    public void addLinkBetweenParentAndNode(int parentGeneratedNodeId, String nodeID, int resourceId) {
        //TODO link between parent and node
    }

    public int updateParent(int resourceId, String nodeId, String scientificName, String rank, int parentGeneratedNodeId) {
        Node node = new Node(resourceId, nodeId, scientificName, rank, parentGeneratedNodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNodeWithFulldata"), node);
        System.out.println("===============================");
        System.out.println("A node is created with id " + response);
        System.out.println("===============================");
        return Integer.valueOf(response);
    }

    public boolean updateTaxonParent(String nodeId, int resourceId, String scientificName, String rank, String parentUsageId) {
        Node node = new Node(resourceId, nodeId, scientificName, rank);
        Boolean response = restClientHandler.updateTaxonNeo4j(PropertiesHandler.getProperty("updateParentFormat")+"/"+parentUsageId, node);
        System.out.println("===============================");
        System.out.println("A node is updated with id " + response);
        System.out.println("===============================");
        return response;
    }
}
