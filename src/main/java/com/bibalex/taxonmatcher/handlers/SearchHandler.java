package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import com.bibalex.taxonmatcher.models.Node;
import com.bibalex.taxonmatcher.models.SearchResult;
import com.bibalex.taxonmatcher.models.Strategy;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import java.util.ArrayList;
import java.util.Collection;

public class SearchHandler {

    private GlobalNamesHandler globalNameHandler;
    private SolrHandler solrHandler;
    private static Logger logger;
    private Neo4jHandler neo4jHandler;
    private NodeHandler nodeHandler;

    public SearchHandler()
    {
        globalNameHandler = new GlobalNamesHandler();
        solrHandler = new SolrHandler();
        logger = LogHandler.getLogger(NodeMapper.class.getName());
        neo4jHandler = new Neo4jHandler();
        nodeHandler = new NodeHandler();
    }

    private String buildSearchQuery(Node node, Strategy strategy, Node ancestor){
        String searchQuery = "";
        String scientificNameAttr = ResourceHandler.getPropertyValue("scientificName");
        if (strategy != null){
            searchQuery += strategy.getIndex();
//            searchQuery += strategy.getType().equalsIgnoreCase("in") ? " : " : " = ";
            searchQuery += ResourceHandler.getPropertyValue("searchqueryColon");
            searchQuery += ResourceHandler.getPropertyValue("searchQueryQuotation");
            if(strategy.getIndex().equalsIgnoreCase(ResourceHandler.getPropertyValue("synonyms"))
                    ||strategy.getIndex().equalsIgnoreCase(ResourceHandler.getPropertyValue("otherSynonyms"))){
               searchQuery +=node.getScientificName();
            }
            else if(strategy.getIndex().equalsIgnoreCase(ResourceHandler.getPropertyValue("canonicalSynonyms"))
                    ||strategy.getIndex().equalsIgnoreCase(ResourceHandler.getPropertyValue("otherCanonicalSynonyms"))){
                searchQuery += globalNameHandler.getCanonicalForm(node.getScientificName());
            }


//            if (strategy.getIndex().equalsIgnoreCase("synonyms")||strategy.getIndex().equalsIgnoreCase("other_synonyms")||
//                    strategy.getIndex().equalsIgnoreCase("canonical_synonyms")||strategy.getIndex().equalsIgnoreCase("other_canonical_synonyms")){
//                ArrayList<Node> synonyms = nodeHandler.nodeMapper(neo4jHandler.getsynonyms(node.getGeneratedNodeId()));
//                if(strategy.getIndex().equalsIgnoreCase("synonyms")||strategy.getIndex().equalsIgnoreCase("canonical_synonyms"))
//                {
//                    searchQuery +="(";
//                    for(Node n:synonyms)
//                    {
//                        if(n.getResourceId()==Integer.valueOf(ResourceHandler.getPropertyValue("DWHId")))
//                        {
//                            searchQuery += '"';
//                            searchQuery += strategy.getAttribute().equalsIgnoreCase(scientificNameAttr) ?
//                                    n.getScientificName() : globalNameHandler.getCanonicalForm(n.getScientificName());
//                            searchQuery += '"';
//
//                        }
//
//                    }
//                    searchQuery +=")";
//
//                }
//                else
//                {
//                    searchQuery +="(";
//                    for(Node n:synonyms)
//                    {
//
//                        if(n.getResourceId()!=Integer.valueOf(ResourceHandler.getPropertyValue("DWHId")))
//                        {
//                            searchQuery += '"';
//                            searchQuery += strategy.getAttribute().equalsIgnoreCase(scientificNameAttr) ?
//                                    n.getScientificName() : globalNameHandler.getCanonicalForm(n.getScientificName());
//                            searchQuery += '"';
//
//                        }
//
//                    }
//                    searchQuery +=")";
//                }
//            }
            else{
                searchQuery += strategy.getAttribute().equalsIgnoreCase(scientificNameAttr) ?
                        node.getScientificName() : globalNameHandler.getCanonicalForm(node.getScientificName());
            }
            searchQuery += ResourceHandler.getPropertyValue("searchQueryQuotation");
            //not finalized
            //case search by canonical name
            //if next strategy == null search by canonical and ancestor at that depth
//            if (ancestor != null && !strategy.getAttribute().equalsIgnoreCase(scientificNameAttr)){
//                //case other ancestor will not be valid in the code
//                searchQuery += " AND ancestors_ids : " + ancestor.getGeneratedNodeId();
//            }
            if(globalNameHandler.isHybrid(node.getScientificName())){
                searchQuery += " AND is_hybrid : True";
            }
        }
        System.out.println("=======================================");
        logger.info("Search query is: " + searchQuery);
        System.out.println("========================================");
        return searchQuery;
    }

//    private ArrayList<Integer> splitAndConvert(String solrField){
//        return new ArrayList(Arrays.asList(solrField.split("\\|")));
//    }

    public ArrayList<SearchResult> getResults(Node node, Strategy strategy, Node ancestor){
        ArrayList<Integer> children = new ArrayList<Integer>();
        ArrayList<Integer> ancestors = new ArrayList<Integer>();
        logger.info("before build search query");
        String searchQuery = buildSearchQuery(node, strategy, ancestor);
        logger.info(" after build search query");
        logger.info(" before performing query");
        logger.info("before adding document in solr");
//        long startTime = System.nanoTime();
        SolrDocumentList solrResultDocuments = solrHandler.performQuery(searchQuery);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
//        System.out.println("duration of performing query in solr: "+ duration);
        logger.info(" after performing query");
        System.out.println("*********" + solrResultDocuments.toString());
        ArrayList<SearchResult> results = new ArrayList<SearchResult>();
        for(SolrDocument document : solrResultDocuments){
            if(document.getFieldValues(ResourceHandler.getPropertyValue("childrenIDS")) != null) {
                children = castObjectsCollectionToIntegerList(document
                        .getFieldValues(ResourceHandler.getPropertyValue("childrenIDS")));
            }
            if(document.getFieldValues(ResourceHandler.getPropertyValue("ancestorsIDS")) != null) {
                ancestors = castObjectsCollectionToIntegerList(document
                        .getFieldValues(ResourceHandler.getPropertyValue("ancestorsIDS")));
            }

            int pageId = document.getFieldValue("page_id") == null? 0:Integer.parseInt(document
                    .getFieldValue(ResourceHandler.getPropertyValue("pageId")).toString());
            String scientific_name = document.getFieldValue(ResourceHandler.getPropertyValue("scientificName")) == null
                    ? String.valueOf(document.getFieldValue(ResourceHandler.getPropertyValue("otherScientificName")))
                    : String.valueOf(document.getFieldValue(ResourceHandler.getPropertyValue("scientificName")));
            SearchResult result = new SearchResult(Integer.parseInt(document.getFieldValue(ResourceHandler.getPropertyValue("ID"))
                    .toString()), pageId, children, ancestors, scientific_name);
            results.add(result);
        }
        return results;
    }

    private ArrayList<Integer> castObjectsCollectionToIntegerList(Collection<Object> childrenCollection){
        ArrayList<Integer> children = new ArrayList<Integer>();
        for(Object child : childrenCollection){
            String child_string = child.toString();
            children.add(Integer.valueOf(child_string));
//            children.add((Integer) child);
        }
        return children;
    }

//    private ArrayList<String> mapChildrenNames(Collection<Object> children){
//        ArrayList<String> childrenNames = new ArrayList<String>();
//        for(Object object : children){
//            childrenNames.add(object != null ? object.toString() : null);
//        }
//        return childrenNames;
//    }

    public static void main (String []args){
        ResourceHandler.initialize("configs.properties");
        LogHandler.initializeHandler();
        SearchHandler searchHandler = new SearchHandler();
        Node node = new Node( "2", 1,  "tiger",  2,  "family",5,
         "5",  "6",  6,  -1, -1, -1);
        Strategy strategy = new Strategy("scientific_name", "synonyms", "eq");
        Node ancestor = new Node( "2", 1,  "tiger",  2,  "family",5,
                "5",  "6",  6,  -1, -1, -1);
        searchHandler.getResults(node, strategy, ancestor);
    }
}
