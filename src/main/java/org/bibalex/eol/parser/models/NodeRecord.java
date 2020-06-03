package org.bibalex.eol.parser.models;

import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;

import java.util.ArrayList;
import java.util.Map;

/*
This class will include the needed objects and attributes
 */
public class NodeRecord {
    int resourceId;
    ArrayList<VernacularName> vernaculars;
    //ArrayList<Reference> references;
    //ArrayList<Occurrence> occurrences;
    //ArrayList<Association> associations;
    //ArrayList<MeasurementOrFact> measurementOrFacts;
    // ArrayList<Media> media;
    //Map<String, String> targetOccurrences;
    String _id;
    Taxon taxon;
    String deltaStatus;

    public NodeRecord(String generatedNodeId, int resourceId) {
        this._id = generatedNodeId;
        this.resourceId = resourceId;
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

//    public ArrayList<Association> getAssociations() {
//        return associations;
//    }

    public Taxon getTaxon() {
        return taxon;
    }

    public void setTaxon(Taxon taxon) {
        this.taxon = taxon;
    }

//    public void setAssociations(ArrayList<Association> associations) {
//        this.associations = associations;
//    }
//
//    public ArrayList<MeasurementOrFact> getMeasurementOrFacts() {
//        return measurementOrFacts;
//    }
//
//    public void setMeasurementOrFacts(ArrayList<MeasurementOrFact> measurementOrFacts) {
//        this.measurementOrFacts = measurementOrFacts;
//    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

//    public String getTaxonId() {
//        return taxonId;
//    }
//
//    public void setTaxonId(String taxonId) {
//        this.taxonId = taxonId;
//    }

//    public ArrayList<Media> getMedia() {
//        return media;
//    }
//
//    public void setMedia(ArrayList<Media> media) {
//        this.media = media;
//    }
//
//    public Map<String, String> getTargetOccurrences() {
//        return targetOccurrences;
//    }
//
//    public void setTargetOccurrences(Map<String, String> targetOccurrences) {
//        this.targetOccurrences = targetOccurrences;
//    }
//
//    public ArrayList<Occurrence> getOccurrences() {
//        return occurrences;
//    }
//
//    public void setOccurrences(ArrayList<Occurrence> occurrences) {
//        this.occurrences = occurrences;
//    }
//
//    public ArrayList<Reference> getReferences() {
//        return references;
//    }
//
//    public void setReferences(ArrayList<Reference> references) {
//        this.references = references;
//    }

    public ArrayList<VernacularName> getVernaculars() {
        return vernaculars;
    }

    public void setVernaculars(ArrayList<VernacularName> vernaculars) {
        this.vernaculars = vernaculars;
    }

}
