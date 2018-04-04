package org.bibalex.eol.harvester;

import org.apache.log4j.Logger;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/harvest/")
public class HarvesterController {

    @Autowired
    private HarvesterAPI harvesterAPI;
    private static final Logger logger = Logger.getLogger(HarvesterController.class);


    @RequestMapping(method = RequestMethod.POST)
    public boolean harvest(@RequestParam(value = "resourceID") String resourceID) {
        try {
            PropertiesHandler.initializeProperties();
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
                if(!checkFilePath.equals(null))
                StorageLayerClient.callDeltaCalculator(oldPath, updatedPath);
            } catch (NoSuchFileException exception) {
                logger.info(exception+": No Older Versions of the resource found, calling Validator");
            }
            return harvesterAPI.callValidation(updatedPath, Integer.parseInt(resourceID));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }


    }}
