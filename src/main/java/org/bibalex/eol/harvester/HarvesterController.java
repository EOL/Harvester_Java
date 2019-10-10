package org.bibalex.eol.harvester;

import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/")
public class HarvesterController {

    @Autowired
    private HarvesterAPI harvesterAPI;
    private static final Logger logger = LoggerFactory.getLogger(HarvesterController.class);


    @RequestMapping(method = RequestMethod.POST)
    public boolean harvest(@RequestParam(value = "resourceID") String resourceID){
        try {
//            System.out.println("maped true");
            logger.info("The request to harvest resource number: "+resourceID+" is mapped successfully");
            boolean newResource = false;
            PropertiesHandler.initializeProperties();
            SynonymNodeHandler.setSynonymNodeHandler();
//            System.out.println("before download");
            StorageLayerClient.downloadResource(resourceID + "", "1", "1");
            String updatedPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_org";
            String oldPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_old" + "_org";
            StorageLayerClient.downloadResource(resourceID, "1", "0");
            try {
                File checkOldVersion = new File(oldPath);
                String checkFilePath = checkOldVersion.getPath();
//                System.out.println(checkFilePath);
                if(!checkFilePath.equals(null)) {
                    String deltaPath = StorageLayerClient.callDeltaCalculator(oldPath, updatedPath, resourceID);
                    return harvesterAPI.callValidation(deltaPath, Integer.parseInt(resourceID), newResource);
                }
            } catch (NoSuchFileException exception) {
                newResource = true;
                logger.error("NoSuchFileException: ", exception);
                logger.info("No Older Versions of the resource number: "+resourceID+" found, calling Validator");
            }
            return harvesterAPI.callValidation(updatedPath, Integer.parseInt(resourceID), newResource);
        } catch (IOException e) {
            logger.error("IOException: ", e);
//            e.printStackTrace();
            return false;
        }
    }
}
