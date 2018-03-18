package org.bibalex.eol.validator.rules;

import org.bibalex.eol.validator.handlers.DwcaHandler;
import org.bibalex.eol.validator.ArchiveFileState;
import org.bibalex.eol.validator.ValidationResult;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * RowValidationRule encapsulate the data and actions of the validation rules that are applied on
 * a specific darwin core archive file row
 *
 */
public class RowValidationRule extends ValidationRule {

    /**
     * RowValidationRule constructor
     */
    public RowValidationRule() {

    }

    protected String rowTypeURI;

    public String getRowTypeURI() {
        return rowTypeURI;
    }

    public void setRowTypeURI(String rowTypeURI) {
        this.rowTypeURI = rowTypeURI;
    }

    /**
     * Provide one line description for the rule
     *
     * @return description String
     */
    public String toString() {
        return "Rowtype : " + rowTypeURI + " , " + super.toString();
    }

    /**
     * RowValidationRule constructor
     *
     * @param rowTypeURI  the rowType of the archive file that the rule will be applied
     * @param function    the complete name of the code validation function
     * @param failMessage the failure message
     * @param failType    the level of the failure
     */
    public RowValidationRule(String rowTypeURI, String function, String failMessage, FailureTypes failType) {
        super(function, failMessage, failType);
        this.rowTypeURI = rowTypeURI;
    }

    @Override
    protected Method dynamicallyLoadMethod() throws ClassNotFoundException, NoSuchMethodException {
//        logger.info("Apply the validation function [ " + this.toString() + " ]");
        String className = this.validationFunction.substring(0, this.validationFunction.lastIndexOf("."));
        String methodName = this.validationFunction.substring(this.validationFunction.lastIndexOf(".") + 1);
//        logger.info("Dynamically loading the class : " + className + " , method : " + methodName);
        Class<?> myClass = Class.forName(className);
        return myClass.getMethod(methodName, ArchiveFile.class, ArrayList.class);
    }

    @Override
    protected boolean callValidationFunction(Method method, ArchiveFile archiveFile, ValidationResult validationResult, ArrayList<Record> records) {

        try {
//            for (ArchiveFile archiveFile : archiveFiles) {
                ArchiveFileState result = (ArchiveFileState) method.invoke(null, archiveFile, records);
                reportResult(this.rowTypeURI, result, validationResult);
//            }
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

    protected boolean callValidationFunction(Method method, Archive dwcArchive, ValidationResult validationResult) {
        return false;
    }

}
