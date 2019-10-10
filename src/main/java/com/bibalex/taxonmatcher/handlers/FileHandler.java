package com.bibalex.taxonmatcher.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Amr.Morad
 */
public class FileHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);


    public FileHandler() {
//        logger = LogHandler.getLogger(NodeMapper.class.getName());
    }

    public void writeToFile(String s) {
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
            logger.error("Output file exception: ", e);
        }
    }

}
