package org.bibalex.eol.validator.functions;

import org.bibalex.eol.validator.handlers.DwcaHandler;
import org.bibalex.eol.validator.ArchiveFileState;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.util.ArrayList;
import java.util.Arrays;

//import org.eol.handlers.LogHandler;

/**
 * Functions populated in ValidationRules.xml using ValidationFunctions.java
 * 1.Check whether Taxa scientificNames exists or not
 * 2. Checks if the term name is following the "UTF-8" encoding
 */
public class TaxonValidationFunctions {
    public static final String ROW_TYPE = "http://rs.tdwg.org/dwc/terms/Taxon";
//    private static Logger logger = LogHandler.getLogger(TaxonValidationFunctions.class.getName());
    public static ArrayList<String> failedTaxa = new ArrayList<String>();

    /**
     * Check if the rank is one of these values :
     * "species", "superkingdom", "kingdom", "regnum", "subkingdom", "infrakingdom", "subregnum", "division",
     * "superphylum", "phylum", "divisio", "subdivision", "subphylum", "infraphylum", "parvphylum", "subdivisio",
     * "superclass", "class", "classis", "infraclass", "subclass", "subclassis", "superorder", "order", "ordo",
     * "infraorder", "suborder", "subordo", "superfamily", "family", "familia", "subfamily", "subfamilia", "tribe",
     * "tribus", "subtribe", "subtribus", "genus", "subgenus", "section", "sectio", "subsection", "subsectio", "series",
     * "subseries", "species", "subspecies", "infraspecies", "variety", "varietas", "subvariety", "subvarietas", "form",
     * "forma", "subform", "subforma"
     *
     * @param archiveFile
     * @param fieldURI
     * @return ArchiveFileState
     */
    public static ArchiveFileState checkTaxonHasValidRank_FieldValidator(ArchiveFile archiveFile, String fieldURI, ArrayList<Record> records)
            throws Exception {
        String[] ranks =
                {"species", "superkingdom", "kingdom", "regnum", "subkingdom", "infrakingdom", "subregnum", "division",
                        "superphylum", "phylum", "divisio", "subdivision", "subphylum", "infraphylum", "parvphylum",
                        "subdivisio", "superclass", "class", "classis", "infraclass", "subclass", "subclassis",
                        "superorder", "order", "ordo", "infraorder", "suborder", "subordo", "superfamily", "family",
                        "familia", "subfamily", "subfamilia", "tribe", "tribus", "subtribe", "subtribus", "genus",
                        "subgenus", "section", "sectio", "subsection", "subsectio", "series", "subseries", "species",
                        "subspecies", "infraspecies", "variety", "varietas", "subvariety", "subvarietas", "form",
                        "forma", "subform", "subforma"};
        Term rankTerm = null;
        try {
            rankTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            return new ArchiveFileState(true);
        }
        int failures = 0;
        int totalLines = records.size();
        for (Record record : records) {
            if (record.value(rankTerm) == null || record.value(rankTerm).length() <= 0 ||
                    !Arrays.asList(ranks).contains(record.value(rankTerm).toLowerCase())) {
//                logger.debug(
//                        "line : " + record.toString() + " is violating a rule \"" +
//                                "Does not have a valid field : " + fieldURI + " = " + record.value(rankTerm) + " \"");
                failedTaxa.add(record.value(DwcTerm.taxonID));
                failures++;
            }
        }
        return new ArchiveFileState(totalLines, failures);
    }

    /**
     *
     * Checks if the scientific name is following the "UTF-8" encoding
     */
    public static ArchiveFileState checkTaxonHasValidScientificName_FieldValidator(ArchiveFile archiveFile, String
            fieldURI)
            throws Exception {
        Term scientificNameTerm = null;
        try {
            scientificNameTerm = DwcaHandler.getTermFromArchiveFile(archiveFile, fieldURI);
        } catch (Exception e) {
            return new ArchiveFileState(true);
        }
        int failures = 0;
        int totalLines = 0;
        for (Record record : archiveFile) {
            if (record.value(scientificNameTerm) == null || record.value(scientificNameTerm).length() <= 0 ||
                   !ValidationFunctions.isUTF8(record.value(scientificNameTerm))) {
//                logger.debug(
//                        "line : " + record.toString() + " is violating a rule \"" +
//                                "Does not have a valid field : " + fieldURI + " = " + record.value(scientificNameTerm) + " \"");
                failedTaxa.add(record.value(DwcTerm.taxonID));
                failures++;
            }
            totalLines++;
        }
        return new ArchiveFileState(totalLines, failures);
    }


    /**
     * The row should has at least one of the following fields :
     * http://rs.tdwg.org/dwc/terms/taxonID
     * http://purl.org/dc/terms/identifier
     *
     * @param archiveFile
     * @return
     */
    public static ArchiveFileState checkTaxonHasValidIdentifier_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) {
        String[] termsString = {TermURIs.taxonID_URI, TermURIs.identifierURI};
        return DwcaHandler.checkRecordsHaveAtLeastOneOfTermsListError(archiveFile, termsString, TermURIs.taxonID_URI, records); //other URI
    }

    /**
     * The row should has at least one of the following fields :
     * "http://rs.tdwg.org/dwc/terms/scientificName",
     * "http://rs.tdwg.org/dwc/terms/kingdom",
     * "http://rs.tdwg.org/dwc/terms/phylum",
     * "http://rs.tdwg.org/dwc/terms/class",
     * "http://rs.tdwg.org/dwc/terms/order",
     * "http://rs.tdwg.org/dwc/terms/family",
     * "http://rs.tdwg.org/dwc/terms/genus"
     *
     * @param archiveFile
     * @return
     */
    public static ArchiveFileState validatePresenceOfAnyName_RowValidator(ArchiveFile archiveFile, ArrayList<Record> records) {
        String[] termsString = {TermURIs.kingdomURI, TermURIs.classURI, TermURIs.orderURI,
                TermURIs.familyURI, TermURIs.genusURI};
        return DwcaHandler.checkRecordsHaveAtLeastOneOfTermsListWarning(archiveFile, termsString, TermURIs.taxonID_URI, records);
    }




}
