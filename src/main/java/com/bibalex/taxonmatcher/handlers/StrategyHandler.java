package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import com.bibalex.taxonmatcher.models.Strategy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.logging.log4j.Logger;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Amr.Morad
 */
public class StrategyHandler {

    private ArrayList<Strategy> strategies;
    private static Logger logger;

    public StrategyHandler(){
        strategies = new ArrayList<Strategy>();
        logger = LogHandler.getLogger(NodeMapper.class.getName());
        loadStrategies();
    }

    public void loadStrategies() {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) parser.parse(new FileReader(ResourceHandler.getPropertyValue("strategiesFilePath")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (Object jsonArrayElement : jsonArray){
            JSONObject jsonObject = (JSONObject) jsonArrayElement;
            Strategy strategy = new Strategy((String)jsonObject.get("attribute"), (String)jsonObject.get("index"),
                    (String)jsonObject.get("type"));
            strategies.add(strategy);
        }
    }

    public Strategy firstNonScientificStrategy(){
        for(Strategy strategy : strategies){
            if (!strategy.getAttribute().equals(ResourceHandler.getPropertyValue("strategyScientificName")))
                return strategy;
        }
        return null;
    }

    public Strategy defaultStrategy(){
        return strategies.get(0);
    }

    public Strategy getNextStrategy(Strategy strategy){
        logger.info("================================");
        int oldIndex = 0;
        for(Strategy s : strategies){
            if (strategy.getAttribute().equalsIgnoreCase(s.getAttribute()) &&
                    strategy.getIndex().equalsIgnoreCase(s.getIndex()))
                break;
            oldIndex++;
        }
        oldIndex++;
        logger.info("Index is: "+ oldIndex);
        logger.info("================================");
        return oldIndex > strategies.size() - 1 ? null : strategies.get(oldIndex);
    }


}
