package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Amr.Morad
 */
public class FileHandler {

    private static Logger logger;

    public FileHandler(){
        logger = LogHandler.getLogger(NodeMapper.class.getName());
    }

    public void writeToFile(String s){
        try {
            FileWriter outputFile = new FileWriter
                    (ResourceHandler.getPropertyValue("outputFileName"), true);
            logger.info("After creating file writer");
            outputFile.write("\n");
            outputFile.write(s);
            logger.info("After writing to the file");
            outputFile.flush();
            outputFile.close();
            logger.info("After flushing and closing the file writer");
        } catch (IOException e) {
            logger.error("output file exception "+e);
        }
    }

}
