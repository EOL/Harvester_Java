package org.bibalex.eol.parser.models;

public class Metadata {
    String traitId;
    int    resourceId;
    String measurementType;
    String measurementUnit;
    String statisticalMethod;
    String source;
    String measurementValue;
    String measurement;
    String literal;
    String lifestage;
    String sex;

    @Override
    public String toString() {
        return "Metadata{" +
                "traitId='" + traitId + '\'' +
                ", resourceId=" + resourceId +
                ", measurementType='" + measurementType + '\'' +
                ", measurementUnit='" + measurementUnit + '\'' +
                ", statisticalMethod='" + statisticalMethod + '\'' +
                ", source='" + source + '\'' +
                ", measurementValue='" + measurementValue + '\'' +
                ", measurement='" + measurement + '\'' +
                ", literal='" + literal + '\'' +
                ", lifestage='" + lifestage + '\'' +
                ", sex='" + sex + '\'' +
                '}';
    }

    public Metadata(int resourceId, String measurementType,
                    String measurementUnit, String statisticalMethod, String source, String measurementValue,
                    String measurement, String literal, String lifestage, String sex) {
        this.resourceId = resourceId;
        this.measurementType = measurementType;
        this.measurementUnit = measurementUnit;
        this.statisticalMethod = statisticalMethod;
        this.source = source;
        this.measurementValue = measurementValue;
        this.measurement = measurement;
        this.literal = literal;
        this.lifestage = lifestage;
        this.sex = sex;
    }

    public Metadata ()
    {}


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
}
