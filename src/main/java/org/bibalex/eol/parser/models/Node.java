package org.bibalex.eol.parser.models;

/**
 * Created by Amr.Morad
 */
public class Node {

    String nodeId;
    int resourceId;
    String scientificName;
    int generatedNodeId;
    String rank;
    int parentGeneratedNodeId;
    String parentNodeId;
    String acceptedNodeId;
    int acceptedNodeGeneratedId;
    int pageId;

    public Node(String nodeId, int resourceId, String scientificName, int generatedNodeId, String rank, int parentGeneratedNodeId,
                String parentNodeId, String acceptedNodeId, int acceptedNodeGeneratedId) {
        this.nodeId = nodeId;
        this.resourceId = resourceId;
        this.scientificName = scientificName;
        this.generatedNodeId = generatedNodeId;
        this.rank = rank;
        this.parentGeneratedNodeId = parentGeneratedNodeId;
        this.parentNodeId = parentNodeId;
        this.acceptedNodeId = acceptedNodeId;
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }

    public Node(int resourceId, String nodeId, String scientificName, String rank, int parentGeneratedNodeId, int pageId) {
        this.nodeId = nodeId;
        this.resourceId = resourceId;
        this.scientificName = scientificName;
        this.rank = rank;
        this.parentGeneratedNodeId = parentGeneratedNodeId;
        this.pageId = pageId;
    }

    public Node(int resourceId, String nodeId){
        this.resourceId = resourceId;
        this.nodeId = nodeId;
    }

    public Node(String nodeId, String scientificName, int resourceId){
        this.resourceId = resourceId;
        this.nodeId = nodeId;
        this.scientificName = scientificName;
    }

    public Node(String parentNodeId, int resourceId){
        this.resourceId = resourceId;
        this.parentNodeId = parentNodeId;
    }

    public Node(int generatedNodeId, int  acceptedNodeGeneratedId){
        this.generatedNodeId = generatedNodeId;
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }
    public Node(int resourceId, String nodeId, String scientificName, String rank, String acceptedNodeId, int acceptedNodeGeneratedId){
        this.resourceId = resourceId;
        this.nodeId = nodeId;
        this.scientificName = scientificName;
        this.rank = rank;
        this.acceptedNodeId = acceptedNodeId;
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }

    public Node(int resourceId, String nodeId, String scientificName, int acceptedNodeGeneratedId, String acceptedNodeId){
        this.resourceId = resourceId;
        this.nodeId = nodeId;
        this.scientificName = scientificName;
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
        this.acceptedNodeId = acceptedNodeId;
    }

    public Node (int resourceId, String nodeId, String scientificName, String rank){
        this.resourceId = resourceId;
        this.nodeId = nodeId;
        this.scientificName = scientificName;
        this.rank = rank;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public int getGeneratedNodeId() {
        return generatedNodeId;
    }

    public void setGeneratedNodeId(int generatedNodeId) {
        this.generatedNodeId = generatedNodeId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getParentGeneratedNodeId() {
        return parentGeneratedNodeId;
    }

    public void setParentGeneratedNodeId(int parentGeneratedNodeId) {
        this.parentGeneratedNodeId = parentGeneratedNodeId;
    }

    public String getParentNodeId() {
        return parentNodeId;
    }

    public void setParentNodeId(String parentNodeId) {
        this.parentNodeId = parentNodeId;
    }

    public String getAcceptedNodeId() {
        return acceptedNodeId;
    }

    public void setAcceptedNodeId(String acceptedNodeId) {
        this.acceptedNodeId = acceptedNodeId;
    }

    public int getAcceptedNodeGeneratedId() {
        return acceptedNodeGeneratedId;
    }

    public void setAcceptedNodeGeneratedId(int acceptedNodeGeneratedId) {
        this.acceptedNodeGeneratedId = acceptedNodeGeneratedId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }
}
