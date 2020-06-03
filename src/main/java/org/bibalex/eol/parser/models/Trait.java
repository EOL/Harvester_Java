package org.bibalex.eol.parser.models;

public class Trait {
    public static Integer traitCount = 0;
    String traitId;
    int resourceId;
    String taxonId;
    String bibliographicCitation;
    String measurementType;
    String measurementUnit;
    String normalizedMeasurementValue;
    String normalizedMeasurementUnit;
    String statisticalMethod;
    String source;
    String referenceId;
    String scientificName;
    String targetTaxonId;
    String targetScientificName;
    String measurementValue;
    String measurement;
    String literal;
    String lifestage;
    String sex;
    //TODO: add locations

    //TODO: constructors attributes will differ based on whether it is a measuremnet or association

    public String getTraitId() {
        return traitId;
    }

    public void setTraitId(String traitId) {
        this.traitId = traitId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getBibliographicCitation() {
        return bibliographicCitation;
    }

    public void setBibliographicCitation(String bibliographicCitation) {
        this.bibliographicCitation = bibliographicCitation;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(String measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public String getNormalizedMeasurementValue() {
        return normalizedMeasurementValue;
    }

    public void setNormalizedMeasurementValue(String normalizedMeasurementValue) {
        this.normalizedMeasurementValue = normalizedMeasurementValue;
    }

    public String getNormalizedMeasurementUnit() {
        return normalizedMeasurementUnit;
    }

    public void setNormalizedMeasurementUnit(String normalizedMeasurementUnit) {
        this.normalizedMeasurementUnit = normalizedMeasurementUnit;
    }

    public String getStatisticalMethod() {
        return statisticalMethod;
    }

    public void setStatisticalMethod(String statisticalMethod) {
        this.statisticalMethod = statisticalMethod;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getTargetTaxonId() {
        return targetTaxonId;
    }

    public void setTargetTaxonId(String targetTaxonId) {
        this.targetTaxonId = targetTaxonId;
    }

    public String getTargetScientificName() {
        return targetScientificName;
    }

    public void setTargetScientificName(String targetScientificName) {
        this.targetScientificName = targetScientificName;
    }

    public String getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(String measurementValue) {
        this.measurementValue = measurementValue;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public String getLifestage() {
        return lifestage;
    }

    public void setLifestage(String lifestage) {
        this.lifestage = lifestage;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Trait(String traitId, int resourceId, String taxonId, String bibliographicCitation, String measurementUnit, String normalizedMeasurementValue,
                 String normalizedMeasurementUnit, String statisticalMethod, String source, String referenceId,
                 String scientificName, String measurementValue, String measurement, String literal, String lifestage,
                 String sex)
    {
        this.traitId = traitId;
        this.resourceId = resourceId;
        this.taxonId = taxonId;
        this.bibliographicCitation = bibliographicCitation;
        this.measurementUnit = measurementUnit;
        this.normalizedMeasurementValue = normalizedMeasurementValue;
        this.normalizedMeasurementUnit = normalizedMeasurementUnit;
        this.statisticalMethod = statisticalMethod;
        this.source = source;
        this.referenceId = referenceId;
        this.scientificName = scientificName;
        this.measurementValue = measurementValue;
        this.measurement = measurement;
        this.literal = literal;
        this.lifestage = lifestage;
        this.sex = sex;

    }

    @Override
    public String toString() {
        return "Trait{" +
                "traitId='" + traitId + '\'' +
                ", resourceId=" + resourceId +
                ", taxonId='" + taxonId + '\'' +
                ", bibliographicCitation='" + bibliographicCitation + '\'' +
                ", measurementType='" + measurementType + '\'' +
                ", measurementUnit='" + measurementUnit + '\'' +
                ", normalizedMeasurementValue='" + normalizedMeasurementValue + '\'' +
                ", normalizedMeasurementUnit='" + normalizedMeasurementUnit + '\'' +
                ", statisticalMethod='" + statisticalMethod + '\'' +
                ", source='" + source + '\'' +
                ", referenceId='" + referenceId + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", targetTaxonId='" + targetTaxonId + '\'' +
                ", targetScientificName='" + targetScientificName + '\'' +
                ", measurementValue='" + measurementValue + '\'' +
                ", measurement='" + measurement + '\'' +
                ", literal='" + literal + '\'' +
                ", lifestage='" + lifestage + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }
}
