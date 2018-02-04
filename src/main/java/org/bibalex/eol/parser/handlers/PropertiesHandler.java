package org.bibalex.eol.parser.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Amr.Morad
 */

public class PropertiesHandler {
    static Properties prop = new Properties();

    public static void initializeProperties() throws IOException {
        InputStream input = PropertiesHandler.class.getClassLoader().getResourceAsStream("configs.properties");
        prop.load(input);
    }

    public static String getProperty(String key){
        return prop.getProperty(key);
    }
}
