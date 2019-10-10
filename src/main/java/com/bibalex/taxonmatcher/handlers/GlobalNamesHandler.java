package com.bibalex.taxonmatcher.handlers;

import org.bibalex.eol.handler.MetaHandler;
import org.globalnames.parser.ScientificNameParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Amr.Morad on 4/16/2017.
 */
public class GlobalNamesHandler {

    private JSONParser parser;
//    private static Logger logger;
    private static final Logger logger = LoggerFactory.getLogger(GlobalNamesHandler.class);

    public GlobalNamesHandler(){
        parser = new JSONParser();
//        logger = LogHandler.getLogger(GlobalNamesHandler.class.getName());
    }

    private JSONObject getParsedJson(String name){
        String jsonStr = ScientificNameParser.instance()
                .fromString(name)
                .renderCompactJson();
        try {
            return (JSONObject)parser.parse(jsonStr);
        } catch (ParseException e) {
            logger.error("ParseException: ", e);
//            e.printStackTrace();
        }
        return null;
    }

    private boolean parseAndGetResult(String name, String attribute){
        Object att = (getParsedJson(name)).get(attribute);
        return att == null ? false : (Boolean)att;
    }

    public boolean isVirus(String name){
        return parseAndGetResult(name, "virus");
    }

    public boolean isSurrogate(String name){
        return parseAndGetResult(name, "surrogate");
    }

    public boolean isHybrid(String name){
        return parseAndGetResult(name, "hybrid");
    }

    public String getCanonicalForm(String name){
        JSONObject jsonObject = getParsedJson(name);
//        System.out.println(jsonObject);
        return (Boolean) jsonObject.get("parsed") ? (String) ((JSONObject) jsonObject.get("canonical_name")).get("value") : "";
    }

    public Boolean isParsed (String name){
        JSONObject jsonObject = getParsedJson(name);
        return  (Boolean) jsonObject.get("parsed") ;
    }

    public JSONArray  getAuthors(String name){
        JSONObject jsonObject = getParsedJson(name);
        JSONObject authorship = null;
        JSONObject basionym_authorship = null;
        JSONArray infraspecific_epithets = null;
        if ((Boolean) jsonObject.get("parsed"))
        {
//            System.out.println(jsonObject);
            JSONArray details= (JSONArray) jsonObject.get("details");
            JSONObject  details_first= (JSONObject)details.get(0);

            if((infraspecific_epithets = (JSONArray) details_first.get("infraspecific_epithets"))!= null){
                JSONObject  infraspecific_epithets_first= (JSONObject)infraspecific_epithets.get(0);
                if((authorship = (JSONObject) infraspecific_epithets_first.get("authorship"))!=null) {
                    if ((basionym_authorship = (JSONObject) authorship.get("basionym_authorship")) != null) {
                        JSONArray authors_arrray = (JSONArray) basionym_authorship.get("authors");
                        return authors_arrray;

                    }
                }

            }
            else if(details_first.get("specific_epithet")!=null){
                JSONObject specific_epithet = (JSONObject) details_first.get("specific_epithet");
                if(specific_epithet.get("authorship")!=null){
                  if((authorship = (JSONObject) specific_epithet.get("authorship"))!=null) {
                      if ((basionym_authorship = (JSONObject) authorship.get("basionym_authorship")) != null) {
                          JSONArray authors_arrray = (JSONArray) basionym_authorship.get("authors");
                          return authors_arrray;
                      }
                  }
                }
            }

        }
        return null;


    }

    public boolean hasAuthority(String name){
        JSONArray nameParts = (JSONArray) getParsedJson(name).get("positions");
        if (nameParts != null) {
            for (int i = 0; i < nameParts.size(); i++) {
                JSONArray partArray = (JSONArray) nameParts.get(i);
                if (partArray.get(0).toString().contains("author")) {
//                    System.out.println("has authority");
                    logger.info("Name: " + name + " has authority.");
                    return true;
                }
            }
        }
        logger.info("Name: " + name + " has no authority.");
        logger.info("Returned False");
        return false;
    }

    public static void main(String [] args){

//        ResourceHandler.initialize("config.properties");
//        LogHandler.initializeHandler();

        GlobalNamesHandler gnh = new GlobalNamesHandler();
//        gnh.hasAuthority("Parus major Linnaeus, 1788");
//        System.out.println(gnh.getAuthors("Parus major"));
//        System.out.println("================================================");
//        System.out.println(gnh.getAuthors("Parus major Linnaeus, 1788"));
//        System.out.println("================================================");
//        System.out.println(gnh.getAuthors("Globorotalia miocenica subsp. mediterranea Catalano & Sprovieri, 1969"));
//        System.out.println("================================================");
//        System.out.println(gnh.getAuthors("Gymnodiniales s.l."));
//        System.out.println("================================================");
        System.out.println(gnh.isParsed("Parus major Linnaeus, 1788"));
        System.out.println(gnh.isParsed("Parus major"));
        System.out.println(gnh.isParsed("unplaced extinct Diptera"));
//        System.out.println(gnh.getAuthors("unplaced extinct Diptera"));
//        gnh.hasAuthority("test");
    }
}
