package com.deltacalculator;

import org.apache.log4j.Logger;
import org.gbif.dwca.io.Archive;

import javax.validation.constraints.Null;
import java.io.*;
import java.util.Date;

public class DeltaCalculator {
    static File DWCADiff = new File("/home/ba/eol_workspace/originals/DifferenceArchive_" + new Date().getTime());

    public DeltaCalculator() {
    }

    private static final Logger logger = Logger.getLogger(DeltaCalculator.class);

    public static void main(String[] args) {
      File file1 = new File ("/home/ba/eol_workspace/originals/421_org.out_valid.zip");
      File file2 = new File ("/home/ba/eol_workspace/originals/421_org.out_valid.zip");
      DeltaCalculator deltaCalculator = new DeltaCalculator();
      deltaCalculator.deltaCalculatorMain(file1, file2);
    }
    public String deltaCalculatorMain (File oldVersionFile, File updatedVersionFile){
        ModelsIds.setModelsIds(null);
        DeltaCalculator deltaCalculator = new DeltaCalculator();
        ArchiveHandler archiveHandler = new ArchiveHandler();
        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();
        CommandExecutor commandExecutor = new CommandExecutor();
        if (!DWCADiff.exists())
            DWCADiff.mkdir();
        String archive1Path = oldVersionFile.getPath(),
                archive2Path = updatedVersionFile.getPath();
        try {
            Archive dwca1 = archiveHandler.openDwcAFolder(archive1Path), dwca2 = archiveHandler.openDwcAFolder(archive2Path);
            System.out.println(dwca1.getMetadataLocation());
            System.out.println(dwca2.getMetadataLocation());
            logger.info("Version 1 Meta File: " + dwca1.getMetadataLocation());
            logger.info("Version 2 Meta File: " + dwca2.getMetadataLocation());
            File metaFile = new File(dwca2.getMetadataLocation());
            if (!dwca2.getMetadataLocation().equals(null)) {
                logger.info("Meta File Found - Sending File: " + metaFile.getPath());
                archiveFileHandler.copyMetaFile(metaFile, DWCADiff.getPath());
            }

            archiveHandler.filterContent(dwca1, dwca2);
            commandExecutor.removeDirectory(dwca1.getLocation().getPath());
            commandExecutor.removeDirectory(dwca2.getLocation().getPath());
            Archive updatedArchive = archiveHandler.openDwcAFolder(archive2Path);
            Archive oldArchive = archiveHandler.openDwcAFolder(archive1Path);
            deltaCalculator.adjustUnchangedRecords(updatedArchive, oldArchive);
            commandExecutor.removeDirectory(dwca1.getLocation().getPath());
            commandExecutor.removeDirectory(dwca2.getLocation().getPath());
            commandExecutor.compress(DWCADiff);
            commandExecutor.removeDirectory(DWCADiff.getPath());
            return DWCADiff.getPath()+".tar.gz";
        } catch (NullPointerException e) {
            logger.info(e);
            System.out.println(e);
            return "";
        }

    }

    private void adjustUnchangedRecords(Archive updatedArchive, Archive oldArchive) {
        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();
        archiveFileHandler.setMediaOfChangedAgents(updatedArchive);
        archiveFileHandler.setMediaOfChangedReference(updatedArchive);

        archiveFileHandler.setMeasurementOfChangedReference(updatedArchive);
        archiveFileHandler.setAssociationOfChangedReference(updatedArchive);

        archiveFileHandler.setOccurrenceOfChangedMeasurements(updatedArchive);
        archiveFileHandler.setOccurrenceOfChangedAssociations(updatedArchive);

        archiveFileHandler.setTaxaOfChangedMedia(updatedArchive, oldArchive);
//        archiveFileHandler.setTaxaOfChangedOccurrences(updatedArchive);
        archiveFileHandler.setTaxaOfChangedReference(updatedArchive);
//        archiveFileHandler.setTaxaOfChangedVernacular(updatedArchive);
    }
}