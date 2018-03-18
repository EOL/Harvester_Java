package org.bibalex.eol.validator.rules;

import org.bibalex.eol.validator.ArchiveFileState;
import org.bibalex.eol.validator.ValidationResult;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.lang.reflect.Method;
import java.util.ArrayList;

//import org.eol.handlers.LogHandler;

/**
 * Class Encapsulating the validation rule details plus
 *
 */
public abstract class ValidationRule {
    public enum FailureTypes {
        ERROR, WARNING
    }

    protected String validationFunction;
    protected FailureTypes failureType;
    protected String failureMessage;
//    protected Logger logger;

    /**
     * Create a logger instance with the name of the class
     */
    public ValidationRule() {
//        this.logger = LogHandler.getLogger(ValidationRule.class.getName());
    }

    /**
     * Initialize the local variables and create logger instance
     *
     * @param function    validation function complete name ( class_name.function_name)
     * @param failMessage the message in case of validation rule failure
     * @param failType    the level of the validation function failure
     */
    public ValidationRule(String function, String failMessage, FailureTypes failType) {
        this.failureMessage = failMessage;
        this.validationFunction = function;
        this.failureType = failType;
//        this.logger = LogHandler.getLogger(ValidationRule.class.getName());
    }

    @Override
    public String toString() {
        return "FailureMessage : " + failureMessage + " , validationFunction : " + validationFunction + " , FailureType : " + failureType;
    }

    public String getValidationFunction() {
        return validationFunction;
    }

    public void setValidationFunction(String validationFunction) {
        this.validationFunction = validationFunction;
    }

    public FailureTypes getFailureType() {
        return failureType;
    }

    public void setFailureType(FailureTypes failureType) {
        this.failureType = failureType;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    /**
     * Invoke the validation function passing to it the appropriate arguments ( according to the
     * type of the validation rule ) , then it report the result in the ValidationResult object
     *
     * @param method           the dynamically loaded method
     * @param archiveFile       the input archiveFile
     * @param validationResult the object that will hold the validation result
     * @return false in case of failing in calling the validation function
     */
    protected abstract boolean callValidationFunction(Method method, ArchiveFile archiveFile, ValidationResult validationResult, ArrayList<Record> records);

    /**
     * Invoke the validation function passing to it the appropriate arguments ( according to the
     * type of the validation rule ) , then it report the result in the ValidationResult object
     *
     * @param method           the dynamically loaded method
     * @param dwcArchive       the input darwin core archive
     * @param validationResult the object that will hold the validation result
     * @return false in case of failing in calling the validation function
     */
    protected abstract boolean callValidationFunction(Method method, Archive dwcArchive, ValidationResult validationResult);
    /**
     * Dynamically load the validation function.
     *
     * @return Method object holding the validation function
     */
    protected abstract Method dynamicallyLoadMethod() throws ClassNotFoundException, NoSuchMethodException;

    /**
     * Apply the validation rule on the darwincore archive file, and put the result in the
     * validation result
     *
     * @param archiveFile       the input archiveFile
     * @param validationResult the object that should hold the validation result
     * @return false in case of failure in applying the rule
     */
    public boolean validate(ArchiveFile archiveFile, ValidationResult validationResult, ArrayList<Record> records) {
//        logger = LogHandler.getLogger(ValidationRule.class.getName());
        Method method;

        try {
            method = dynamicallyLoadMethod();
        } catch (ClassNotFoundException e) {
//            logger.fatal("ClassNotFoundException while trying to dynamically load class of method : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (SecurityException e) {
//            logger.fatal("SecurityException while trying to dynamically load method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (NoSuchMethodException e) {
//            logger.fatal("NoSuchMethodException while trying to dynamically load method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        }
        return callValidationFunction(method, archiveFile, validationResult, records);
    }

    /**
     * Apply the validation rule on the darwincore archive file, and put the result in the
     * validation result
     *
     * @param dwcArchive       the input Darwincore archive
     * @param validationResult the object that should hold the validation result
     * @return false in case of failure in applying the rule
     */
    public boolean validate(Archive dwcArchive, ValidationResult validationResult) {
//        logger = LogHandler.getLogger(ValidationRule.class.getName());
        Method method;

        try {
            method = dynamicallyLoadMethod();
        } catch (ClassNotFoundException e) {
//            logger.fatal("ClassNotFoundException while trying to dynamically load class of method : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (SecurityException e) {
//            logger.fatal("SecurityException while trying to dynamically load method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        } catch (NoSuchMethodException e) {
//            logger.fatal("NoSuchMethodException while trying to dynamically load method  : " + this.validationFunction);
//            logger.fatal(e);
            return false;
        }
        return callValidationFunction(method, dwcArchive, validationResult);
    }

    /**
     * Extract the rule result into the ValidationReult object
     *
     * @param result           ArchiveFileState that the dynamically loaded function has returned
     * @param validationResult the Object that the result of the validation should be added to
     */
    public void reportResult(String rowTypeURI, ArchiveFileState result, ValidationResult validationResult) {
        if (result.isAllLinesComplying()) {
//            logger.info("All lines are complying the rule : " + this.toString());
        } else if (result.isAllLinesViolating()) {
            if (failureType == FailureTypes.ERROR) {
                validationResult.addRowByRowError(rowTypeURI, this.failureMessage + " . " + " [ All lines violating rule ] ");
            } else if (failureType == FailureTypes.WARNING) {
                validationResult.addRowByRowWarning(rowTypeURI, this.failureMessage + " . " + " [ All lines violating rule ] ");
            }
        } else if (result.getLinesViolatingRule() > 0) {
            if (failureType == FailureTypes.ERROR) {
                validationResult.addRowByRowError(rowTypeURI, this.failureMessage + " . " + " [ Failed " + result.getLinesViolatingRule() + " from total : " + result.getTotalNumberOfLines()+" ] ");
            } else if (failureType == FailureTypes.WARNING) {
                validationResult.addRowByRowWarning(rowTypeURI, this.failureMessage + " . " + " [ Failed " + result.getLinesViolatingRule() + " from total : " + result.getTotalNumberOfLines()+" ] ");
            }
        }
    }

}
