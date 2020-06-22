package org.bibalex.eol.parser.models;

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;

import java.util.ArrayList;
import java.util.Map;

/*
This class will include the needed objects and attributes and is compatible with MongodB
 */
public class NodeRecord {
    int resourceId;
    String nodeId;
    String _id;
    Integer parentGNId; // neo4j
    Boolean preferred;  // taxon matching
    Integer pageId;     // taxon matching
    String acceptedNameUsageId;
    String rank;
    String canonicalName; // gnparser
    String scientificName;
    String italicizedScientificName;  // gnparser
    String taxonomicStatus;
    String landMark;
    String[] referenceIds; // after parsing referecnes
    String[] identifiers; // TODO
    Boolean hasBreadCrumb; // TODO
    String sourceUrl; //TODO FOUND IN JR'S DOC BUT NOT IN FILE
    String remarks;
    String attributions; // TODO injr but where n file
    ArrayList<VernacularName> vernaculars;
    String deltaStatus;

    public NodeRecord(String generatedNodeId, int resourceId) {
        this._id = generatedNodeId;
        this.resourceId = resourceId;
    }

    public NodeRecord(String nodeId, int resourceId, String _id, String acceptedNameUsageId, String rank,
                      String scientificName,String taxonomicStatus, String landMark, String remarks,
                       String deltaStatus){
        this.nodeId = nodeId;
        this.resourceId = resourceId;
        this._id = _id;
        this.acceptedNameUsageId = acceptedNameUsageId;
        this.rank = rank;
        this.scientificName = scientificName;
        this.taxonomicStatus = taxonomicStatus;
        this.landMark = landMark;
        this.remarks = remarks;
        this.deltaStatus = deltaStatus;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public ArrayList<VernacularName> getVernaculars() {
        return vernaculars;
    }

    public void setVernaculars(ArrayList<VernacularName> vernaculars) {
        this.vernaculars = vernaculars;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getParentGNId() {
        return parentGNId;
    }

    public void setParentGNId(Integer parentGNId) {
        this.parentGNId = parentGNId;
    }

    public Boolean getPreferred() {
        return preferred;
    }

    public void setPreferred(Boolean preferred) {
        this.preferred = preferred;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public String getAcceptedNameUsageId() {
        return acceptedNameUsageId;
    }

    public void setAcceptedNameUsageId(String acceptedNameUsageId) {
        this.acceptedNameUsageId = acceptedNameUsageId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getItalicizedScientificName() {
        return italicizedScientificName;
    }

    public void setItalicizedScientificName(String italicizedScientificName) {
        this.italicizedScientificName = italicizedScientificName;
    }

    public String getTaxonomicStatus() {
        return taxonomicStatus;
    }

    public void setTaxonomicStatus(String taxonomicStatus) {
        this.taxonomicStatus = taxonomicStatus;
    }

    public String getLandMark() {
        return landMark;
    }

    public void setLandMark(String landMark) {
        this.landMark = landMark;
    }

    public String[] getReferenceIds() {
        return referenceIds;
    }

    public void setReferenceIds(String[] referenceIds) {
        this.referenceIds = referenceIds;
    }

    public String[] getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(String[] identifiers) {
        this.identifiers = identifiers;
    }

    public Boolean getHasBreadCrumb() {
        return hasBreadCrumb;
    }

    public void setHasBreadCrumb(Boolean hasBreadCrumb) {
        this.hasBreadCrumb = hasBreadCrumb;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }


}
