package org.bibalex.eol.utils;

import org.apache.commons.io.FileUtils;
import org.bibalex.eol.validator.OwnDwcaWriter;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final int ChunkSize = 1000;
    public static final String INSERT = "I";
    public static final String UPDATE = "U";
    public static final String DELETE = "D";
    public static final String UNCHANGED = "N";
    public static final String SEPARATOR = "+";


    public static boolean copyContentOfArchiveFileToDisk(ArrayList<Record> records, ArchiveFile archiveFile) {
        System.out.println("debug " + archiveFile.getTitle() + " size " +records.size());
        Archive archive = archiveFile.getArchive();
        File backup_folder = new File(archive.getLocation().getPath() + "_valid");
        Term rowType = archiveFile.getRowType();
        List<ArchiveField> fieldsSorted = archiveFile.getFieldsSorted();
        ArrayList<Term> termsSorted = new ArrayList<Term>();
        for (ArchiveField archiveField : fieldsSorted) {
            termsSorted.add(archiveField.getTerm());
        }

        try {
            OwnDwcaWriter dwcaWriter = new OwnDwcaWriter(archive.getCore().getRowType() /*rowType*/, backup_folder);
            File backup_file = new File(backup_folder, archiveFile.getTitle());
            if (!backup_file.exists()) {
                backup_file.createNewFile();
            }
            for (Record record : records) {
                Map<Term, String> termStringMap = dwcaWriter.recordToMap(record, archiveFile);
//                System.out.println(record.id());
                if(record.id()!= null)
                    dwcaWriter.newRecord(record.id());
                else
                    dwcaWriter.newRecord("");

                dwcaWriter.addExtensionRecord(termsSorted, rowType, termStringMap, archiveFile.getTitle(),
                        archiveFile.getFieldsTerminatedBy(), archiveFile.getLinesTerminatedBy(), archiveFile.getEncoding());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void writeHeader(ArchiveFile archiveFile) {
        File fileToCopyIt = archiveFile.getLocationFile();

        Archive archive = archiveFile.getArchive();
        File backup_folder = new File(archive.getLocation().getPath() + "_valid");

        try {
            if (!backup_folder.exists())
                FileUtils.forceMkdir(backup_folder);
            File df = new File(backup_folder, archiveFile.getTitle());
            if (!df.exists()) {
                df.createNewFile();
            }
            OutputStream out = new FileOutputStream(df, true);

            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToCopyIt));
            String header = bufferedReader.readLine();
            header += archiveFile.getLinesTerminatedBy();
            out.write(header.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
