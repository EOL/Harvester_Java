package org.bibalex.eol.parser.models;

public class Taxa {
    String identifier;
    String scientificName;
    String parentTaxonId;
    String kingdom;
    String phylum;
    String taxonClass;
    String order;
    String family;
    String genus;
    String taxonRank;
    String furtherInformationURI;
    String taxonomicStatus;
    String taxonRemarks;
    String namePublishedIn;
    String referenceId;

    public Taxa(String identifier, String scientificName, String parentTaxonId, String kingdom, String phylum, String taxonClass, String order,
                String family, String genus, String taxonRank, String furtherInformationURI, String taxonomicStatus, String taxonRemarks,
                String namePublishedIn, String referenceId) {
        this.identifier = identifier;
        this.scientificName = scientificName;
        this.parentTaxonId = parentTaxonId;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxonClass = taxonClass;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.taxonRank = taxonRank;
        this.furtherInformationURI = furtherInformationURI;
        this.taxonomicStatus = taxonomicStatus;
        this.taxonRemarks = taxonRemarks;
        this.namePublishedIn = namePublishedIn;
        this.referenceId = referenceId;
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

    public String getTaxonClass() {
        return taxonClass;
    }

    public void setTaxonClass(String taxonClass) {
        this.taxonClass = taxonClass;
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
}
