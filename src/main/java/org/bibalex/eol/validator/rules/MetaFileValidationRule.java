package org.bibalex.eol.validator.rules;

import org.bibalex.eol.validator.ValidationResult;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


/**
 * MetaFileValidationRule encapsulate the data and actions of the validation rules that are applied on
 * the meta.xml file of the darwin core archive
 *
 */
public class MetaFileValidationRule extends ValidationRule {

    /**
     * MetaFileValidationRule constructor
     */
    public MetaFileValidationRule() {

    }

    protected String rowTypeURI;
    protected String fieldURI; // can be empty

    public String getRowTypeURI() {
        return rowTypeURI;
    }

    public void setRowTypeURI(String rowTypeURI) {
        this.rowTypeURI = rowTypeURI;
    }

    public String getFieldURI() {
        return fieldURI;
    }

    public void setFieldURI(String fieldURI) {
        this.fieldURI = fieldURI;
    }
    
    /**
     * Provide one line description for the rule
     *
     * @return description String
     */
    public String toString() {
        return super.toString() + ", rowtype : " + rowTypeURI + " , fieldURI : " + fieldURI;
    }

    protected boolean callValidationFunction(Method method, ArchiveFile archiveFile, ValidationResult validationResult, ArrayList<Record> records) {
        return false;
    }

    @Override
    protected boolean callValidationFunction(Method method, Archive dwca, ValidationResult validationResult) {
        try {
            boolean result = (Boolean) method.invoke(null, dwca, this);
            if (!result)
                validationResult.addStructuralError(this.failureMessage);
        } catch (IllegalArgumentException e) {
//            logger.fatal("IllegalArgumentException while trying to dynamically call method method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (IllegalAccessException e) {
//            logger.fatal("IllegalAccessException while trying to dynamically call method method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (InvocationTargetException e) {
//            logger.fatal("InvocationTargetException while trying to dynamically call method method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (Exception e) {
//            logger.fatal("Exception while trying to dynamically call method method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        }
        return true;
    }

    protected Method dynamicallyLoadMethod() throws ClassNotFoundException, NoSuchMethodException {
//        logger.info("Apply the validation function [ " + this.toString() + " ]");
        String className = this.validationFunction.substring(0, this.validationFunction.lastIndexOf("."));
        String methodName = this.validationFunction.substring(this.validationFunction.lastIndexOf(".") + 1);
//        logger.info("Dynamically loading the class : " + className + " , method : " + methodName);
        Class<?> myClass = Class.forName(className);
        return myClass.getMethod(methodName, Archive.class, MetaFileValidationRule.class);
    }
}