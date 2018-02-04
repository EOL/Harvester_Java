package org.bibalex.eol.validator.functions;

import java.util.ArrayList;

//import org.eol.handlers.LogHandler;

/**
 * Functions populated in ValidationRules.xml using ValidationFunctions.java
 * 1. Check whether measurementTypes exists or not
 * 2. Check whether identifier measurementValues exists or not
 */
public class MeasurementOrFactValidationFunctions {
    public static final String ROW_TYPE = "http://eol.org/schema/Association";
//    private static Logger logger = LogHandler.getLogger(AssociationValidationFunctions.class.getName());
    public static ArrayList<String> failedMeasurements = new ArrayList<String>();

}
