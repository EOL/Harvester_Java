package org.bibalex.eol.validator.functions;

import java.util.ArrayList;


/**
 *
 * Functions populated in ValidationRules.xml using ValidationFunctions.java
 * 1.Check whether occurrenceIDs exists or not
 * 2.Check whether identifier associationTypes exists or not
 * 3.Check whether identifier targetOccurrenceIDs exists or not
*/
public class AssociationValidationFunctions {
    public static final String ROW_TYPE = "http://eol.org/schema/Association";
//    private static Logger logger = LogHandler.getLogger(AssociationValidationFunctions.class.getName());
    public static ArrayList<String> failedAsspciations = new ArrayList<String>();

}



