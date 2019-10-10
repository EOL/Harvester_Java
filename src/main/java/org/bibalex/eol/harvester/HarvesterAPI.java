package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;

import org.bibalex.eol.handler.MetaHandler;
import org.bibalex.eol.parser.DwcaParser;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.validator.DwcaValidator;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;

@Service
public class HarvesterAPI {

    @PersistenceContext
    private EntityManager entityManager;
    private static final Logger logger = LoggerFactory.getLogger(HarvesterAPI.class);

    public boolean callValidation(String path, int resourceID, boolean newResource){
        try {
            MetaHandler metaHandler =new MetaHandler();

            DwcaValidator validator = new DwcaValidator("configs.properties");
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            Archive dwcArchive=null;
            try {
                dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
            }catch (Exception e){
//                System.out.println("folder need to editing to be readable by library");
                logger.error("Exception: ", e);
                logger.info("Resource Number: "+resourceID+": Resource Archive is not readable by DWCA IO, modifying");
                metaHandler.adjustMetaFileToBeReadableByLibrary(extractToFolder.getPath());
                dwcArchive = ArchiveFactory.openArchive(extractToFolder);
            }
//            System.out.println("call validationnnnnnnnnnnnnn");
            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
            String validArchivePath= FilenameUtils.removeExtension(path)+".out_valid";
            metaHandler.addGeneratedAutoId(validArchivePath);

            boolean done = callParser(validArchivePath, resourceID, newResource, entityManager);
            return done;
        } catch (IOException e) {
            logger.error("IOException: ", e);
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
            return false;
        } catch (Exception e) {
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
//            e.printStackTrace();
            logger.error("Exception: ",  e);
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
            logger.error("IOException: ", e);
            return false;
        }

    }
}
