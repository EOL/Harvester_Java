package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.DwcaParser;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.validator.DwcaValidator;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class HarvesterAPI {

    public boolean callValidation(String path, int resourceID){
        try {
            DwcaValidator validator = new DwcaValidator("configs.properties");
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(path));
            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
//            return true;
            return callParser(path+".out_valid", resourceID);
        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
            return false;
        } catch (Exception e) {
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
//            e.printStackTrace();
            return false;
        }

    }

    private boolean callParser(String path, int resourceID){
        Archive dwcArchive = null;
        try {
            PropertiesHandler.initializeProperties();
            dwcArchive = ArchiveFactory.openArchive(new File(path));
            DwcaParser dwcaP = new DwcaParser(dwcArchive);
            dwcaP.prepareNodesRecord(resourceID);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
