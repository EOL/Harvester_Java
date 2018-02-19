package org.bibalex.eol.validator.handlers;

import org.bibalex.eol.validator.DwcaValidator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author  Ahmad.Eldefrawy
 */
public class XMLHandler {
    private static boolean initialized = false;
//    protected static Logger logger;
private static final Logger logger = Logger.getLogger(DwcaValidator.class);
    private static String xmlFilePath;
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder builder;
    public static Document document;

    public static void initializeHandler(String propertiesFile, String validationRulesFile) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(XMLHandler.class.getClassLoader().getResource(propertiesFile).getPath());
            prop.load(input);
            xmlFilePath = prop.getProperty("validationRulesFile");
            initialized = true;
        } catch (IOException ex) {
            System.err.println("Failed to load the validationRulesFilePath from the properties file");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("Failed to close the properties file after trying to read the validationRulesFilePath");
                }
            }
        }
    }

    public XMLHandler( ) {
//        this.logger = LogHandler.getLogger(XMLHandler.class.getName());
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLHandler.class.getClassLoader().getResource("ValidationRules.xml").getPath()));
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException while trying to create to DocumentBuilder in XMLHandler");
//            logger.error(e);
        } catch (SAXException e) {
            logger.error("SAXException while trying to parse document in XMLHandler");
//            logger.error(e);
        } catch (IOException e) {
            logger.error("IOException while trying to parse document in XMLHandler");
//            logger.error(e);
        }
    }


    public static String getRootElement() {
        Element root = document.getDocumentElement();
        logger.info(root.getNodeName());
        return root.getNodeName();
    }


    // FOR TESTING
    public static void main(String[] args) {
//        LogHandler.initializeHandler("configs.properties");
        XMLHandler xmlHandler = new XMLHandler();
//        xmlHandler.getRootElement();
//        xmlHandler.getValidationRules();
    }

}
