package com.bibalex.taxonmatcher.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//
///**
// * Created by Amr.Morad
// */
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
        return LoggerFactory.getLogger(loggerName);
    }

    public static void main(String[] args) {
//        LogHandler.initializeHandler("configs.properties");
//        Logger logger = getLogger(DwcaValidator.class.getName());
//        logger.info("Starting logging after intializations");
//        Logger logger = LogManager.getLogger(DwcaValidator.class.getName());
//        logger.info("Starting logging after intializations");
    }

}
