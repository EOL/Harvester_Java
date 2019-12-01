package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Has;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static apoc.coll.Coll.compare;
import static java.lang.Math.ceil;

/**
 * Created by Amr.Morad on 3/21/2017.
 */
public class MatchingScoreHandler {

    private double minimumAncestoryMatchPercentage;
    private double childMatchWeight;
    private double ancestorMatchWeight;
    private GlobalNamesHandler globalNameHandler;
    private HashMap<Integer, Double> minimumAncestorsMatch;

    public MatchingScoreHandler(){
        minimumAncestoryMatchPercentage = Double.parseDouble(ResourceHandler.getPropertyValue("minimumAncestoryMatchPercentage"));
        childMatchWeight = Double.parseDouble(ResourceHandler.getPropertyValue("childMatchWeight"));
        ancestorMatchWeight = Double.parseDouble(ResourceHandler.getPropertyValue("ancestorMatchWeight"));
        globalNameHandler = new GlobalNamesHandler();
        minimumAncestorsMatch = new HashMap<>();
        minimumAncestorsMatch.put(0, 0.0);
        minimumAncestorsMatch.put(1, 1.0);
        minimumAncestorsMatch.put(2, 1.0);
        minimumAncestorsMatch.put(3, 1.0);
        minimumAncestorsMatch.put(4, 1.0);
        minimumAncestorsMatch.put(5, 2.0);
        minimumAncestorsMatch.put(6, 2.0);
        minimumAncestorsMatch.put(7, 2.0);
        minimumAncestorsMatch.put(8, 3.0);
        minimumAncestorsMatch.put(9, 3.0);
        for (int i = 10 ; i < 251 ; i++){
            minimumAncestorsMatch.put(i, ceil(i*0.3));
        }
    }

    public int countMatches(ArrayList<Node> matchingNodeChildren, ArrayList<Node> nodeChildren){
        ArrayList<String> matchingNodeChildrenNames = getChildrenNames( matchingNodeChildren );
//        matchingNodeChildrenNames.retainAll(getChildrenNames(nodeChildren));
//        return matchingNodeChildrenNames.size();
        ArrayList<String> nodeChildrenNames =getChildrenNames(nodeChildren);
        HashSet matchingNodeChildrenNamesHash = new HashSet(matchingNodeChildrenNames);
        int count = 0;
        for(int i = 0 ; i< nodeChildrenNames.size() ; i++){
            if(matchingNodeChildrenNamesHash.contains(nodeChildrenNames.get(i)))
            {
                count ++;
            }
        }
        return count;
    }

    private ArrayList<String> getChildrenNames(ArrayList<Node> childrenNodes){
        ArrayList<String> childrenNames = new ArrayList<String>();
        for(Node childNode : childrenNodes){
            childrenNames.add(childNode != null ? childNode.getScientificName() : null);
        }
        return childrenNames;
    }

    //count ancestors has pageId
//    public int countAncestors(Node node){
//        int count = 0;
//        //why call node.getAncestors multiple times
//        ArrayList<Node> nodeAncestors = nodeMapper(node.getAncestors());
//        if (node != null && nodeAncestors != null) {
//            for (Node n : nodeAncestors) {
//                if (n.getPageId() != 0)
//                    count++;
//            }
//            return matchingAncestorsScore(count, nodeAncestors.size());
//        }
//        return 0;
//    }

    public int countAncestors(ArrayList<Node> resultAncestors, HashMap<Integer, Integer> nodesPages, ArrayList<Integer> nodeAncestorsPages, int ancestorsSize){
        System.out.println("size "+resultAncestors.size());
        int count = 0;
        if(resultAncestors.size() > 0 && nodeAncestorsPages.size() > 0) {
            ArrayList<Integer> resultAncestorsPages = new ArrayList<>();

            for (Node n : resultAncestors) {
                if (n.getPageId() != 0 ) {
                    resultAncestorsPages.add(n.getPageId());
                }
                else if (nodesPages.get(n.getGeneratedNodeId()) != null){
                    resultAncestorsPages.add(nodesPages.get(n.getGeneratedNodeId()));
                }
            }

            for(int i = 0 ; i< nodeAncestorsPages.size() ; i++){
                if(resultAncestorsPages.contains(nodeAncestorsPages.get(i)))
                {
                    count ++;
                }
            }

            return matchingAncestorsScore(count, ancestorsSize);
        }
        else return 0;

    }

    public int matchingAncestorsScore(int matchingAncestorsCount, int totalAncestorsCount){

        if (matchingAncestorsCount <= minimumAncestorsMatch.get(totalAncestorsCount)){
            return 0;
        }
        return matchingAncestorsCount;
    }

    public double samenessOfNames(String node_scientific_name, String other_scientific_name){
        if(globalNameHandler.getCanonicalForm(node_scientific_name).equalsIgnoreCase(globalNameHandler.getCanonicalForm(other_scientific_name))){
            JSONArray node_authors = globalNameHandler.getAuthors(node_scientific_name);
            JSONArray other_authors = globalNameHandler.getAuthors(other_scientific_name);
            if (node_authors == null || other_authors == null){
                if (node_authors == null && other_authors == null){
                    return 2;
                }
            }
            else if(node_authors.size()== other_authors.size()) {
                int counter = 0;
                for (int i = 0 ; i<node_authors.size(); i++)
                {
                    if(other_authors.contains(node_authors.get(i)))
                    {
                        counter ++;
                    }
                }
                if(counter == node_authors.size()){return 2.0;}

            }
            return 1.0;
        }

        return 0.5;
    }


    public double calculateScore(int matchingChildren, int matchingAncestors){
        return matchingChildren * childMatchWeight + matchingAncestors * ancestorMatchWeight;
    }

    public ArrayList<Node> nodeMapper(ArrayList<Node> beforeMapping)
    {
        ArrayList<Node> afterMapping = new ArrayList<Node>();
        ObjectMapper mapper = new ObjectMapper();
        for(int i =0 ; i<beforeMapping.size();i++)
        {
            Node n = mapper.convertValue(beforeMapping.get(i), Node.class);
            afterMapping.add(n);
        }
        return afterMapping;
    }

    public static void main(String [] args){


       MatchingScoreHandler msh= new MatchingScoreHandler();
        System.out.println(msh.samenessOfNames("Globorotalia miocenica subsp. mediterranea  Conil & Lys, 1969","Globorotalia miocenica subsp. mediterranea Conil & Lys, 1969"));

    }
}
