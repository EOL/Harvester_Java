package com.deltacalculator;

import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.Logger;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.deltacalculator.DeltaCalculator.DWCADiff;


public class ArchiveHandler {
    private static final Logger logger = Logger.getLogger(ArchiveHandler.class);

    Archive openDwcAFolder(String path) {
        try {
            Archive dwcArchive;
            System.out.println("Extracting Archive: " + path);
            logger.info("Extracting Archive: " + path);
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
            String metaFiles[] = {"metadata.xml", "meta.xml", "eml.xml"};
            int i;
            boolean metaFileExists = false;
            File metaFile = new File(dwcArchive.getLocation().getPath() + "/" + metaFiles[0]);
            System.out.println("%%%%%%%%%%%"+extractToFolder.getPath()+"%%%%%%%%%%%%%%%%");

            for (i = 0; i < metaFiles.length; i++) {
                metaFile = new File(dwcArchive.getLocation().getPath() + "/" + metaFiles[i]);
                if (metaFile.exists()) {
                    metaFileExists = true;
                    break;
                }
            }
            if (metaFileExists == false) {
                System.out.println(path+": Meta File not Found!");
                logger.info(path + ": Meta File not Found!");
            } else dwcArchive.setMetadataLocation(metaFile.getPath());
            return dwcArchive;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
            return null;
        }
    }



    void filterContent(Archive version1, Archive version2) {

        ArrayList<Term>
                archive1Content = setArchiveContentArrayList(version1),
                archive2Content = setArchiveContentArrayList(version2),
                archive1ContentTemp = setArchiveContentArrayList(version1),
                archive2ContentTemp = setArchiveContentArrayList(version2);


        int i;
        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();


        boolean isAddedContent = archive2ContentTemp.removeAll(archive1Content),
                isDeletedContent = archive1ContentTemp.removeAll(archive2Content);
        if(archive2ContentTemp.size()==0)
            isAddedContent = false;
        if(archive1ContentTemp.size()==0)
            isDeletedContent = false;

        System.out.println("Added Files: " + isAddedContent + ", Deleted Files: " + isDeletedContent);
        System.out.println("Number of Added Files: " + archive2ContentTemp.size() + ", Number of Deleted Files: " + archive1ContentTemp.size());
        logger.info("Added Files: " + isAddedContent + ", Deleted Files: " + isDeletedContent);
        logger.info("Number of Added Files: " + archive2ContentTemp.size() + ", Number of Deleted Files: " + archive1ContentTemp.size());

        if (isAddedContent == true) {
            //mark new file as inserted
            for (i = 0; i < archive2ContentTemp.size(); i++) {
                ArchiveFile addedArchiveFile = version2.getExtension(archive2ContentTemp.get(i));
                File addedFile = new File(version2.getLocation().getPath() + "/" + addedArchiveFile.getTitle());
                System.out.println("Added File Found: " + addedArchiveFile.getTitle());
                logger.info("Added File Found: " + addedArchiveFile.getTitle());
                archiveFileHandler.readFromFileWriteToFile(addedFile, true, DWCADiff.getPath(), addedArchiveFile);
                archive2Content.remove(archive2ContentTemp.get(i));
            }
        }

        if (isDeletedContent == true) {
            for (i = 0; i < archive1ContentTemp.size(); i++) {
                ArchiveFile deletedArchiveFile = version1.getExtension(archive1ContentTemp.get(i));
                File deletedFile = new File(version1.getLocation().getPath() + "/" + deletedArchiveFile.getTitle());
                System.out.println("Deleted File Found: " + deletedArchiveFile.getTitle());
                logger.info("Deleted File Found: " + deletedArchiveFile.getTitle());
                archiveFileHandler.readFromFileWriteToFile(deletedFile, false, DWCADiff.getPath(), deletedArchiveFile);
                archive1Content.remove(archive1ContentTemp.get(i));
            }
        }
        // if the two lists are identical, or reharvesting the rest of the two archives files
        archiveFileHandler.compareContent(version1, version2, archive2Content);
    }

    private ArrayList<Term> setArchiveContentArrayList(Archive archive) {
        ArrayList<Term> arrayList = new ArrayList<>();
        int i = 0;
        for (ArchiveFile archiveFile : archive.getExtensions()) {
            arrayList.add(i, archiveFile.getRowType());
            i++;
        }
        arrayList.add(i, archive.getCore().getRowType());
        i++;
        return arrayList;
    }
}