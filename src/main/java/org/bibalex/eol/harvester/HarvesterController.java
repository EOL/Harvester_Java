package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

@RestController
@RequestMapping("/harvest/")
public class HarvesterController {

    @Autowired
    private HarvesterAPI harvesterAPI;

    @RequestMapping(method = RequestMethod.POST)
    public boolean harvest(@RequestParam(value = "resourceID") String resourceID) {
        try {
            PropertiesHandler.initializeProperties();
            StorageLayerClient.downloadResource(resourceID + "", "1", "1");
            String updatedPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_org";
            System.out.println("henaaaaaaaaaaaaaaXXXXXXX " + updatedPath);
            String oldPath = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_old" + "_org";
            StorageLayerClient.downloadResource(resourceID, "1", "0");
            try {
                File checkOldVersion = new File(oldPath);
                String checkFilePath = checkOldVersion.getPath();
                System.out.println(checkFilePath);
                if(!checkFilePath.equals(null))
                StorageLayerClient.getArchiveToValidate(oldPath, updatedPath);
            } catch (NoSuchFileException exception) {
                System.out.println("#################### "+exception+": No Older Versions of the resource found, calling Validator#############");
            }
            return harvesterAPI.callValidation(updatedPath, Integer.parseInt(resourceID));
//            return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }


    }}
