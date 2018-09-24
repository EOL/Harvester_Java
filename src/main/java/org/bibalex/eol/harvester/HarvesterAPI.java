package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.handler.MetaHandler;
import org.bibalex.eol.parser.DwcaParser;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.validator.DwcaValidator;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;

@Service
public class HarvesterAPI {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean callValidation(String path, int resourceID, boolean newResource){
        try {
            MetaHandler metaHandler =new MetaHandler();
//
            DwcaValidator validator = new DwcaValidator("configs.properties");
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            metaHandler.adjustMetaFileToBeReadableByLibrary(extractToFolder.getPath());
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(path));
            System.out.println("call validationnnnnnnnnnnnnn");
            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
//            return true;
            String validArchivePath= FilenameUtils.removeExtension(path)+".out_valid";
            metaHandler.addGeneratedAutoId(validArchivePath);

            boolean done = callParser(path, resourceID, newResource, entityManager);
            return done;
        } catch (IOException e) {
            e.printStackTrace();
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
            return false;
        } catch (Exception e) {
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
            e.printStackTrace();
            return false;
        }

    }

    private boolean callParser(String path, int resourceID, boolean newResource, EntityManager entityManager){
        Archive dwcArchive = null;
        try {
            //for testing
//            File myArchiveFile = new File(path);
//            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
//            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);

            // for production
            PropertiesHandler.initializeProperties();
            dwcArchive = ArchiveFactory.openArchive(new File(path));

            //general
            DwcaParser dwcaP = new DwcaParser(dwcArchive, newResource, entityManager);
            dwcaP.prepareNodesRecord(resourceID);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
