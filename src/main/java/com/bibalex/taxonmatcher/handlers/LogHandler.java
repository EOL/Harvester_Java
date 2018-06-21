package com.bibalex.taxonmatcher.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Amr.Morad
 */
public class LogHandler {

    private static boolean initialized = false;

    public static void initializeHandler() {
        System.setProperty("log4j.configurationFile",
                ResourceHandler.getPropertyValue("log4jConfigurationFile"));
        initialized = true;
    }

    public static Logger getLogger(String loggerName) {
        if (!initialized) {
            System.err.println("LogHandler not initialized !");
        }
        return LogManager.getLogger(loggerName);
    }

    public static void main(String[] args) {
//        LogHandler.initializeHandler("configs.properties");
//        Logger logger = getLogger(DwcaValidator.class.getName());
//        logger.info("Starting logging after intializations");
//        Logger logger = LogManager.getLogger(DwcaValidator.class.getName());
//        logger.info("Starting logging after intializations");
    }

}
