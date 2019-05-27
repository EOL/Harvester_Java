package org.bibalex.eol.parser.models;

import java.util.ArrayList;

public class Taxon {
    String identifier;
    String taxonId;
    String scientificName;
    String parentTaxonId;
    String kingdom;
    String phylum;
    String class_;
    String order;
    String family;
    String genus;
    String taxonRank;
    String furtherInformationURI;
    String taxonomicStatus;
    String taxonRemarks;
    String namePublishedIn;
    String referenceId;
    String pageEolId;
    String acceptedNodeId;
    String source;
    String canonicalName;
    String scientificNameAuthorship;
    String scientificNameID;
    String datasetId;
    String eolIdAnnotations;
    String deltaStatus;
    String landmark;

    ArrayList<String> guids;

    public Taxon(String identifier, String scientificName, String parentTaxonId, String kingdom, String phylum, String taxonClass, String order,
                 String family, String genus, String taxonRank, String furtherInformationURI, String taxonomicStatus, String taxonRemarks,
                 String namePublishedIn, String referenceId, String pageEolId, String acceptedNodeId, String source, String canonicalName,
                 String scientificNameAuthorship, String scientificNameID, String datasetId, String eolIdAnnotations, String deltaStatus, String landmark) {
        this.identifier = identifier;
        this.taxonId = identifier;
        this.scientificName = scientificName;
        this.parentTaxonId = parentTaxonId;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.class_ = taxonClass;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.taxonRank = taxonRank;
        this.furtherInformationURI = furtherInformationURI;
        this.taxonomicStatus = taxonomicStatus;
        this.taxonRemarks = taxonRemarks;
        this.namePublishedIn = namePublishedIn;
        this.referenceId = referenceId;
        this.pageEolId = pageEolId;
        this.acceptedNodeId = acceptedNodeId;
        this.source = source;
        this.canonicalName = canonicalName;
        this.scientificNameAuthorship = scientificNameAuthorship;
        this.scientificNameID = scientificNameID;
        this.datasetId = datasetId;
        this.eolIdAnnotations = eolIdAnnotations;
        this.deltaStatus = deltaStatus;
        this.landmark = landmark;
    }

    public Taxon(String identifier, String scientificName,String taxonRank,String pageEolId){
        this.identifier = identifier;
        this.taxonId = identifier;
        this.scientificName = scientificName;
        this.taxonRank = taxonRank;
        this.pageEolId = pageEolId;
    }

    public ArrayList<String> getGuids() {
        return guids;
    }

    public void setGuids(ArrayList<String> guids) {
        this.guids = guids;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    public void setScientificNameAuthorship(String scientificNameAuthorship) {
        this.scientificNameAuthorship = scientificNameAuthorship;
    }

    public String getScientificNameID() {
        return scientificNameID;
    }

    public void setScientificNameID(String scientificNameID) {
        this.scientificNameID = scientificNameID;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getEolIdAnnotations() {
        return eolIdAnnotations;
    }

    public void setEolIdAnnotations(String eolIdAnnotations) {
        this.eolIdAnnotations = eolIdAnnotations;
    }

    public String getAcceptedNodeId() {
        return acceptedNodeId;
    }

    public void setAcceptedNodeId(String acceptedNodeId) {
        this.acceptedNodeId = acceptedNodeId;
    }

    public String getIdentifier() {

        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getParentTaxonId() {
        return parentTaxonId;
    }

    public void setParentTaxonId(String parentTaxonId) {
        this.parentTaxonId = parentTaxonId;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getTaxonRank() {
        return taxonRank;
    }

    public void setTaxonRank(String taxonRank) {
        this.taxonRank = taxonRank;
    }

    public String getFurtherInformationURI() {
        return furtherInformationURI;
    }

    public void setFurtherInformationURI(String furtherInformationURI) {
        this.furtherInformationURI = furtherInformationURI;
    }

    public String getTaxonomicStatus() {
        return taxonomicStatus;
    }

    public void setTaxonomicStatus(String taxonomicStatus) {
        this.taxonomicStatus = taxonomicStatus;
    }

    public String getTaxonRemarks() {
        return taxonRemarks;
    }

    public void setTaxonRemarks(String taxonRemarks) {
        this.taxonRemarks = taxonRemarks;
    }

    public String getNamePublishedIn() {
        return namePublishedIn;
    }

    public void setNamePublishedIn(String namePublishedIn) {
        this.namePublishedIn = namePublishedIn;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getClass_() {
        return class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    public String getPageEolId() {
        return pageEolId;
    }

    public void setPageEolId(String pageEolId) {
        this.pageEolId = pageEolId;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
}
