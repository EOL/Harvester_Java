package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.DwcaParser;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

//import org.eol.parser.DwcaParser;
//import org.eol.validator.DwcaValidator;
//import org.gbif.dwca.io.Archive;
//import org.gbif.dwca.io.ArchiveFactory;

public class Harvester {

    private static final Logger logger = Logger.getLogger(Harvester.class);

    public static void main(String[] args) throws IOException {
//        String dwcArchivePath = "/home/ba/EOL_Recources/4.tar.gz";
//        Harvester harvester = new Harvester();
//        harvester.processHarvesting(4);
        HarvesterAPI harvesterAPI= new HarvesterAPI();
        try {
            PropertiesHandler.initializeProperties();
            StorageLayerClient.downloadResource(179+"", "1","1");
            String path = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + 179 + "_org";
            System.out.println("henaaaaaaaaaaaaaa "+ path);

//            return true;
//            harvesterAPI.callValidation(path, 179, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String processHarvesting(int resourceId) throws IOException {
        //initialize properties
        logger.debug("Starting: " + resourceId);
        logger.debug("Initialize properties");
        PropertiesHandler.initializeProperties();
        //call storagelayerClient
        logger.debug("Create storage layer client: ");
        StorageLayerClient storageLayerClient = new StorageLayerClient();
        logger.debug("Call download: ");
        storageLayerClient.downloadResource(resourceId+"", "1","1");
        logger.debug("Get the property: ");
        String path = PropertiesHandler.getProperty("storage.output.directory") + File.separator + resourceId + "_org";
        System.out.println(path);
        logger.debug("Call the harvest: ");
        return harvest(path, resourceId);
    }

    public String harvest(String dwcArchivePath, int resourceId){
        Archive dwcArchive = openDwcAFolder(dwcArchivePath);
        DwcaParser dwcaP = new DwcaParser(dwcArchive, true, null);
        dwcaP.prepareNodesRecord(resourceId);
        return "success";
    }

    private Archive openDwcAFolder(String path){
        Archive dwcArchive;
        try {
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
        } catch (Exception e) {
            System.out.println("Failed to parse the Darwin core archive " + e.getMessage());
            return null;
        }
        return dwcArchive;
    }


}