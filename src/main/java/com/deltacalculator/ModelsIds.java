package com.deltacalculator;

import java.util.HashSet;

public class ModelsIds {
    private static ModelsIds modelsIds = null;
    private HashSet<String> referenceIds;
    private HashSet<String> agentIds;
    private HashSet<String> measurementIds;
    private HashSet<String> associationIds;
    private HashSet<String> occurrenceIds;
    private HashSet<String> mediaIds;
    private HashSet<String> vernacularIds;
    private HashSet<String> taxaIds;

    private ModelsIds() {
        referenceIds = new HashSet<>();
        agentIds = new HashSet<>();
        measurementIds = new HashSet<>();
        associationIds = new HashSet<>();
        mediaIds = new HashSet<>();
        occurrenceIds = new HashSet<>();
        vernacularIds = new HashSet<>();
        taxaIds = new HashSet<>();
    }

    public static ModelsIds getModelsIds() {
        if (modelsIds == null)
            modelsIds = new ModelsIds();
        return modelsIds;
    }

    public static void setModelsIds(ModelsIds modelsIds) {
        ModelsIds.modelsIds = modelsIds;
    }

    public HashSet<String> getReferenceIds() {
        return referenceIds;
    }

    public void addReferenceId(String referenceId) {
        referenceIds.add(referenceId);
    }

    public HashSet<String> getAgentIds() {
        return agentIds;
    }

    public void addAgentId(String agentId) {
        agentIds.add(agentId);
    }

    public HashSet<String> getMeasurementIds() {
        return measurementIds;
    }

    public void addMeasurementId(String measurementId) {
        measurementIds.add(measurementId);
    }

    public HashSet<String> getAssociationIds() {
        return associationIds;
    }

    public void addAssociationId(String associationId) {
        associationIds.add(associationId);
    }

    public HashSet<String> getOccurrenceIds() {
        return occurrenceIds;
    }

    public void addOccurrenceId(String occurrenceId) {
        occurrenceIds.add(occurrenceId);
    }

    public HashSet<String> getMediaIds() {
        return mediaIds;
    }

    public void addMediumId(String mediumId) {
        mediaIds.add(mediumId);
    }

    public HashSet<String> getVernacularIds() {
        return vernacularIds;
    }

    public void addVernacularId(String vernacularId) {
        vernacularIds.add(vernacularId);
    }

    public HashSet<String> getTaxaIds() {
        return taxaIds;
    }

    public void addTaxaId(String taxaId) {
        taxaIds.add(taxaId);
    }
}
