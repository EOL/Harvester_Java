package org.bibalex.eol.validator;
/*
 * this a copy of DwcaWriter
 * we need to change it to better solution*/

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.*;
import org.gbif.dwca.record.Record;
import org.gbif.io.TabWriter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Simple writer class to create valid dwc archives using tab data files.
 * The meta.xml descriptor is generated automatically and an optional EML metadata document can be added.
 * The archive is NOT compressed but the final product is a directory with all the necessary files.
 * For usage of this class please @see DwcaWriterTest.
 */
public class OwnDwcaWriter {
    private Logger log = Logger.getLogger(OwnDwcaWriter.class);
    private final File dir;
    private final boolean useHeaders;
    private long recordNum;
    private String coreId;
    private Map<Term, String> coreRow;
    private final Term coreRowType;
    private final Term coreIdTerm;
    private final Map<Term, TabWriter> writers = Maps.newHashMap();
    private final Map<String, TabWriter> writers_files = Maps.newHashMap();
    private final Set<Term> headersOut = Sets.newHashSet();
    private final Map<Term, String> dataFileNames = Maps.newHashMap();
    // key=rowType, value=columns
    private final Map<Term, List<Term>> terms = Maps.newHashMap();
    private final Map<String, List<Term>> terms_files = Maps.newHashMap();
    // key=rowType, value=default values per column
    private Writer writer;
    private int it=0;

    /**
     * Creates a new writer without header rows.
     * @param coreRowType the core row type.
     * @param dir         the directory to create the archive in.
     */
    public OwnDwcaWriter(Term coreRowType, File dir) throws IOException {
        this(coreRowType, dir, false);
    }

    /**
     * If headers are used the first record must include all terms ever used for that file.
     * If in subsequent rows additional terms are introduced an IllegalArgumentException is thrown.
     *
     * @param coreRowType    the core row type
     * @param dir            the directory to create the archive in
     * @param useHeaders if true the first row in every data file will include headers
     */
    public OwnDwcaWriter(Term coreRowType, File dir, boolean useHeaders) throws IOException {
        this(coreRowType, null, dir, useHeaders);
    }

    /**
     * If headers are used the first record must include all terms ever used for that file.
     * If in subsequent rows additional terms are introduced an IllegalArgumentException is thrown.
     *
     * @param coreRowType the core row type
     * @param coreIdTerm the term of the id column
     * @param dir the directory to create the archive in
     * @param useHeaders if true the first row in every data file will include headers
     */
    public OwnDwcaWriter(Term coreRowType, Term coreIdTerm, File dir, boolean useHeaders) throws IOException {
        this.dir = dir;
        this.coreRowType = coreRowType;
        this.coreIdTerm = coreIdTerm;
        this.useHeaders = useHeaders;
//        addRowType(coreRowType);
    }

    private void addRowType(Term rowType) throws IOException {
        terms.put(rowType, new ArrayList<Term>());

        String dfn = dataFileName(rowType);
        dataFileNames.put(rowType, dfn);
        File df = new File(dir, dfn);
        if (!df.getParentFile().exists()){
            FileUtils.forceMkdir(df.getParentFile());
        }
        OutputStream out = new FileOutputStream(df);
        TabWriter wr = new TabWriter(out);
        writers.put(rowType, wr);
    }

    public static String dataFileName(Term rowType) {
        return rowType.simpleName().toLowerCase() + ".txt";
    }


    public static Map<Term, String> recordToMap(Record rec, ArchiveFile af) {
        Map<Term, String> map = new HashMap<Term, String>();
        for (Term t : af.getTerms()) {
            map.put(t, rec.value(t));
        }
        return map;
    }


    /**
     * A new core record is started and the last core and all extension records are written.
     * @param id the new records id
     * @throws IOException
     */
    public void newRecord(String id) throws IOException {
        // flush last record
        flushLastCoreRecord();
        // start new
        recordNum++;
        coreId = id;
        coreRow = new HashMap<Term, String>();
    }

    private void flushLastCoreRecord() throws IOException {
        if (coreRow != null) {
//            writeRow(coreRow, coreRowType);
        }
    }

    private void writeRow(Map<Term, String> rowMap, Term rowType) throws IOException {
        TabWriter writer = writers.get(rowType);
        List<Term> columns = terms.get(rowType);
        if (useHeaders && !headersOut.contains(rowType)){
            // write header row
            writeHeader(writer, rowType, columns);
        }

        // make sure coreId is not null for extensions
        if (coreRowType != rowType && coreId == null){
            log.warn("Adding an {} extension record to a core without an Id! Skip this record", (Throwable) rowType);

        } else {
            String[] row = new String[columns.size() + 1];
            row[0] = coreId;
            for (Map.Entry<Term, String> conceptTermStringEntry : rowMap.entrySet()) {
                int column = 1 + columns.indexOf(conceptTermStringEntry.getKey());
                row[column] = conceptTermStringEntry.getValue();
            }
            writer.write(row);
        }
    }


    /**
     * Add an extension record associated with the current core record.
     *
     * @param rowType
     * @param row
     * @throws IOException
     */
    public void addExtensionRecord(ArrayList<Term> termsSorted, Term rowType, Map<Term, String> row,
                                   String nameOfDataFile, String fieldsTerminatedBy, String linesTerminatedBy, String encoding ) throws IOException {
        it++;
        // make sure we know the extension rowtype
        if (!terms_files.containsKey(nameOfDataFile)) {
            addRowType(rowType, nameOfDataFile, encoding);
        }

        // make sure we know all terms
        List<Term> knownTerms = terms_files.get(nameOfDataFile);
        final boolean isFirst = knownTerms.isEmpty();
        for (Term term : termsSorted) {
            if (!knownTerms.contains(term)) {
                if (useHeaders && !isFirst){
                    throw new IllegalStateException("You cannot add new terms after the first row when headers are enabled");
                }
                knownTerms.add(term);
            }
        }

        // write extension record
        writeRow(row, rowType, nameOfDataFile, fieldsTerminatedBy, linesTerminatedBy );
    }

    private void addRowType(Term rowType, String nameOfDataFile, String encoding) throws IOException {
        terms_files.put(nameOfDataFile, new ArrayList<Term>());

        String dfn = nameOfDataFile;
        dataFileNames.put(rowType, dfn);
        File df = new File(dir, dfn);
        FileUtils.forceMkdir(df.getParentFile());
        OutputStream out = new FileOutputStream(df, true);
        writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
        TabWriter wr = new TabWriter(out);
        writers_files.put(nameOfDataFile, wr);
    }

    private void writeRow(Map<Term, String> rowMap, Term rowType, String nameOfDataFile, String fieldsTerminatedBy, String linesTerminatedBy) throws IOException {
        TabWriter writer = writers_files.get(nameOfDataFile);
        List<Term> columns = terms_files.get(nameOfDataFile);
        if (useHeaders && !headersOut.contains(rowType)){
            // write header row
            writeHeader(writer, rowType, columns);
        }

        // make sure coreId is not null for extensions
        if (coreRowType != rowType && coreId == null){
            log.warn("Adding an {} extension record to a core without an Id! Skip this record", (Throwable) rowType);

        } else {
            String[] row = new String[columns.size()];
//            row[0] = coreId;
            for (Map.Entry<Term, String> conceptTermStringEntry : rowMap.entrySet()) {
                int column = columns.indexOf(conceptTermStringEntry.getKey());
                row[column] = conceptTermStringEntry.getValue();
            }
            write(row, fieldsTerminatedBy, linesTerminatedBy);
        }
    }

    private void writeHeader(TabWriter writer, Term rowType, List<Term> columns) throws IOException {
        int idx = 0;
        String[] row = new String[columns.size() + 1];
        Term idTerm;
        if (DwcTerm.Taxon == coreRowType){
            idTerm = DwcTerm.taxonID;
        } else if (DwcTerm.Occurrence == coreRowType){
            idTerm = DwcTerm.occurrenceID;
        } else if (DwcTerm.Identification == coreRowType){
            idTerm = DwcTerm.identificationID;
        } else if (DwcTerm.Event == coreRowType){
            idTerm = DwcTerm.eventID;
        } else {
            // default to generic dc identifier for id column
            idTerm = DcTerm.identifier;
        }
        row[idx] = idTerm.simpleName();

        for (Term term : columns) {
            idx ++;
            row[idx] = term.simpleName();
        }
        writer.write(row);

        headersOut.add(rowType);
    }

    /* to write files without need to taps */

    public void write(String[] row, String fieldsTerminatedBy, String linesTerminatedBy) throws IOException {
        if (row != null && row.length != 0) {
            String rowString = "";
            for (int i=0; i<row.length;i++){
                if (row[i] != null) {
                    if (row[i].contains(fieldsTerminatedBy) || row[i].contains(linesTerminatedBy)) {
                        rowString += "\"" + row[i] + "\"" + fieldsTerminatedBy;
                    } else {
                        rowString += row[i] + fieldsTerminatedBy;
                    }
                }
                else{
                    rowString += fieldsTerminatedBy;
                }
            }
            if (rowString != null) {
                rowString = StringUtils.removeEnd(rowString, fieldsTerminatedBy);
                rowString += linesTerminatedBy;
                writer.write(rowString);
                writer.flush();
            }

        }
    }

}