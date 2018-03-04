package org.bibalex.eol.harvester;

import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/harvest/")
public class HarvesterController {

    @Autowired
    private HarvesterAPI harvesterAPI;

    @RequestMapping(method = RequestMethod.POST)
    public boolean harvest(@RequestParam(value = "resourceID") String resourceID){
        try {
            PropertiesHandler.initializeProperties();
            StorageLayerClient.downloadResource(resourceID+"", "1");
            String path = PropertiesHandler.getProperty
                    ("storage.output.directory") + File.separator + resourceID + "_org";
            System.out.println("henaaaaaaaaaaaaaa "+ path);

//            return true;
            return harvesterAPI.callValidation(path, Integer.parseInt(resourceID));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
