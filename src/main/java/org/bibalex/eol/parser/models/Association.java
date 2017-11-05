package org.bibalex.eol.parser.models;

public class Association {
    String associationId;
    String occurrenceId;
    String associationType;
    String targetOccurrenceId;
    String determinedDate;
    String determinedBy;
    String measurementMethod;
    String remarks;
    String source;
    String citation;
    String contributor;
    String referenceId;
    String deltaStatus;

    public Association(String associationId, String occurrenceId, String associationType, String targetOccurrenceId, String determinedDate,
                       String determinedBy, String measurementMethod, String remarks, String source, String citation, String contributor, String referenceId) {
        this.associationId = associationId;
        this.occurrenceId = occurrenceId;
        this.associationType = associationType;
        this.targetOccurrenceId = targetOccurrenceId;
        this.determinedDate = determinedDate;
        this.determinedBy = determinedBy;
        this.measurementMethod = measurementMethod;
        this.remarks = remarks;
        this.source = source;
        this.citation = citation;
        this.contributor = contributor;
        this.referenceId = referenceId;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public String getAssociationId() {

        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getOccurrenceId() {
        return occurrenceId;
    }

    public void setOccurrenceId(String occurrenceId) {
        this.occurrenceId = occurrenceId;
    }

    public String getAssociationType() {
        return associationType;
    }

    public void setAssociationType(String associationType) {
        this.associationType = associationType;
    }

    public String getTargetOccurrenceId() {
        return targetOccurrenceId;
    }

    public void setTargetOccurrenceId(String targetOccurrenceId) {
        this.targetOccurrenceId = targetOccurrenceId;
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
}
