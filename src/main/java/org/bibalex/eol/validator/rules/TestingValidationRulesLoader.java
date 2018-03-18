package org.bibalex.eol.validator.rules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestingValidationRulesLoader extends ValidationRulesLoader {
    public TestingValidationRulesLoader(String propertiesFile) {
        super(propertiesFile);

    }

    @Override
    public boolean loadValidationRules() {

        return true;
    }

    /**
     * get the list of the rowtypesURIs that have validation rules
     *
     * @return e.g. rows of Media, names
     */
    @Override
    public List<String> getRowTypeList() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("http://eol.org/schema/media/Document");
        list.add("http://rs.tdwg.org/dwc/terms/Taxon");
        list.add("http://eol.org/schema/reference/Reference");
        list.add("http://eol.org/schema/agent/Agent");
        list.add("http://rs.gbif.org/terms/1.0/VernacularName");
        list.add("http://rs.tdwg.org/dwc/terms/MeasurementOrFact");
        list.add("http://eol.org/schema/Association");
        list.add("http://rs.tdwg.org/dwc/terms/Occurrence");
        list.add("http://rs.tdwg.org/dwc/terms/Event");
        return list;
    }

    /**
     * Return the list of the validation rules that should be applied on rowTypeURI
     *
     * @param rowTypeURI e.g. rows of Media, names
     * @return
     */
    @Override
    public List<RowValidationRule> getValidationRulesOfRowType(String rowTypeURI) {
        List<RowValidationRule> list = new ArrayList<RowValidationRule>();
        String documentURI = "http://eol.org/schema/media/Document";
        String taxonURI = "http://rs.tdwg.org/dwc/terms/Taxon";
        String referenceURI = "http://eol.org/schema/reference/Reference";
        String agentURI = "http://eol.org/schema/agent/Agent";
        if (rowTypeURI.equalsIgnoreCase(documentURI)) {
            list.add(new RowValidationRule(documentURI, "org.bibalex.eol.dwca.validation.functions.MediaValidationFunctions.checkMediaHasURL_RowValidator", "Multimedia must have accessURIs", ValidationRule.FailureTypes.ERROR));
            list.add(new RowValidationRule(documentURI, "org.bibalex.eol.dwca.validation.functions.MediaValidationFunctions.checkTextHasDescription_RowValidator", "Text must have descriptions", ValidationRule.FailureTypes.ERROR));
            list.add(new RowValidationRule(documentURI, "org.bibalex.eol.dwca.validation.functions.MediaValidationFunctions.checkTextHasSubject_RowValidator", "Text must have subjects", ValidationRule.FailureTypes.ERROR));
        } else if (rowTypeURI.equalsIgnoreCase(taxonURI)) {
            list.add(new RowValidationRule(taxonURI, "org.bibalex.eol.dwca.validation.functions.TaxonValidationFunctions.checkTaxonHasValidIdentifier", "Taxa must have identifiers", ValidationRule.FailureTypes.ERROR));
            list.add(new RowValidationRule(taxonURI, "org.bibalex.eol.dwca.validation.functions.TaxonValidationFunctions.validatePresenceOfAnyName", "Taxa should contain a scientificName or minimally a kingdom, phylum, class, order, family or genus", ValidationRule.FailureTypes.WARNING));
        } else if (rowTypeURI.equalsIgnoreCase(referenceURI)) {
            list.add(new RowValidationRule(referenceURI, "org.bibalex.eol.dwca.validation.functions.ReferenceValidationFunctions.checkReferencesHasTitles", "References must minimally contain a full_reference or title", ValidationRule.FailureTypes.ERROR));
        } else if (rowTypeURI.equalsIgnoreCase(agentURI)) {
            list.add(new RowValidationRule(agentURI, "org.bibalex.eol.dwca.validation.functions.AgentValidationFunctions.checkAgentsHaveNames", "Agents must minimally contain a term_name, term_firstName or term_familyName", ValidationRule.FailureTypes.ERROR));
        }
        return list;
    }

    /**
     * Get the list of the validation rules that should be applied on the meta.xml file
     *
     * @return
     */
    @Override
    public List<MetaFileValidationRule> getMetaFileValidationRules() {
        List<MetaFileValidationRule> list = new ArrayList<MetaFileValidationRule>();
        String documentURI = "http://eol.org/schema/media/Document";
        String checkExistanceFunction = "org.bibalex.eol.dwca.validation.functions.MetaFileValidationFunctions.checkFieldExists";
        MetaFileValidationRule rule1 = new MetaFileValidationRule();
        rule1.setFailureMessage("core file should be of type taxon");
        rule1.setFailureType(ValidationRule.FailureTypes.ERROR);
        rule1.setRowTypeURI("http://rs.tdwg.org/dwc/terms/Taxon");
        rule1.setValidationFunction("org.bibalex.eol.dwca.validation.functions.MetaFileValidationFunctions.checkCoreIsTaxon");
//        list.add(rule1);

        MetaFileValidationRule rule2 = new MetaFileValidationRule();
        rule2.setRowTypeURI(documentURI);
        rule2.setFieldURI("http://purl.org/dc/terms/identifier");
        rule2.setFailureMessage("Media must have identifiers");
        rule2.setFailureType(ValidationRule.FailureTypes.ERROR);
        rule2.setValidationFunction(checkExistanceFunction);
        list.add(rule2);

        MetaFileValidationRule rule3 = new MetaFileValidationRule();
        rule3.setFailureMessage("Media must have taxonIDs");
        rule3.setRowTypeURI(documentURI);
        rule3.setFieldURI("http://rs.tdwg.org/dwc/terms/taxonID");
        rule3.setFailureType(ValidationRule.FailureTypes.ERROR);
        rule3.setValidationFunction(checkExistanceFunction);
        list.add(rule3);

        MetaFileValidationRule rule4 = new MetaFileValidationRule();
        rule4.setFailureMessage("Media must have DataType");
        rule4.setRowTypeURI(documentURI);
        rule4.setFieldURI("http://purl.org/dc/terms/type");
        rule4.setFailureType(ValidationRule.FailureTypes.ERROR);
        rule4.setValidationFunction(checkExistanceFunction);

        list.add(rule4);


        return list;
    }

    public List<FieldValidationRule> getValidationRulesOfFieldType(String rowTypeURI) {
        //TODO add the implementation here
        return new LinkedList<FieldValidationRule>();
    }
}
