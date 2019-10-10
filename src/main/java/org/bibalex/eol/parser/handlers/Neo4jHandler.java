package org.bibalex.eol.parser.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bibalex.eol.harvester.HarvesterAPI;
import org.bibalex.eol.parser.models.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AmrMorad
 * This class will be used to call Neo4j and obtain the results
 */
public class Neo4jHandler {

    RestClientHandler restClientHandler;
    private static final Logger logger = LoggerFactory.getLogger(Neo4jHandler.class);


    public Neo4jHandler(){
        restClientHandler = new RestClientHandler();
    }

    /**
     * Methods for ancestry format
     */
    public int createAncestorIfNotExist(int resourceId, String scientificName, String rank, String taxonId,
                                int parentGeneratedId, int pageId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedId, pageId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createAncestor"), node);
//        System.out.println("===============================");
//        System.out.println("The node is " + response);
//        System.out.println("===============================");
        logger.info("Created Node: " + response);
        return Integer.parseInt(response);
    }

    /**
     * Methods for parent format
     */
    public int createParentWithPlaceholder(int resourceId, String parentUsageId){
        //TODO call neo4j and create the parent with placeholder
        Node node = new Node(parentUsageId, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createParentPlaceholder"), node);
//        System.out.println("===============================");
//        System.out.println("The node is " + response);
//        System.out.println("===============================");
        logger.info("Created Node: " + response);

        return Integer.parseInt(response);
    }

    /**
     * General Methods
     */
    public boolean createRelationBetweenNodeAndSynonyms(int acceptedNodeGeneratedId, int synonymNodeId){
        //TODO call neo4j to adjust the relations between the accepted node and its synonyms
        Node node = new Node(synonymNodeId, acceptedNodeGeneratedId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createRelationBetweenNodeAndSynonym"), node);
//        System.out.println("===============================");
//        System.out.println("The node is " + response);
//        System.out.println("===============================");
        logger.info("Created Node: " + response);

        return Boolean.valueOf(response);
    }

    public int getNodeIfExist(String nodeId, int resourceId){
        //TODO call Neo4j to get the node using nodeId and resourceId
//        System.out.println("getNodeIfExist");
        Node node = new Node(resourceId, nodeId);
        System.out.println(resourceId+ " "+ nodeId);
        logger.info("Getting Node: " + resourceId + " " + nodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getNeo4jNode"), node);
//        System.out.println("===============================");
//        System.out.println("The node is " + response);
//        System.out.println("===============================");
        logger.info("Created Node: " + response);
        return Integer.parseInt(response);
    }

    public int getAcceptedNodeIfExist(String nodeId, String scientificName, int resourceId){
        //TODO call neo4j and return true if it exists; false otherwise
        Node node = new Node(nodeId, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getAcceptedNode"), node);
//        System.out.println("===============================");
//        System.out.println("The accepted node is: " + response);
//        System.out.println("===============================");
        logger.info("Accepted Node: " + response);
        return Integer.parseInt(response);
    }

    public int createAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                                 int parentGeneratedNodeId, int pageId){
        //TODO call the neo4j and return the id
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedNodeId, pageId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNeo4jNode"), node);
//        System.out.println("===============================");
//        System.out.println("A node is created with id " + response);
//        System.out.println("===============================");
        logger.info("Created Node with ID: " + response);

        return Integer.parseInt(response);
    }

    public int createSynonymNode(int resourceId, String nodeId, String scientificName, String rank,
                                  String acceptedNodeId, int acceptedNodeGeneratedId){
        //TODO call neo4j and return the id of the generated node
        Node node = new Node(resourceId, nodeId, scientificName, rank, acceptedNodeId, acceptedNodeGeneratedId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createSynonymNode"), node);
//        System.out.println("===============================");
//        System.out.println("A node is created with id " + response);
//        System.out.println("===============================");
        logger.info("Created Node with ID: " + response);
        return Integer.parseInt(response);
    }

    public int getSynonymNodeIfExist(String nodeId, String scientificName, int resourceId, String acceptedNodeId, int
                                     acceptedGeneratedId){
        //TODO call neo4j and return the id of the generated node
        Node node = new Node(resourceId, nodeId, scientificName, acceptedGeneratedId, acceptedNodeId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("getSynonymNode"), node);
//        System.out.println("===============================");
//        System.out.println("A node is created with id " + response);
//        System.out.println("===============================");
        logger.info("Created Node with ID: " + response);

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
//        System.out.println("===============================");
//        System.out.println("A node is deleted that has id " + response);
//        System.out.println("===============================");
        logger.info("Deleted Node with ID: " + response);

        return Integer.valueOf(response);
    }

    public int deleteNodeAncestryFormat (String nodeID, String scientificName, int resourceId){
        //TODO delete node and return its parent id not generated parent id
        Node node = new Node(nodeID, scientificName, resourceId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("deleteNodeAncestryFormat"), node);
//        System.out.println("===============================");
//        System.out.println("A node is deleted that has id " + response);
//        System.out.println("===============================");
        logger.info("Deleted Node with ID: " + response);

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

    public int updateParent(int resourceId, String nodeId, String scientificName, String rank, int parentGeneratedNodeId, int pageId) {
        Node node = new Node(resourceId, nodeId, scientificName, rank, parentGeneratedNodeId, pageId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("createNodeWithFulldata"), node);
//        System.out.println("===============================");
//        System.out.println("A node is created with id " + response);
//        System.out.println("===============================");
        logger.info("Updated Node with ID: " + response);
        return Integer.valueOf(response);
    }

    public int updateTaxonParentFormat(String nodeId, int resourceId, String scientificName, String rank, String parentUsageId) {
        Node node = new Node(resourceId, nodeId, scientificName, rank);
        if(rank == null)
            node.setRank("null");
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("updateParentFormat")+"/"+parentUsageId, node);
//        System.out.println("===============================");
//        System.out.println("A node is updated with response " + response);
//        System.out.println("===============================");
        logger.info("Updated Node with Response: " + response);
        return Integer.valueOf(response);
    }

    public boolean updateTaxonAncestoryFormat(String nodeId, int resourceId, String scientificName, String rank, String parentUsageId, ArrayList<Node> nodes) {
        Node node = new Node(resourceId, nodeId, scientificName, rank);
        if(rank == null)
            node.setRank("null");
        nodes.add(node);
        Boolean response = restClientHandler.updateTaxonInNeo4jAncestoryFormat(PropertiesHandler.getProperty("updateAncestoryFormat"), nodes);
//        System.out.println("===============================");
//        System.out.println("A node is updated " + response);
//        System.out.println("===============================");
        logger.info("Updated Node: " + response);
        return response;
    }

    public int getPageIdOfNode(int generatedNodeId){
        int response = restClientHandler.getPageId(PropertiesHandler.getProperty("getNodePageId"), generatedNodeId);
//        System.out.println("===============================");
//        System.out.println("A node has Page id = " + response);
//        System.out.println("===============================");
        logger.info("Node: " + generatedNodeId + " Has Page ID: " + response);

        return response;
    }

    public ArrayList<Integer> getPageIdsOfNodes(ArrayList<Integer> generated_node_ids){
        Object response = restClientHandler.getPageIds(PropertiesHandler.getProperty("getPageIdsOfNodes"),generated_node_ids);
//        System.out.println("===============================");
//        System.out.println("returned nodes using ids "+ response);
//        System.out.println("===============================");
        logger.info("Returned Nodes Using IDs: " + response);
        ArrayList<Integer>  page_ids = (ArrayList<Integer>)response;
        return page_ids;
//        return generated_node_ids;
    }

    public int updateAcceptedNode(int resourceId, String taxonId, String scientificName, String rank,
                                  int parentGeneratedNodeId, int pageId) {
        Node node = new Node(resourceId, taxonId, scientificName, rank, parentGeneratedNodeId, pageId);
        String response = restClientHandler.doConnection(PropertiesHandler.getProperty("updateAcceptedNode"), node);
//        System.out.println("===============================");
//        System.out.println("A node is updated with response " + response);
//        System.out.println("===============================");
        logger.info("Updated Node with Response: " + response);
        return Integer.valueOf(response);
    }

    public ArrayList<Node> getPlaceholderNodes (int resource_id){

        ArrayList<Node> placeholderNodes = (ArrayList<Node>) restClientHandler.getPlaceholderNodes(PropertiesHandler.getProperty("getPlaceholderNodes")+"/"+resource_id);
//        System.out.println("===============================");
//        System.out.println("get placeholder nodes with response ");
//        System.out.println("===============================");
        logger.info("Called getPlaceholderNodes for Resource: " + resource_id + " Successfully");
        return nodeMapper(placeholderNodes);
    }

    public ArrayList<Node> nodeMapper(ArrayList<Node> beforeMapping)
    {
        ArrayList<Node> afterMapping = new ArrayList<Node>();
        ObjectMapper mapper = new ObjectMapper();
        for(int i = 0 ; i < beforeMapping.size(); i++)
        {
            Node n = mapper.convertValue(beforeMapping.get(i), Node.class);
            afterMapping.add(n);
        }
        return afterMapping;
    }

}
