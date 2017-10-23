package org.bibalex.eol.parser.models;

/**
 * Created by Amr Morad
 */
public class AncestryFormatNode {

    String taxonId;
    String scientificName;
    String parentNameUsageId;
    String kingdom;
    String phylum;
    String nodeClass;
    String order;
    String family;
    String genus;
    String rank;
    String status;

    public AncestryFormatNode(String taxonId, String scientificName, String parentNameUsageId, String kingdom,
                              String phylum, String nodeClass, String order, String family, String genus,
                              String rank, String status) {
        this.taxonId = taxonId;
        this.scientificName = scientificName;
        this.parentNameUsageId = parentNameUsageId;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.nodeClass = nodeClass;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.rank = rank;
        this.status = status;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getParentNameUsageId() {
        return parentNameUsageId;
    }

    public void setParentNameUsageId(String parentNameUsageId) {
        this.parentNameUsageId = parentNameUsageId;
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

    public String getNodeClass() {
        return nodeClass;
    }

    public void setNodeClass(String nodeClass) {
        this.nodeClass = nodeClass;
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

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
