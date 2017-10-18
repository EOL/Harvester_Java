package org.bibalex.eol.harvester;

import org.bibalex.eol.handler.PropertiesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

//import org.eol.parser.DwcaParser;
//import org.eol.validator.DwcaValidator;
//import org.gbif.dwca.io.Archive;
//import org.gbif.dwca.io.ArchiveFactory;

public class Harvester {

    private static final Logger logger = LoggerFactory.getLogger(Harvester.class);

    public static void main(String[] args) throws IOException {
//        String dwcArchivePath = "/home/ba/EOL_Recources/4.tar.gz";
        Harvester harvester = new Harvester();
        harvester.processHarvesting(1);
    }

    public void processHarvesting(int resourceId) throws IOException {
        //initialize properties
        logger.debug("Starting: " + resourceId);
        logger.debug("Initialize properties");
        PropertiesHandler.initializeProperties();
        //call storagelayerClient
        logger.debug("Create storage layer client: ");
        StorageLayerClient storageLayerClient = new StorageLayerClient();
        logger.debug("Call download: ");
//        storageLayerClient.downloadResource(resourceId+"", "1");
        logger.debug("Get the property: ");
        String path = PropertiesHandler.getProperty("storage.output.directory") + File.separator + resourceId + "_org";
        System.out.println(path);
        logger.debug("Call the harvest: ");
//        harvest(path, resourceId);
    }

}
