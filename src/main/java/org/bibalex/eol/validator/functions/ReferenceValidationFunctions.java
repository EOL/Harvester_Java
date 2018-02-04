package org.bibalex.eol.validator.functions;

import org.bibalex.eol.validator.handlers.DwcaHandler;
import org.bibalex.eol.validator.ArchiveFileState;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.util.ArrayList;

/**
 * Functions populated in ValidationRules.xml using ValidationFunctions.java
 * 1. Check whether identifier exists or not
 */

public class ReferenceValidationFunctions {
    public static final String ROW_TYPE = "http://eol.org/schema/reference/Reference";
    public static ArrayList<String> failedReferences = new ArrayList<String>();
    /**
     * Checking if any of the following fields exist:
     * http://eol.org/schema/reference/
     * http://eol.org/schema/reference/primaryTitle
     * http://purl.org/dc/terms/title'
     *
     * @param archiveFile
     * @return
     */
    public static ArchiveFileState checkReferencesHasTitles_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) {
        String[] termsString = {TermURIs.referenceURI, TermURIs.primaryTitleURI, TermURIs.titleURI};
        return DwcaHandler.checkRecordsHaveAtLeastOneOfTermsListError(archiveFile, termsString, TermURIs.referenceURI, records);
    }

}
