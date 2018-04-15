package org.bibalex.eol.parser.models;

public class MeasurementOrFact {
    String measurementId;
    String occurrenceId;
    String measurementOfTaxon;
    String associationId;
    String parentMeasurementId;
    String measurementType;
    String measurementValue;
    String unit;
    String accuracy;
    String statisticalMethod;
    String determinedDate;
    String determinedBy;
    String measurementMethod;
    String remarks;
    String source;
    String citation;
    String contributor;
    String referenceId;
    String deltaStatus;

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }

    public String getOccurrenceId() {
        return occurrenceId;
    }

    public void setOccurrenceId(String occurrenceId) {
        this.occurrenceId = occurrenceId;
    }

    public String getMeasurementOfTaxon() {
        return measurementOfTaxon;
    }

    public void setMeasurementOfTaxon(String measurementOfTaxon) {
        this.measurementOfTaxon = measurementOfTaxon;
    }

    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getParentMeasurementId() {
        return parentMeasurementId;
    }

    public void setParentMeasurementId(String parentMeasurementId) {
        this.parentMeasurementId = parentMeasurementId;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public String getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(String measurementValue) {
        this.measurementValue = measurementValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getStatisticalMethod() {
        return statisticalMethod;
    }

    public void setStatisticalMethod(String statisticalMethod) {
        this.statisticalMethod = statisticalMethod;
    }

    public String getDeterminedDate() {
        return determinedDate;
    }

    public void setDeterminedDate(String determinedDate) {
        this.determinedDate = determinedDate;
    }

    public String getDeterminedBy() {
        return determinedBy;
    }

    public void setDeterminedBy(String determinedBy) {
        this.determinedBy = determinedBy;
    }

    public String getMeasurementMethod() {
        return measurementMethod;
    }

    public void setMeasurementMethod(String measurementMethod) {
        this.measurementMethod = measurementMethod;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public MeasurementOrFact(String measurementId, String occurrenceId, String measurementOfTaxon, String associationId,
                             String parentMeasurementId, String measurementType, String measurementValue, String unit, String accuracy,
                             String statisticalMethod, String determinedDate, String determinedBy, String measurementMethod, String remarks,
                             String source, String citation, String contributor, String referenceId) {
        this.measurementId = measurementId;
        this.occurrenceId = occurrenceId;
        this.measurementOfTaxon = measurementOfTaxon;
        this.associationId = associationId;
        this.parentMeasurementId = parentMeasurementId;
        this.measurementType = measurementType;
        this.measurementValue = measurementValue;
        this.unit = unit;
        this.accuracy = accuracy;
        this.statisticalMethod = statisticalMethod;
        this.determinedDate = determinedDate;
        this.determinedBy = determinedBy;
        this.measurementMethod = measurementMethod;
        this.remarks = remarks;
        this.source = source;
        this.citation = citation;
        this.contributor = contributor;
        this.referenceId = referenceId;
    }
}
