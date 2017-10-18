package org.bibalex.eol.parser.models;

import java.util.List;

public class Relation {
    List<Integer> pageIds;
    List<String> guids;
    String parentTaxonId;
    String kingdom;
    String phylum;
    String taxonClass;
    String order;
    String family;
    String genus;
    String referenceId;
    String scientificName;

    public Relation(List<Integer> pageIds, List<String> guids, String parentTaxonId,
                    String kingdom, String phylum, String taxonClass, String order, String family,
                    String genus, String referenceId) {
        this.pageIds = pageIds;
        this.guids = guids;
        this.parentTaxonId = parentTaxonId;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxonClass = taxonClass;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.referenceId = referenceId;
    }

    public Relation(String parentTaxonId, String kingdom, String phylum, String taxonClass,
                    String order, String family, String genus, String referenceId, String scirntificName) {
        this.parentTaxonId = parentTaxonId;
        this.kingdom = kingdom;
        this.phylum = phylum;
        this.taxonClass = taxonClass;
        this.order = order;
        this.family = family;
        this.genus = genus;
        this.referenceId = referenceId;
        this.scientificName = scirntificName;

    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public List<Integer> getPageIds() {
        return pageIds;
    }

    public void setPageIds(List<Integer> pageIds) {
        this.pageIds = pageIds;
    }

    public List<String> getGuids() {
        return guids;
    }

    public void setGuids(List<String> guids) {
        this.guids = guids;
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

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
