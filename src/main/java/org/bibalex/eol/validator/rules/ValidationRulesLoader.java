package org.bibalex.eol.validator.rules;

//import org.eol.handlers.LogHandler;
import org.bibalex.eol.validator.handlers.XMLHandler;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ValidationRulesLoader {
//    protected static Logger logger;
private static final Logger logger = Logger.getLogger(ValidationRulesLoader.class);
    private static List<String> rowTypeList = new ArrayList<String>();
    private static List<FieldValidationRule> fieldValidationRuleList = new
            ArrayList<FieldValidationRule>();
    private static List<RowValidationRule> rowValidationRuleList = new
            ArrayList<RowValidationRule>();
    private static List<MetaFileValidationRule> metaFileValidationRuleList = new
            ArrayList<MetaFileValidationRule>();

    /**
     * Construct a new Loader for the validation rules
     */
    public ValidationRulesLoader(String propertiesFile) {
//        logger = LogHandler.getLogger(ValidationRulesLoader.class.getName());
        XMLHandler.initializeHandler(propertiesFile, "validationRulesFile");
    }

    /**
     * Load the Field validation rules into a list
     *
     * @param ruleListNode List of nodes from the XML file
     * @param rowType      Row type e.g. Document
     */
    public static void loadFieldValidationRules(Node ruleListNode, String rowType) {
        NodeList fieldValidationRulesNodeList = ((Element) ruleListNode).getElementsByTagName
                ("FieldValidationRule");
        logger.info("\t\t" + "(FieldValidationRules = " + fieldValidationRulesNodeList.getLength
                () + ")");    //Just a separator
        for (int i = 0; i < fieldValidationRulesNodeList.getLength(); i++) {
            logger.info("--------------------Rule " + (i + 1) + "--------------------");
            Node fieldValidationRuleNode = fieldValidationRulesNodeList.item(i);
            if (fieldValidationRuleNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) fieldValidationRuleNode;

                String failureTypeString = eElement.getElementsByTagName("FailureType").item(0)
                        .getTextContent();
                ValidationRule.FailureTypes failureType = getError(failureTypeString);

                FieldValidationRule newFieldValidationRule = new FieldValidationRule(rowType,
                        eElement.getElementsByTagName("FieldUri").item(0).getTextContent(),
                        eElement.getElementsByTagName("ValidationFunction").item(0)
                                .getTextContent().trim(),
                        eElement.getElementsByTagName("FailureMessage").item(0).getTextContent(),
                        failureType);
                fieldValidationRuleList.add(newFieldValidationRule);

                logger.info("FieldUri = " + eElement.getElementsByTagName("FieldUri").item(0)
                        .getTextContent());
                logger.info("ValidationFunction = " + eElement.getElementsByTagName
                        ("ValidationFunction").item(0).getTextContent());
                logger.info("FailureMessage = " + eElement.getElementsByTagName("FailureMessage")
                        .item(0).getTextContent());
                logger.info("FailureType = " + failureTypeString);

            }
            logger.info("-----------------------------------------------");
        }
    }

    /**
     * Load the Field validation rules into a list
     *
     * @param ruleListNode List of nodes from the XML file
     * @param rowType      Row type e.g. Document
     */
    public static void loadRowValidationRules(Node ruleListNode, String rowType) {
        NodeList rowValidationRulesNodeList = ((Element) ruleListNode).getElementsByTagName
                ("RowValidationRule");
        logger.info("\t\t" + "(RowValidationRules = " + rowValidationRulesNodeList.getLength() +
                ")");    //Just a separator
        for (int i = 0; i < rowValidationRulesNodeList.getLength(); i++) {
            logger.info("--------------------Rule " + (i + 1) + "--------------------");
            Node rowValidationRuleNode = rowValidationRulesNodeList.item(i);
            if (rowValidationRuleNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) rowValidationRuleNode;

                String failureTypeString = eElement.getElementsByTagName("FailureType").item(0)
                        .getTextContent();
                ValidationRule.FailureTypes failureType = getError(failureTypeString);

                RowValidationRule newRowValidationRule = new RowValidationRule(rowType,
                        eElement.getElementsByTagName("ValidationFunction").item(0)
                                .getTextContent().trim(),
                        eElement.getElementsByTagName("FailureMessage").item(0).getTextContent(),
                        failureType);
                rowValidationRuleList.add(newRowValidationRule);

                logger.info("ValidationFunction = " + eElement.getElementsByTagName
                        ("ValidationFunction").item(0).getTextContent());
                logger.info("FailureType = " + eElement.getElementsByTagName("FailureType").item
                        (0).getTextContent());
                logger.info("FailureMessage = " + eElement.getElementsByTagName("FailureMessage")
                        .item(0).getTextContent());
            }
            logger.info("-----------------------------------------------");
        }

    }

    /**
     * Load the Meta validation rules into a list
     *
     * @param ruleListNode List of nodes from the XML file
     * @param rowType      Row type e.g. Document
     */
    public static void loadMetaFileValidationRules(Node ruleListNode, String rowType) {

    }


    /**
     * Load the failure type into the validationRule.FailureTypes
     *
     * @param failureTypeString Failure type string e.g error, warning
     */
    private static ValidationRule.FailureTypes getError(String failureTypeString) {
        ValidationRule.FailureTypes failureType = null;
        if (failureTypeString.equalsIgnoreCase("error"))
            failureType = ValidationRule.FailureTypes.ERROR;
        else if (failureTypeString.equalsIgnoreCase("warning"))
            failureType = ValidationRule.FailureTypes.WARNING;
        return failureType;
    }

    public static void main(String[] args) {
//        LogHandler.initializeHandler("configs.properties");
        ValidationRulesLoader rulesLoader = new ValidationRulesLoader("configs.properties");
        rulesLoader.loadValidationRules();

        System.out.println(rowTypeList.toString());

        for (FieldValidationRule item : fieldValidationRuleList) {

            System.out.println(item.getRowTypeURI());
            System.out.println(item.getFieldURI());
            System.out.println(item.getValidationFunction());
            System.out.println(item.getFailureMessage());
            System.out.println(item.getFailureType().toString());

        }
        String rowType = "http://eol.org/schema/media/Document";
        List<RowValidationRule> Frules = rulesLoader.getValidationRulesOfRowType(rowType);
        System.out.println("RULES =="+ Frules.size());

        List<FieldValidationRule> Rrules = rulesLoader.getValidationRulesOfFieldType(rowType);
        System.out.println("RULES =="+ Rrules.size());
    }

    /**
     * get the list of the rowtypesURIs that have validation rules
     *
     * @return rowTypeList  list of string  (rows,  Media, names)
     */
    public List<String> getRowTypeList() {
        return rowTypeList;
    }

    /**
     * Return the list of the validation rules that should be applied on rowTypeURI
     *
     * @param rowTypeURI e.g. rows of Media, names
     * @return List<FieldValidationRule> a list of the Field validation rules for the rowTypeURI
     */
    public List<FieldValidationRule> getValidationRulesOfFieldType(String rowTypeURI) {
        List<FieldValidationRule> fieldValidationRuleListOfRowType = new
                ArrayList<FieldValidationRule>();
        for (FieldValidationRule item : fieldValidationRuleList) {
            if (item.getRowTypeURI().equals(rowTypeURI)) {
                fieldValidationRuleListOfRowType.add(item);
            }
        }
        return fieldValidationRuleListOfRowType;
    }

    /**
     * Return the list of the validation rules that should be applied on rowTypeURI
     *
     * @param rowTypeURI e.g. rows of Media, names
     * @return List<RowValidationRule> a list of the Field validation rules for the rowTypeURI
     */
    public List<RowValidationRule> getValidationRulesOfRowType(String rowTypeURI) {
        List<RowValidationRule> rowValidationRuleListOfRowType = new ArrayList<RowValidationRule>();
        for (RowValidationRule item : rowValidationRuleList) {
            if (item.getRowTypeURI().equals(rowTypeURI)) {
                rowValidationRuleListOfRowType.add(item);
            }
        }
        return rowValidationRuleListOfRowType;
    }

    /**
     * get the list of the validation rules that should be applied on the meta.xml file
     *
     * @return List<MetaFileValidationRule> a list of the Field validation rules for the rowTypeURI
     */
    public List<MetaFileValidationRule> getMetaFileValidationRules() {
        //TODO Defrawy GET THE RULES AND IMPLEMENT THE LOADER
        return metaFileValidationRuleList;
    }

    /**
     * Load the validation rules into lists (Field, Row and Meta Validation rules)
     *
     * @return false in case in failing to load the validation rules, true in case of succes.Fs
     */
    public boolean loadValidationRules() {
        rowTypeList.clear();
        fieldValidationRuleList.clear();
        rowValidationRuleList.clear();

        XMLHandler xmlHandler = new XMLHandler();
        logger.info("============ STARTING GETTING VALIDATION RULES ==============");
        NodeList nList = xmlHandler.document.getElementsByTagName("RulesList");

        // Looping on rules lists for each row type
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node ruleListNode = nList.item(temp);
            Element ruleListNodeElement = (Element) ruleListNode;
            String rowType = ruleListNodeElement.getAttribute("RowType");
            logger.info("====== Row Type = " + rowType + " ======");
            rowTypeList.add(rowType);

            loadFieldValidationRules(ruleListNode, rowType);
            loadRowValidationRules(ruleListNode, rowType);
        }
        logger.info("============ FINISHED GETTING VALIDATION RULES ==============");
        return true;
    }
}




