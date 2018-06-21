package com.bibalex.taxonmatcher.parsers;

import com.bibalex.taxonmatcher.handlers.LogHandler;
import com.bibalex.taxonmatcher.handlers.ResourceHandler;
import com.bibalex.taxonmatcher.handlers.SolrHandler;
import org.apache.solr.common.SolrInputDocument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by Amr.Morad
 */
public class InputParser {

    private int limit;
    private SolrHandler solrHandler;

    public InputParser() {
        limit = Integer.parseInt(ResourceHandler.getPropertyValue("fileLimit"));
        solrHandler = new SolrHandler();
    }

    public void readFile(String filePath){
        System.out.println("Read file");
        BufferedReader bReader = null;
        ArrayList<String> lines = new ArrayList<String>();
        String line;
        int i = 0;

        try {
            bReader = new BufferedReader(new FileReader(filePath));

            //skip first line
            bReader.readLine();

            while ((line = bReader.readLine()) != null) {
                lines.add(line);
                if (i > limit){
                    parseTabSeparatedLines(lines);
                    lines = new ArrayList<String>();
                    i = 0;
                }
            }
            bReader.close();
            parseTabSeparatedLines(lines);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void parseTabSeparatedLines(ArrayList<String> lines){
        System.out.println("parse tab sepaerated");
        for (String line : lines){
            String [] splittedLine = line.split("\t", -1);
            for(String k : splittedLine){
                System.out.print(k + " ");
            }
            System.out.println();
            indexDynamicHierarchy(splittedLine);
        }
    }

    public void readNewNodesFile(){
        
    }

    private void indexDynamicHierarchy(String [] splittedLine){

        System.out.println("index dynamic hierarchy");
        SolrInputDocument document = fillCommonFields(splittedLine);

//        String [] childrenNames = splittedLine[9].split("\\|");
//        for (String childName : childrenNames)
//            document.addField("children", childName);

        String [] childrenNodesIds = splittedLine[9].split("\\|");
        for (String childNodeId : childrenNodesIds)
            document.addField("children_nodes_ids", childNodeId);

        String [] pageOtherNodes = splittedLine[10].split("\\|");
        for (String pageOtherNode : pageOtherNodes)
            document.addField("page_other_nodes", pageOtherNode);

        for (String s : splittedLine)
            System.out.println(s + " ");
        System.out.println();

        document.addField("pageId", splittedLine[11]);

//        String [] ancestorIds = splittedLine[13].split("\\|");
//        for (String ancestorId : ancestorIds)
//            document.addField("ancestor_ids", ancestorId);

        solrHandler.commitDocument(document);
    }

    private SolrInputDocument fillCommonFields(String [] splittedLine){
        System.out.println("fillCommon");
        SolrInputDocument document = new SolrInputDocument();

        document.addField("id", splittedLine[0]);
        document.addField("scientific_name", splittedLine[1]);
        document.addField("rank", splittedLine[2]);

        String [] nodeSynonyms = splittedLine[3].split("\\|");
        for(String synonym : nodeSynonyms)
            document.addField("synonyms", synonym);

        document.addField("kingdom", splittedLine[4]);
        document.addField("family", splittedLine[5]);
        document.addField("genus", splittedLine[6]);

        String [] ancestors = splittedLine[7].split("\\|");
        for(String ancestor : ancestors)
            document.addField("ancestors", ancestor);

        String [] children = splittedLine[8].split("\\|");
        for(String child : children)
            document.addField("children", child);

        return document;
    }

    public static void main (String[] args){
        ResourceHandler.initialize("config.properties");
        LogHandler.initializeHandler();

        InputParser dhip = new InputParser();
        dhip.readFile(ResourceHandler.getPropertyValue("dynamicHierarchyFile"));
    }
}
