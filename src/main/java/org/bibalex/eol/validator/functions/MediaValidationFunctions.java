package org.bibalex.eol.validator.functions;

import org.bibalex.eol.validator.handlers.DwcaHandler;
import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.validator.ArchiveFileState;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *         <p/>
 *         Functions populated in ValidationRules.xml using ValidationFunctions.java
 *         <p/>
 *         . Checking accessURI if it is valid URL using the regex :/^(https?|ftp):\/\/.*\./
 *         . Checking thumbnailURL if it is valid URL using the regex :/^(https?|ftp):\/\/.*\./
 *         . Checking furtherInformationURL if it is valid URL using the regex :(https?|ftp):\/\/.*\./
 *         . Checks if the description is following the "UTF-8" encoding
 *         . Checks if the title is following the "UTF-8" encoding
 */
public class MediaValidationFunctions {
    public static final String ROW_TYPE = "http://eol.org/schema/media/Document";
//    private static Logger logger = LogHandler.getLogger(MediaValidationFunctions.class.getName());
    public static ArrayList<String> failedMedia = new ArrayList<String>();

    //====================================================================//

    /**
     * Checking the existence of URL and that it's Syntax is valid as an http or ftp link "/^(https?|ftp):\/\/.*\./i"
     *
     * @param archiveFile
     * @param fieldURI
     * @return ArchiveFileState
     */
    public static ArchiveFileState checkMediaHasValidURL_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records)
            throws
            Exception {
        Term urlTerm = null;
        try {
            urlTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            records.clear();
            return new ArchiveFileState(true);
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (record.value(urlTerm) == null || record.value(urlTerm).length() <= 0 ||
                    !record.value(urlTerm).matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")) {
                //TODO DEFRAWY regex format is weak according to sources online
//                logger.debug(
//                        "line : " + record.toString() + " is violating a rule \"" +
//                                "Does not have a valid field : " + fieldURI + " = " + record.value(urlTerm) + " \"");
                failedMedia.add(record.value(CommonTerms.identifierTerm));
                i.remove();
                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    //==================================================================//


    /**
     * if the value of the field "http://purl.org/dc/terms/type" equals one of the followings :
     * http://purl.org/dc/dcmitype/stillimage
     * http://purl.org/dc/dcmitype/movingimage
     * http://purl.org/dc/dcmitype/sound
     * and there is no "http://rs.tdwg.org/ac/terms/accessURI", the row is rejected.
     *
     * @param archiveFile input archive file
     * @return the number of lines violating the rules
     */
    public static ArchiveFileState checkMediaHasURL_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) throws Exception {
        Term typeTerm;
        Term accessTerm = null;
        try {
            typeTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.termTypeURI);
        } catch (Exception e) {
//            logger.error("type term is not found in the media archive file " + e);
            ArchiveFileState stateResult = new ArchiveFileState();
            stateResult.setAllLinesComplying(true);
            return stateResult;
        }

        boolean archiveFileHasAccessURI = true;
        try {
            accessTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.accessURI);
        } catch (Exception e) {
//            logger.error(e.getMessage());
            archiveFileHasAccessURI = false;
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (record.value(typeTerm).equalsIgnoreCase(TermURIs.stillImageURI)
                    || record.value(typeTerm).equalsIgnoreCase(TermURIs.movingImageURI)
                    || record.value(typeTerm).equalsIgnoreCase(TermURIs.soundURI)) {
                if (!archiveFileHasAccessURI || !DwcaHandler.recordHasTerm(accessTerm, record)) {
//                    logger.debug("Media Archive line without accessURI");
                    failedMedia.add(record.value(CommonTerms.identifierTerm));
                    i.remove();
                    failures++;
                }
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * If the value of the field "http://purl.org/dc/terms/type" equals "http://purl.org/dc/dcmitype/text"
     * ( i.e. the row is description ) and there is no field "http://purl.org/dc/terms/description" the row is rejected.
     *
     * @param archiveFile
     * @return
     */
    public static ArchiveFileState checkTextHasDescription_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) throws Exception {
        Term typeTerm;
        Term descriptionTerm = null;
        try {
            typeTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.termTypeURI);
        } catch (Exception e) {
//            logger.error("type term is not found in the media archive file " + e);
            ArchiveFileState stateResult = new ArchiveFileState();
            stateResult.setAllLinesComplying(true);
            return stateResult;
        }
        boolean archiveFileHasDescription = true;
        try {
            descriptionTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.descriptionURI);
        } catch (Exception e) {
//            logger.error(e.getMessage());
            archiveFileHasDescription = false;
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (record.value(typeTerm).equalsIgnoreCase(TermURIs.textURI)) {
                if (!archiveFileHasDescription || !DwcaHandler.recordHasTerm(descriptionTerm, record)) {
//                    logger.debug("Media Archive line without accessURI");
                    failedMedia.add(record.value(CommonTerms.identifierTerm));
                    i.remove();
                    failures++;
                }
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     * If the value of the field "http://purl.org/dc/terms/type" equals "http://purl.org/dc/dcmitype/text"
     * ( i.e. the row is description ) and there is no field "http://iptc.org/std/Iptc4xmpExt/1.0/xmlns/CVterm" the row is rejected.
     *
     * @param archiveFile
     * @return
     */
    public static ArchiveFileState checkTextHasSubject_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) throws Exception {
        Term typeTerm = null, descriptionTerm = null;
        try {
            typeTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.termTypeURI);
        } catch (Exception e) {
//            logger.error("type term is not found in the media archive file " + e);
            ArchiveFileState stateResult = new ArchiveFileState();
            stateResult.setAllLinesComplying(true);
            return stateResult;
        }
        boolean archiveFileHasCVTerm = true;
        try {
            descriptionTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, TermURIs.descriptionURI);
        } catch (Exception e) {
//            logger.error(e.getMessage());
            archiveFileHasCVTerm = false;
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (record.value(typeTerm).equalsIgnoreCase(TermURIs.cvTermURI)) {
                if (!archiveFileHasCVTerm || !DwcaHandler.recordHasTerm(descriptionTerm, record)) {
//                    logger.debug("Media Archive line without " + TermURIs.descriptionURI);
                    failedMedia.add(record.value(CommonTerms.identifierTerm));
                    i.remove();
                    failures++;
                }
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }


    /**
     * Check that the row should has at least one of the two field URIs
     * http://ns.adobe.com/xap/1.0/rights/UsageTerms
     * http://purl.org/dc/terms/license
     *
     * @param archiveFile input archive file
     * @return return the archiveFileState against the rule
     * @throws Exception in case of failure in applying the rule
     */
    public static ArchiveFileState checkLicenseExist_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) throws Exception {
        String[] termsString = {TermURIs.usageTermsURI, TermURIs.licenseURI};
        return DwcaHandler.checkRecordsHaveAtLeastOneOfTermsListError(archiveFile, termsString, TermURIs.mediaURI, records);
    }

    /**
     * check if the type is one of these URIs :
     * http://purl.org/dc/dcmitype/movingimage
     * http://purl.org/dc/dcmitype/sound
     * http://purl.org/dc/dcmitype/stillimage
     * http://purl.org/dc/dcmitype/text
     *
     * @param archiveFile input archive file
     * @param fieldURI
     * @return
     * @throws Exception
     */
    public static ArchiveFileState checkValidMediaType_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        String[] validValues = {TermURIs.movingImageURI, TermURIs.soundURI, TermURIs.stillImageURI, TermURIs.textURI};
        return DwcaHandler.checkFieldHasOneOfListOfValues(archiveFile, fieldURI, validValues, false, TermURIs.mediaURI, records);
    }

    /**
     * Validate the media subtype field, it is valid if not existing or existing with value : "map"
     *
     * @param archiveFile
     * @param fieldURI
     * @return
     * @throws Exception
     */
    public static ArchiveFileState checkValidMediaSubType_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        String[] validValues = {"map"};
        return DwcaHandler.checkFieldHasOneOfListOfValues(archiveFile, fieldURI, validValues, true, TermURIs.mediaURI, records);
    }


    /**
     * checks if the subject is valid ( has a valid prefix and a valid suffix )
     *
     * @param subject input subject
     * @return true if valid, false otherwise
     */
    protected static boolean isValidSubject(String subject) {
        String[] validPrefixes = {"http://rs.tdwg.org/ontology/voc/SPMInfoItems#", "http://rs.tdwg.org/pccore/",
                "http://eol.org/schema/eol_info_items.xml#", "http://www.eol.org/oc/table_of_contents#"};
        String[] validSuffixes = {"associations", "behaviour", "biology", "conservation", "conservationstatus",
                "cyclicity", "cytology", "description", "diagnosticdescription", "diseases", "dispersal",
                "distribution", "ecology", "evolution", "generaldescription", "genetics", "growth",
                "habitat", "key", "legislation", "lifecycle", "lifeexpectancy", "lookalikes", "management",
                "migration", "molecularbiology", "morphology", "physiology", "populationbiology",
                "procedures", "reproduction", "riskstatement", "size", "taxonbiology", "threats", "trends",
                "trophicstrategy", "uses", "use", "distribution", "abstract", "annualcycle", "behavior", "briefdescription",
                "chromosomicnumbern", "conservationstatuscites", "conservationstatusuicn",
                "distribution", "documenteduses", "ecologicalsignificance", "endemicity", "endemism",
                "feeding", "folklore", "habit", "habitat", "identificationkeys", "interactions",
                "invasivenessdata", "legislation", "lifecycle", "management",
                "migratorydata", "moleculardata", "nationallegislation", "otherinformationsources",
                "papers", "phenology", "population", "populationbiology", "populationstate",
                "publicationdate", "regionallegislation", "reproduction", "scientificdescription",
                "seasons", "speciespublicationreference", "symbioticrelationship",
                "targetaudiences", "territory", "threatstatus", "traditionaluses", "typecollector",
                "typedepository", "typelocation", "typification", "unstructureddocumentation",
                "unstructurednaturalhistory", "uses", "typeinformation", "education", "barcode", "wikipedia", "citizenscience",
                "educationresources", "genome", "nucleotidesequences", "gossilhistory",
                "systematicsorphylogenetics", "functionaladaptations", "development",
                "identificationresources", "notes", "taxonomy", "typeinformation",
                "taxonomy", "development"};

        String foundString = "";
        boolean foundPrefix = false;
        boolean foundSuffix = false;
        for (String prefix : validPrefixes) {
            if (subject.startsWith(prefix)) {
                foundString += prefix;
                foundPrefix = true;
                break;
            }
        }
        if (!foundPrefix)
            return false;
        for (String suffix : validSuffixes) {
            if (subject.endsWith(suffix)) {
                foundString += suffix;
                foundSuffix = true;
                break;
            }
        }
        if (!foundSuffix)
            return false;
        return foundString.equals(subject);
    }

    /**
     * checks if the subject of each line is a valid subject or not
     *
     * @param archiveFile
     * @param fieldURI
     * @return
     * @throws Exception
     */
    public static ArchiveFileState checkValidSubject_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term fieldTerm;
        try {
            fieldTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
//            logger.error(e.getMessage() + " - " + e);
//            logger.error("All lines do not have Term " + fieldURI + " so all lines is violating the rule");
            return new ArchiveFileState(true);
        }
        int violatingLines = 0;
        int totalLines = records.size();
        for (Record record : records) {
            String recordValue = record.value(fieldTerm);
            if (recordValue == null || recordValue.length() <= 0) {
                violatingLines++;
                failedMedia.add(record.value(CommonTerms.identifierTerm));
//                logger.debug("archiveFile " + archiveFile.getRowType().qualifiedName() + " line with null field value or value length equals zero");
                continue;
            }
            if (!isValidSubject(recordValue)) {
                violatingLines++;
                failedMedia.add(record.value(CommonTerms.identifierTerm));
//                logger.debug("Record of Media archive file has invalid subject : " + recordValue);
            }
        }
        return new ArchiveFileState(totalLines, violatingLines);
    }

    /**
     * check is the license is a valid license
     *
     * @param license input String
     * @return boolean result
     */
    protected static boolean isValidLicense(String license) {
        String[] validValues = {"http://creativecommons.org/publicdomain/zero/1.0/", "http://www.flickr.com/commons/usage/", "http://www.flickr.com/commons/usage/"
                , "no known copyright restrictions", "public domain", "not applicable"};
        if (license.matches("^http://creativecommons\\.org/licen(s|c)es/((by|by-nc|by-sa|by-nc-sa|publicdomain)/(1\\.0|2\\.0|2\\.5|3\\.0|4\\.0)|publicdomain)/$"))
            return true;
        for (String validValue : validValues)
            if (license.equalsIgnoreCase(validValue))
                return true;
        return false;
    }

    /**
     * Check that the fieldURI value is a valid license, if row does not have this fieldURI
     * it considered as not violating row
     * http://ns.adobe.com/xap/1.0/rights/UsageTerms
     * http://purl.org/dc/terms/license
     *
     * @param archiveFile input archive file
     * @return return the archiveFileState against the rule
     * @throws Exception in case of failure in applying the rule
     */
    public static ArchiveFileState checkValidLicense_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records) throws Exception {
        Term licenseTerm = null;
        try {
            licenseTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
//            logger.error("Error while getting " + fieldURI + " from archive file. error message : " + e.getMessage());
            ArchiveFileState state = new ArchiveFileState();
            state.setAllLinesComplying(true);
            return state;
        }

        if (licenseTerm == null) {
//            logger.error("Archive file does not have the term : " + fieldURI);
            ArchiveFileState state = new ArchiveFileState();
            state.setAllLinesComplying(true);
            return state;
        }
        int failures = 0;
        int totalLines = records.size();
        Iterator<Record> i = records.iterator();
        while (i.hasNext()) {
            Record record = i.next();
            if (DwcaHandler.recordHasTerm(licenseTerm, record) && !isValidLicense(record.value(licenseTerm))) {
                failedMedia.add(record.value(CommonTerms.identifierTerm));
                i.remove();
                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }


}
