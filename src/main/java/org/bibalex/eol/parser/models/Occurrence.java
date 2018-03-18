package org.bibalex.eol.parser.models;

public class Occurrence {
    String occurrenceId;
    String eventId;
    String institutionCode;
    String collectionCode;
    String catalogNumber;
    String sex;
    String lifeStage;
    String reproductiveCondition;
    String behavior;
    String establishmentMeans;
    String remarks;
    String countOfIndividuals;
    String preparations;
    String fieldNotes;
    String samplingProtocol;
    String samplingEffort;
    String recordedBy;
    String identifiedBy;
    String dateIdentified;
    String eventDate;
    String modifiedDate;
    String locality;
    String decimalLatitude;
    String decimalLongitude;
    String verbatimLatitude;
    String verbatimLongitude;
    String verbatimElevation;
    String deltaStatus;
    String action;

    public Occurrence(String occurrenceId, String eventId, String institutionCode, String collectionCode, String catalogNumber, String sex, String lifeStage,
                      String reproductiveCondition, String behavior, String establishmentMeans, String remarks, String countOfIndividuals,
                      String preparations, String fieldNotes, String samplingProtocol, String samplingEffort, String recordedBy, String identifiedBy,
                      String dateIdentified, String eventDate, String modifiedDate, String locality, String decimalLatitude, String decimalLongitude,
                      String verbatimLatitude, String verbatimLongitude, String verbatimElevation, String action) {
        this.occurrenceId = occurrenceId;
        this.eventId = eventId;
        this.institutionCode = institutionCode;
        this.collectionCode = collectionCode;

        this.catalogNumber = catalogNumber;
        this.sex = sex;
        this.lifeStage = lifeStage;
        this.reproductiveCondition = reproductiveCondition;
        this.behavior = behavior;
        this.establishmentMeans = establishmentMeans;
        this.remarks = remarks;
        this.countOfIndividuals = countOfIndividuals;
        this.preparations = preparations;
        this.fieldNotes = fieldNotes;
        this.samplingProtocol = samplingProtocol;
        this.samplingEffort = samplingEffort;
        this.recordedBy = recordedBy;
        this.identifiedBy = identifiedBy;
        this.dateIdentified = dateIdentified;
        this.eventDate = eventDate;
        this.modifiedDate = modifiedDate;
        this.locality = locality;
        this.decimalLatitude = decimalLatitude;
        this.decimalLongitude = decimalLongitude;
        this.verbatimLatitude = verbatimLatitude;
        this.verbatimLongitude = verbatimLongitude;
        this.verbatimElevation = verbatimElevation;
        this.action = action;
    }

    public String getOccurrenceId() {

        return occurrenceId;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public void setOccurrenceId(String occurrenceId) {
        this.occurrenceId = occurrenceId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getCollectionCode() {
        return collectionCode;
    }

    public void setCollectionCode(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    public String getReproductiveCondition() {
        return reproductiveCondition;
    }

    public void setReproductiveCondition(String reproductiveCondition) {
        this.reproductiveCondition = reproductiveCondition;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public String getEstablishmentMeans() {
        return establishmentMeans;
    }

    public void setEstablishmentMeans(String establishmentMeans) {
        this.establishmentMeans = establishmentMeans;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCountOfIndividuals() {
        return countOfIndividuals;
    }

    public void setCountOfIndividuals(String countOfIndividuals) {
        this.countOfIndividuals = countOfIndividuals;
    }

    public String getPreparations() {
        return preparations;
    }

    public void setPreparations(String preparations) {
        this.preparations = preparations;
    }

    public String getFieldNotes() {
        return fieldNotes;
    }

    public void setFieldNotes(String fieldNotes) {
        this.fieldNotes = fieldNotes;
    }

    public String getSamplingProtocol() {
        return samplingProtocol;
    }

    public void setSamplingProtocol(String samplingProtocol) {
        this.samplingProtocol = samplingProtocol;
    }

    public String getSamplingEffort() {
        return samplingEffort;
    }

    public void setSamplingEffort(String samplingEffort) {
        this.samplingEffort = samplingEffort;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public String getIdentifiedBy() {
        return identifiedBy;
    }

    public void setIdentifiedBy(String identifiedBy) {
        this.identifiedBy = identifiedBy;
    }

    public String getDateIdentified() {
        return dateIdentified;
    }

    public void setDateIdentified(String dateIdentified) {
        this.dateIdentified = dateIdentified;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getDecimalLatitude() {
        return decimalLatitude;
    }

    public void setDecimalLatitude(String decimalLatitude) {
        this.decimalLatitude = decimalLatitude;
    }

    public String getDecimalLongitude() {
        return decimalLongitude;
    }

    public void setDecimalLongitude(String decimalLongitude) {
        this.decimalLongitude = decimalLongitude;
    }

    public String getVerbatimLatitude() {
        return verbatimLatitude;
    }

    public void setVerbatimLatitude(String verbatimLatitude) {
        this.verbatimLatitude = verbatimLatitude;
    }

    public String getVerbatimLongitude() {
        return verbatimLongitude;
    }

    public void setVerbatimLongitude(String verbatimLongitude) {
        this.verbatimLongitude = verbatimLongitude;
    }

    public String getVerbatimElevation() {
        return verbatimElevation;
    }

    public void setVerbatimElevation(String verbatimElevation) {
        this.verbatimElevation = verbatimElevation;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
