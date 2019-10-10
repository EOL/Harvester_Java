package com.bibalex.taxonmatcher.controllers;

import com.bibalex.taxonmatcher.handlers.*;
import com.bibalex.taxonmatcher.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrServerException;
import org.bibalex.eol.handler.MetaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by Amr.Morad
 */
public class NodeMapper {

    private StrategyHandler strategyHandler;
    private GlobalNamesHandler globalNameHandler;
    private NodeHandler nodeHandler;
    private MatchingScoreHandler matchingScoreHandler;
    private SearchHandler searchHandler;
    private int maxAncestorDepth;
//    private static Logger logger;
//    private FileHandler fileHandler;
    private Neo4jHandler neo4jHandler;
    private SolrHandler solrHandler;
    private static final Logger logger = LoggerFactory.getLogger(NodeMapper.class);

    int resourceId;
    private HashMap<Integer, Integer> nodePages;

    public NodeMapper(int resourceId){
        strategyHandler = new StrategyHandler();
        globalNameHandler = new GlobalNamesHandler();
        nodeHandler = new NodeHandler();
        matchingScoreHandler = new MatchingScoreHandler();
        searchHandler = new SearchHandler();
        maxAncestorDepth = Integer.parseInt(ResourceHandler.getPropertyValue("maxAncestorDepth"));
//        logger = LogHandler.getLogger(NodeMapper.class.getName());
//        fileHandler = new FileHandler();
        neo4jHandler = new Neo4jHandler();
        this.resourceId = resourceId;
        this.solrHandler = new SolrHandler();
        this.nodePages = new HashMap<>();
    }

    public void mapAllNodesToPages(ArrayList<Node> rootNodes){
//        logger.info("before getting root nodes");
        ArrayList<Node> mappedRootNodes= nodeHandler.nodeMapper(rootNodes);
        logger.info("Resource number: "+ resourceId +"- Got all root nodes");
        mapNodes(mappedRootNodes);
        neo4jHandler.assignPageToNodes(nodePages);
    }

    public void mapNodes(ArrayList<Node> rootNodes){
//        logger.info("Inside Map Node method");
//        System.out.println("mapNodes");
        logger.info("Running");
        for(Node node : rootNodes){
//            System.out.println("mapNodes: mapping node: " + node.getScientificName());
            logger.info("Mapping Node: " + node.getScientificName());
            mapIfNeeded(node);
        }
    }

    public void mapIfNeeded(Node node){
        Strategy usedStrategy = strategyHandler.defaultStrategy();
//        System.out.println("mapIfNeeded: used strategy is: " + usedStrategy.getAttribute());
        logger.info("The Used Strategy is: " + usedStrategy.getAttribute());
        int usedAncestorDepth = 0;
        if (node.needsToBeMapped()&& globalNameHandler.isParsed(node.getScientificName())){
//            System.out.println("mapIfNeeded: needs to be mapped");
            logger.info("Node: " +node.getGeneratedNodeId()+ " needs to be mapped.");
            if(!globalNameHandler.hasAuthority(node.getScientificName())){
//                System.out.println("mapIfNeeded: node does not have authority");
                logger.info("Node: "+ node.getGeneratedNodeId()+ " has no authority.");
                usedStrategy = strategyHandler.firstNonScientificStrategy();
            }
//            System.out.println("mapIfNeeded: before mapNode");
//            logger.info("mapIfNeeded: before mapNode");
            mapNode(node, usedAncestorDepth, usedStrategy);
        }
//        System.out.println("---------------- has children is ------------- " + node.hasChildren() + " size " + node.getChildren().size());
        ArrayList<Node> children = node.getChildren();
        if(children.size() > 0 ){
            logger.info("Getting Children Nodes for Node: "+ node.getGeneratedNodeId());
            mapNodes(nodeHandler.nodeMapper(children));
        }
    }

    private void mapNode(Node node, int depth, Strategy strategy){
        Node ancestor;
        if(globalNameHandler.isSurrogate(node.getScientificName())){
//            System.out.println("map node: surrogate");
            logger.info("Node: "+ node.getGeneratedNodeId()+" is Surrogate");
            unmappedNode(node);
        }else{
            ArrayList<Node> ancestors = nodeHandler.nodeMapper(node.getAncestors());
            if (globalNameHandler.isVirus(node.getScientificName())){
//                System.out.println("map node: virus");
                logger.info("Node: "+ node.getGeneratedNodeId()+" is Virus");
                //not finalized as we need ancestor to be arraylist
                // we don't know the root node of virus
//                ancestor = nodeHandler.nodeMapper(nodeHandler.nativeVirus()).get(0);
//                ancestor = nodeHandler.matchedAncestor(nodeHandler.nodeMapper(node.getAncestors()), depth);
                ancestor = nodeHandler.matchedAncestor(ancestors, depth, nodePages);
            }else{
//                System.out.println("map Node : not virus neither surrogate");
                logger.info("Node: "+node.getGeneratedNodeId()+" is neither Surrogate nor Virus");
                logger.info("Calling matchedAncestor for Node: "+ node.getGeneratedNodeId());
//            ancestor = nodeHandler.matchedAncestor(nodeHandler.nodeMapper(node.getAncestors()), depth);
                ancestor = nodeHandler.matchedAncestor(ancestors, depth, nodePages);
                logger.info("Matched All Ancestors for Node: "+ node.getGeneratedNodeId());
            }
            mapUnflaggedNode(node, ancestor, depth, strategy,ancestors);
        }
    }

    private void  mapUnflaggedNode(Node node, Node ancestor, int depth, Strategy strategy,ArrayList<Node> ancestors){
        ArrayList<SearchResult> results = searchHandler.getResults(node, strategy, ancestor);
        Strategy nextStrategy;
        if(results.size() == 1){
//            System.out.println("other "+results.get(0).getScientificName());
            logger.info("Only one result is returned.");
            if (results.get(0).getPageId() == 0) {
                unmappedNode(node);
            } else {
                mapToPage(node, results.get(0).getPageId(), results.get(0).getNodeId());
            }


        }else if(results.size() > 1){
            logger.info("More than one result are returned.");
            logger.info("Calling findBestMatch for Node: " + node.getGeneratedNodeId());
            MatchingScore matchingScore = findBestMatch(node, results);
            logger.info("Found Best Match for Node: " + node.getGeneratedNodeId());
            if(matchingScore != null && matchingScore.getScore() >= 0.1){
                mapToPage(node, matchingScore.getPageId(), matchingScore.getNodeId());
            }
            else {
                unmappedNode(node);
            }
        }else{
            logger.info("No Result Found, Start Node ID: " + node.getGeneratedNodeId() + ", Depth: "+depth);
            nextStrategy = strategyHandler.getNextStrategy(strategy);
            if (nextStrategy == null) {

                nextStrategy = strategyHandler.firstNonScientificStrategy();
                depth++;
                Node prev_ancestor = ancestor;
//                ancestor = nodeHandler.matchedAncestor(nodeHandler.nodeMapper(node.getAncestors()), depth);
                ancestor = nodeHandler.matchedAncestor(ancestors, depth, nodePages);
                logger.info("Depth: " + depth);
                if(ancestor!=null)
//                    System.out.println("ancestor "+ancestor.getGeneratedNodeId());
                    logger.info("Ancestor: "+ ancestor.getGeneratedNodeId());
                if(prev_ancestor!=null)
                    logger.info("Previous Ancestor: "+prev_ancestor.getGeneratedNodeId());
                if (depth > maxAncestorDepth||ancestor == null||ancestor.getGeneratedNodeId() == prev_ancestor.getGeneratedNodeId() ) {
                    logger.info("Maximum Ancestor Depth Exceeded");
                    unmappedNode(node);
                    return;
                }
//                System.out.println("depth is less than max depth and will call recursion");
                logger.info("Depth is less than maximum depth, calling the current function recursively.");
            }
//            logger.info("Recursive call");
            mapUnflaggedNode(node, ancestor, depth, nextStrategy,ancestors);
        }
    }

    private MatchingScore findBestMatch(Node node, ArrayList<SearchResult> results){
        ArrayList<MatchingScore> scores = new ArrayList<MatchingScore>();

        for(SearchResult result : results){

//            logger.info("before getting matched children count");
//            System.out.println(neo4jHandler.getNodesFromIds(result.getChildren()));
            int matchedChildrenCount = matchingScoreHandler.countMatches(nodeHandler.nodeMapper(neo4jHandler.getNodesFromIds(result.getChildren())),nodeHandler.nodeMapper(node.getChildren()));
//            System.out.println("**********************************************************************************");
//            logger.info("after getting matched children count");
            logger.info("Node: " + node.getGeneratedNodeId() + "- Matched Children Count: " + matchedChildrenCount);
//            logger.info("before getting matched ancestors count");
            int matchedAncestorsCount = matchingScoreHandler.countAncestors(nodeHandler.nodeMapper(neo4jHandler.getNodesFromIds(result.getAncestors())), nodePages);
//            int matchedAncestorsCount = matchingScoreHandler.countAncestors(node);
//            logger.info("after getting matched ancestors count");
            logger.info("Node: " + node.getGeneratedNodeId() + "- Matched Ancestors Count: "+ matchedAncestorsCount);
//            logger.info("before sameness of names ");
            logger.info("Node Scientific Name: " + node.getScientificName());
            logger.info("Result Scientific Name: " + result.getScientificName());
            double sameness_of_names = matchingScoreHandler.samenessOfNames(node.getScientificName(),result.getScientificName());
            logger.info("After Calling samenessOfNames: "+ sameness_of_names);
//            logger.info("before calculating score");
            double overallScore = matchingScoreHandler.calculateScore(matchedChildrenCount, matchedAncestorsCount);
            overallScore *= sameness_of_names;
//            logger.info("after calculating score");
            logger.info("After Calculating Score- Overall Score: " + overallScore);
            MatchingScore score = new MatchingScore(matchedChildrenCount,
                    matchedAncestorsCount, overallScore, result.getPageId(), result.getNodeId());
//            logger.info("score: "+score.getScore() + " of page: "+score.getPageId());
            logger.info("Page: " + score.getPageId() + "- Score: " + score.getScore());
//            System.out.println("**********************************************************************************");
            scores.add(score);
        }
        logger.info("Sorting and Reversing Score");
        Collections.sort(scores,new Comparator<MatchingScore>(){
            public int compare(MatchingScore score1, MatchingScore score2)
            {
                return  Double.compare(score1.getScore(), score2.getScore());
            }
        });
//        Collections.reverse(scores);

        logger.info("Score Sorted and Reversed Successfully ");

        for( int i = scores.size()-1 ; i >= 0 ;i--)
        {
            if(scores.get(i).getPageId()!=0)
                return scores.get(i) ;
        }

        return null;

    }

    private Node mapToPage(Node node, int pageId, int nodeId){
        try {
            solrHandler.updateRecord(nodeId, node);
        } catch (IOException e) {
            logger.error("IOException: ", e);
//            e.printStackTrace();
        } catch (SolrServerException e) {
            logger.error("SolrServerException: ", e);
//            e.printStackTrace();
        }
//        boolean response =neo4jHandler.assignPageToNode(node.getGeneratedNodeId(), pageId);
//        if (response ==true){
        nodePages.put(node.getGeneratedNodeId(),pageId);
        node.setPageId(pageId);
//        }
//        System.out.println("Node with name " + node.getScientificName() + " is mapped to page "+node.getPageId());
        logger.info("Node: " + node.getScientificName() + " is mapped to Page: " + node.getPageId());
//        fileHandler.writeToFile("Node with name " + node.getScientificName() + " is mapped to page "+node.getPageId());
        return node;
    }

    private void unmappedNode(Node node){
//        System.out.println("New page is created for node named: "+node.getScientificName());
        logger.info("New Page Created for Node: "+node.getScientificName());
//        fileHandler.writeToFile("New page is created for node named: "+node.getScientificName());
        logger.info("Calling assignPageToNode");
        int page_id = neo4jHandler.assignPageToNode(node.getGeneratedNodeId());
        nodePages.put(node.getGeneratedNodeId(),page_id);
        logger.info("Called assignPageToNode");
        logger.info("Node: " + node.getGeneratedNodeId() + "- neither Surrogate nor Virus");
        try {
            logger.info("Adding Document in Solr");
//            long startTime = System.nanoTime();
            solrHandler.addDocument(node, page_id);
//            long endTime = System.nanoTime();
//
//            long duration = (endTime - startTime);
//            System.out.println("duration of adding document in solr: "+ duration);
            logger.info("Added Document in Solr");
        } catch (IOException e) {
//            e.printStackTrace();
            logger.error("IOException: ", e);
        } catch (SolrServerException e) {
//            e.printStackTrace();
            logger.error("SolrServerException: ",  e);
        }
        //  Page newPage = new Page();
       // node.setPageId(newPage.getId());
    }



}
