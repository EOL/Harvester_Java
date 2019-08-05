package org.bibalex.eol.harvester;

import org.apache.log4j.Logger;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
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
    private static final Logger logger = Logger.getLogger(StorageLayerClient.class);


    @RequestMapping(method = RequestMethod.POST)
    public boolean harvest(@RequestParam(value = "resourceID") String resourceID){
        try {
            System.out.println("maped true");
            boolean newResource = false;
            PropertiesHandler.initializeProperties();
            SynonymNodeHandler.setSynonymNodeHandler();
            System.out.println("before download");
            StorageLayerClient.downloadResource(resourceID + "", "1", "1");
            String updatedPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_org";
            String oldPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_old" + "_org";
            StorageLayerClient.downloadResource(resourceID, "1", "0");
            try {
                File checkOldVersion = new File(oldPath);
                String checkFilePath = checkOldVersion.getPath();
                System.out.println(checkFilePath);
                if(!checkFilePath.equals(null)) {
                    String deltaPath = StorageLayerClient.callDeltaCalculator(oldPath, updatedPath);
                    return harvesterAPI.preProcessing(deltaPath, Integer.parseInt(resourceID), newResource);
                }
            } catch (NoSuchFileException exception) {
                newResource = true;
                logger.info(exception+": No Older Versions of the resource found, calling Validator");
            }
            return harvesterAPI.preProcessing(updatedPath, Integer.parseInt(resourceID), newResource);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
