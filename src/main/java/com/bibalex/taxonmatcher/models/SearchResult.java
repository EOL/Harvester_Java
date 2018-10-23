package com.bibalex.taxonmatcher.models;

import java.util.ArrayList;

/**
 * Created by amr.morad.
 */
public class SearchResult {

    private int nodeId;
    private int pageId;
    private ArrayList<Integer> children;
    private ArrayList<Integer> ancestors;
    private String scientific_name ;

    public SearchResult(int nodeId, int pageId, ArrayList<Integer> children, ArrayList<Integer> ancestors, String scientific_name) {
        this.nodeId = nodeId;
        this.pageId = pageId;
        this.children = children;
        this.ancestors = ancestors;
        this.scientific_name = scientific_name;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public void setChildren(ArrayList<Integer> children) {
        this.children = children;
    }

    public void setAncestors(ArrayList<Integer> ancestors) {
        this.ancestors = ancestors;
    }

    public  void setScientificName(String scientific_name){this.scientific_name = scientific_name; }

    public int getPageId() {
        return pageId;
    }

    public ArrayList<Integer> getChildren() {
        return children;
    }

    public ArrayList<Integer> getAncestors() {
        return ancestors;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getScientificName() {
        return scientific_name;
    }
}
