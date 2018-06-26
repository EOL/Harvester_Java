package com.bibalex.taxonmatcher.models;

/**
 * Created by Amr.Morad on 3/21/2017.
 */
public class MatchingScore {
    int numberOfMatchingChildren;
    int numberOfMatchingAncestors;
    double score;
    int pageId;
    int nodeId;

    public MatchingScore(int numberOfMatchingChildren, int numberOfMatchingAncestors, double score, int pageId, int nodeId) {
        this.numberOfMatchingChildren = numberOfMatchingChildren;
        this.numberOfMatchingAncestors = numberOfMatchingAncestors;
        this.score = score;
        this.pageId = pageId;
        this.nodeId = nodeId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getPageId() {

        return pageId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getNumberOfMatchingChildren() {
        return numberOfMatchingChildren;
    }

    public int getNumberOfMatchingAncestors() {
        return numberOfMatchingAncestors;
    }

    public double getScore() {
        return score;
    }

    public void setNumberOfMatchingChildren(int numberOfMatchingChildren) {
        this.numberOfMatchingChildren = numberOfMatchingChildren;
    }

    public void setNumberOfMatchingAncestors(int numberOfMatchingAncestors) {
        this.numberOfMatchingAncestors = numberOfMatchingAncestors;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
