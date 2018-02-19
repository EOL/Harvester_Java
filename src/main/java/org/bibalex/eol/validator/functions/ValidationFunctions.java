package org.bibalex.eol.validator.functions;

import org.bibalex.eol.validator.handlers.DwcaHandler;
import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.utils.Constants;
import org.bibalex.eol.validator.ArchiveFileState;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

//import org.eol.handlers.LogHandler;

public class ValidationFunctions {
    private static int chunkSize = Constants.ChunkSize;
    private static Logger logger = Logger.getLogger(ValidationFunctions.class.getName());

    /**
     * Check whether ArchiveFile have field or not and remove record as it is error
     *
     * @param archiveFile the input archive file
     * @return ArchiveFileState  number of violating lines and total number of lines
     */
    public static ArchiveFileState checkArchiveFileHasField_FieldValidator_Error(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term fieldTerm = null;
        try {
            fieldTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            System.out.println("all lines violating");
            ArchiveFileState archiveFileState = new ArchiveFileState();
            archiveFileState.setAllLinesViolating(true);
            records.clear();
            return archiveFileState;
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (record.value(fieldTerm) == null || record.value(fieldTerm).length() <= 0) {

//                logger.debug("line violating a rule \"Does not have the field : " + fieldURI + " \"");

                //add the check
                System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                countFailedLines(record);
                i.remove();
                failures++;
            }
        }

        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * Check whether ArchiveFile have field or not and doesn't remove record as it is warning
     *
     * @param archiveFile the input archive file
     * @return ArchiveFileState  number of violating lines and total number of lines
     */
    public static ArchiveFileState checkArchiveFileHasField_FieldValidator_Warning(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term fieldTerm = null;
        try {
            fieldTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            System.out.println("all lines violating");
            ArchiveFileState archiveFileState = new ArchiveFileState();
            archiveFileState.setAllLinesViolating(true);
            return archiveFileState;
        }
        int failures = 0;
        int totalLines = records.size();

        for (Record record : records) {
            if (record.value(fieldTerm) == null || record.value(fieldTerm).length() <= 0) {

//                logger.debug("line violating a rule \"Does not have the field : " + fieldURI + " \"");

                //add the check
                System.out.println("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
                countFailedLines(record);
                failures++;
            }

        }
        return new ArchiveFileState(totalLines, failures);
    }

    private static void countFailedLines(Record record){
        System.out.println("COUNTTTTTTTTTTTTTTTTTTTT");
        System.out.println(record.rowType().qualifiedName());
        if(record.rowType() == CommonTerms.mediaTerm){
            System.out.println("WE ARE HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            MediaValidationFunctions.failedMedia.add(record.value(CommonTerms.identifierTerm));
        }else if(record.rowType() == CommonTerms.referenceTerm){
            ReferenceValidationFunctions.failedReferences.add(record.value(CommonTerms.referenceIDTerm));
        }else if(record.rowType() == CommonTerms.agentTerm){
            AgentValidationFunctions.failedAgents.add(record.value(CommonTerms.agentIDTerm));
        }else{
            TaxonValidationFunctions.failedTaxa.add(record.value(DwcTerm.taxonID));
        }
    }

    /**
     * Check if the languages syntax using standardized ISO 639 language codes and remove violating record
     *
     * @param archiveFile the input archive file
     * @return ArchiveFileState  number of violating lines and total number of lines
     */
    public static ArchiveFileState checkLanguageIsValid_FieldValidator_Error(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term languageTerm = null;
        try{
            languageTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        }catch (Exception e) {
            logger.warn("Language should use standardized ISO 639 language codes");
        }
        int failures = 0;

        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();

            if (record.value(languageTerm) == null || record.value(languageTerm).length() <= 0 || !record.value(languageTerm).matches("^[a-z]{2,3}(-[a-z]{2,5})?$")) {
//                logger.debug("line violating a rule \"Does not have the field : " + fieldURI + " \"");

                i.remove();
                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * Check if the languages syntax using standardized ISO 639 language codes and doesn't remove record
     *
     * @param archiveFile the input archive file
     * @return ArchiveFileState  number of violating lines and total number of lines
     */
    public static ArchiveFileState checkLanguageIsValid_FieldValidator_Warning(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term languageTerm = null;
        try{
            languageTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        }catch (Exception e) {
            logger.warn("Language should use standardized ISO 639 language codes");
        }
        int failures = 0;

        int totalLines = records.size();
        for (Record record : records) {

            if (record.value(languageTerm) == null || record.value(languageTerm).length() <= 0 || !record.value(languageTerm).matches("^[a-z]{2,3}(-[a-z]{2,5})?$")) {
//                logger.debug("line violating a rule \"Does not have the field : " + fieldURI + " \"");

                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * Checks if the term name is following the "UTF-8" encoding
     *
     * @param archiveFile
     * @param fieldURI
     * @return
     * @throws Exception
     */
    public static ArchiveFileState checkTermOfFieldURIisUTF8_FieldValidator(ArchiveFile archiveFile, String
            fieldURI, ArrayList<Record> records)
            throws Exception {
        Term term = null;
        try {
            term = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            return new ArchiveFileState(true);
        }
        int failures = 0;
        int totalLines = records.size();
        for (Record record : records) {
            if (record.value(term) == null || record.value(term).length() <= 0 ||
                    !isUTF8(record.value(term))) {
//                logger.debug(
//                        "line : " + record.toString() + " is violating a rule \"" +
//                                "Does not have a valid field : " + fieldURI + " = " + record.value(term) + " \"");
                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * Check if a string is valid UTF-8 or not
     *
     * @param string
     * @return boolean
     */
    public static boolean isUTF8(String string) {
        try {
            byte[] bytes = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
