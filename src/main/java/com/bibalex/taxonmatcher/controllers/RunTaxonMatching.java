package com.bibalex.taxonmatcher.controllers;

import com.bibalex.taxonmatcher.handlers.LogHandler;
import com.bibalex.taxonmatcher.handlers.Neo4jHandler;
import com.bibalex.taxonmatcher.handlers.ResourceHandler;

public class RunTaxonMatching {
    public void RunTaxonMatching(int resourceId)

    {
        ResourceHandler.initialize("configs.properties");
        LogHandler.initializeHandler();
        Neo4jHandler neo4jHandler = new Neo4jHandler();
        NodeMapper nodeMapper = new NodeMapper(resourceId);
        nodeMapper.mapAllNodesToPages(neo4jHandler.getRootNodes(resourceId));
    }

    public static void main (String[]args){
        RunTaxonMatching runTaxonMatching = new RunTaxonMatching();
        runTaxonMatching.RunTaxonMatching(1);
    }
}
