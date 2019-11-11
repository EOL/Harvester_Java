package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import com.bibalex.taxonmatcher.models.Node;
import com.bibalex.taxonmatcher.util.Neo4jSolr;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class SolrHandler {

    private String zkHostString;
    private String defaultCollection;
    private CloudSolrClient solr;
    private static Logger logger;
    CloudSolrClient client = null;
    private GlobalNamesHandler globalNameHandler;

    public SolrHandler() {
        zkHostString = ResourceHandler.getPropertyValue("zookeeperHost");
        defaultCollection = ResourceHandler.getPropertyValue("defaultCollection");
        solr = new CloudSolrClient(zkHostString);
//        solr= new CloudSolrClient.Builder().withZkHost(zkHostString).build();
        solr.setDefaultCollection(defaultCollection);
        globalNameHandler = new GlobalNamesHandler();
        logger = LogHandler.getLogger(NodeMapper.class.getName());
        openConnection(defaultCollection);
    }

    public SolrDocumentList performQuery(String queryString) {
        SolrQuery query = new SolrQuery();

        query.setQuery(queryString);
        try {
            return solr.query(query).getResults();
        } catch (SolrServerException e) {
            logger.error("SolrServerException in performing query exception " + e.getStackTrace());
        } catch (IOException e) {
            logger.error("IOException in performing query " + e.getStackTrace());
        }
        return null;
    }

    public void commitDocument(SolrInputDocument doc) {
        try {
            solr.add(doc);
            solr.commit();
        } catch (SolrServerException e) {
            logger.error("SolrServerException in commit document " + e.getStackTrace());
        } catch (IOException e) {
            logger.error("IOException in commit document " + e.getStackTrace());
        }
    }

    public CloudSolrClient openConnection(String collectionName) {
        String zkHosts = ResourceHandler.getPropertyValue("zookeeperHost");
        client = new CloudSolrClient.Builder().withZkHost(zkHosts).build();
        client.setDefaultCollection(collectionName);
        return client;
    }

    public void addDocument(Node node, int pageId) throws IOException, SolrServerException {
        int[] generatedNodeIds = new int[1];
        generatedNodeIds[0] = node.getGeneratedNodeId();
        ArrayList<JSONObject> returnedJson = Neo4jHandler.getJSonObject(generatedNodeIds);
        JSONObject obj = returnedJson.get(0);
        Neo4jSolr neo4jSolr = new Neo4jSolr();
        int resource_id = neo4jSolr.getInt(obj,
                ResourceHandler.getPropertyValue("neo4jResourceID"));
//        int resource_id = node.getResourceId();
        String scientificName = neo4jSolr.getString(obj,
                ResourceHandler.getPropertyValue("neo4jScientificName"));
        String rank = neo4jSolr.getString(obj,
                ResourceHandler.getPropertyValue("neo4jRank"));
        String canonicalName = neo4jSolr.getString(obj,
                ResourceHandler.getPropertyValue("neo4jCanonicalName"));
        boolean is_hybrid = neo4jSolr.isHybrid(obj);
        ArrayList<String> canonicalSynonyms = neo4jSolr.getStringArray(obj,
                ResourceHandler.getPropertyValue("neo4jCanonicalSynonyms"));
        ArrayList<String> otherCanonicalSynonyms = neo4jSolr.getStringArray(obj,
                ResourceHandler.getPropertyValue("neo4jOtherCanonicalSynonyms"));
        ArrayList<String> synonyms = neo4jSolr.getStringArray(obj,
                ResourceHandler.getPropertyValue("neo4jSynonyms"));
        ArrayList<String> otherSynonyms = neo4jSolr.getStringArray(obj,
                ResourceHandler.getPropertyValue("neo4jOtherSynonyms"));
        ArrayList<String> childrenIds = neo4jSolr.getStringArray(obj,
                ResourceHandler.getPropertyValue("neo4jChildrenIDS"));
        ArrayList<Integer> ancestorsIds = neo4jSolr.getIntegerArray(obj,
                ResourceHandler.getPropertyValue("neo4jAncestorsIDS"));
//        openConnection("indexer");
        SolrInputDocument doc = new SolrInputDocument();

        if (resource_id == Integer.valueOf(ResourceHandler.getPropertyValue("DWHId"))) {
            if (!scientificName.equals("")) {
                doc.addField(ResourceHandler.getPropertyValue("scientificName"), scientificName);
            }
            if (!canonicalName.equals("")) {
                doc.addField(ResourceHandler.getPropertyValue("canonicalName"), canonicalName);
            }
            doc.addField(ResourceHandler.getPropertyValue("canonicalSynonyms"), canonicalSynonyms);
            doc.addField(ResourceHandler.getPropertyValue("synonyms"), synonyms);
            doc.addField(ResourceHandler.getPropertyValue("childrenIDS"), childrenIds);
            doc.addField(ResourceHandler.getPropertyValue("ancestorsIDS"), ancestorsIds);
            doc.addField(ResourceHandler.getPropertyValue("otherCanonicalSynonyms"), otherCanonicalSynonyms);
            doc.addField(ResourceHandler.getPropertyValue("otherSynonyms"), otherSynonyms);
        }
        else{
            doc.addField(ResourceHandler.getPropertyValue("otherScientificName"), scientificName);
            doc.addField(ResourceHandler.getPropertyValue("otherCanonicalSynonyms"), canonicalSynonyms);
            doc.addField(ResourceHandler.getPropertyValue("otherSynonyms"), synonyms);
        }

        doc.addField(ResourceHandler.getPropertyValue("ID"), node.getGeneratedNodeId());
        if (pageId != -1) {
            doc.addField(ResourceHandler.getPropertyValue("pageID"), pageId);
        }
        if (!rank.equals("")) {
            doc.addField(ResourceHandler.getPropertyValue("rank"), rank);
        }
        if (String.valueOf(is_hybrid) != null) {
            doc.addField(ResourceHandler.getPropertyValue("isHybrid"), is_hybrid);
        }
        logger.info("new added doc: " + doc);
        client.add(doc);
        client.commit();
//        client.close();

    }


    public void updateRecord(int generatedNodeId, Node node) throws IOException, SolrServerException {
        Neo4jSolr neo4jSolr = new Neo4jSolr();
        ArrayList<String> synonyms = new ArrayList<>();
        ArrayList<String> canonicalSynonyms = new ArrayList<>();
        ArrayList<String> otherCanonicalSynonyms = new ArrayList<>();
        ArrayList<String> otherSynonyms = new ArrayList<>();

        if (node.getResourceId() == Integer.valueOf(ResourceHandler.getPropertyValue("DWHId"))) {
            synonyms.add(node.getScientificName());
            canonicalSynonyms.add(globalNameHandler.getCanonicalForm(node.getScientificName()));
        } else {
            otherSynonyms.add(node.getScientificName());
            otherCanonicalSynonyms.add(globalNameHandler.getCanonicalForm(node.getScientificName()));
        }

//        openConnection("indexer");
        SolrInputDocument doc = new SolrInputDocument();

//        SolrQuery q = new SolrQuery("id:" + generatedNodeId);
        String query =  ResourceHandler.getPropertyValue("ID") + ResourceHandler.getPropertyValue("searchqueryColon") ;
        SolrQuery q = new SolrQuery(query + generatedNodeId);
        QueryResponse r = client.query(q);
        SolrDocument oldDoc = r.getResults().get(0);
        doc.addField(ResourceHandler.getPropertyValue("ID"), generatedNodeId);

        //scientificName is empty string canonical will be empty string also and won't be inserted in  solr
        //pageId is -1 won't be inserted in solr
        //rank if empty string won't be inserted in solr
        //update other canonical synonyms and other synonyms include adding old one too
        //update canonical synonyms and synonyms not include adding old one

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("scientificName")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("scientificName"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("scientificName"))));
        }

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("rank")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("rank"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("rank"))));
        }

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("pageID")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("pageID"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("pageID"))));
        }

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("canonicalName")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("canonicalName"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("canonicalName"))));
        }

        if (String.valueOf(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("isHybrid"))) != null) {
            doc.addField(ResourceHandler.getPropertyValue("isHybrid"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("isHybrid"))));
        }

        if (canonicalSynonyms.size() > 0 || oldDoc.getFieldValues(ResourceHandler.getPropertyValue("canonicalSynonyms")) != null) {
            if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("canonicalSynonyms")) != null) {
                ArrayList oldCanonicalSynonyms = (ArrayList) oldDoc.getFieldValues(ResourceHandler.getPropertyValue("canonicalSynonyms"));
                canonicalSynonyms.addAll(oldCanonicalSynonyms);
            }
            doc.addField(ResourceHandler.getPropertyValue("canonicalSynonyms"), neo4jSolr.mapToDoc(canonicalSynonyms));
        }

        if (otherCanonicalSynonyms.size() > 0 || oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherCanonicalSynonyms")) != null) {
            if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherCanonicalSynonyms")) != null) {
                ArrayList oldOtherCanonicalSynonyms = (ArrayList) oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherCanonicalSynonyms"));
                otherCanonicalSynonyms.addAll(oldOtherCanonicalSynonyms);
            }
            doc.addField(ResourceHandler.getPropertyValue("otherCanonicalSynonyms"), neo4jSolr.mapToDoc(otherCanonicalSynonyms));
        }

        if (synonyms.size() > 0 || oldDoc.getFieldValues(ResourceHandler.getPropertyValue("synonyms")) != null) {
            if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("synonyms")) != null) {
                ArrayList oldSynonyms = (ArrayList) oldDoc.getFieldValues(ResourceHandler.getPropertyValue("synonyms"));
                synonyms.addAll(oldSynonyms);
            }
            doc.addField(ResourceHandler.getPropertyValue("synonyms"), neo4jSolr.mapToDoc(synonyms));
        }

        if (otherSynonyms.size() > 0 || oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherSynonyms")) != null) {
            if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherSynonyms")) != null) {
                ArrayList oldOtherSynonyms = (ArrayList) oldDoc.getFieldValues(ResourceHandler.getPropertyValue("otherSynonyms"));
                otherSynonyms.addAll(oldOtherSynonyms);
            }
            doc.addField(ResourceHandler.getPropertyValue("otherSynonyms"), neo4jSolr.mapToDoc(otherSynonyms));
        }

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("childrenIDS")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("childrenIDS"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("childrenIDS"))));
        }

        if (oldDoc.getFieldValues(ResourceHandler.getPropertyValue("ancestorsIDS")) != null) {
            doc.addField(ResourceHandler.getPropertyValue("ancestorsIDS"),
                    neo4jSolr.mapToDoc(oldDoc.getFieldValues(ResourceHandler.getPropertyValue("ancestorsIDS"))));
        }

        client.add(doc);
        client.commit();
//        client.close();

    }

    public static void main(String[] args) {
        ResourceHandler.initialize("configs.properties");
        LogHandler.initializeHandler();
        SolrHandler sh = new SolrHandler();
        Node node = new Node("2", 2, "other tiger synonym", 2855108, "family",
                5, "5", "6", 6, -1, -1, -1);
        try {
//            sh.addDocument(node, 7);
            sh.updateRecord(2855108,node);
            System.out.println("without close");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

}
