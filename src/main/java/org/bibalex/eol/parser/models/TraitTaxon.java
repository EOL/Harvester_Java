package org.bibalex.eol.parser.models;

public class TraitTaxon {
    private String taxonId;
    private String scientificName;

    public TraitTaxon(String taxonId, String scientificName) {
        this.taxonId = taxonId;
        this.scientificName = scientificName;
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
}
