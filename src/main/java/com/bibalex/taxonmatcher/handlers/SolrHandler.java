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

/**
 * Created by Amr.Morad
 */
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
        String zkHosts = "localhost:9983";
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
//        int resource_id = neo4jSolr.getInt(obj, "resource id");
        int resource_id = node.getResourceId();
        String scientificName = neo4jSolr.getString(obj, "scientific name");
        String rank = neo4jSolr.getString(obj, "Rank");
        String canonicalName = neo4jSolr.getString(obj, "canonical name");
        boolean is_hybrid = neo4jSolr.isHybrid(obj);
        ArrayList<String> canonicalSynonyms = neo4jSolr.getStringArray(obj, "canonical synonyms");
        ArrayList<String> otherCanonicalSynonyms = neo4jSolr.getStringArray(obj, "other canonical synonyms");
        ArrayList<String> synonyms = neo4jSolr.getStringArray(obj, "synonyms");
        ArrayList<String> otherSynonyms = neo4jSolr.getStringArray(obj, "other synonyms");
        ArrayList<String> childrenIds = neo4jSolr.getStringArray(obj, "children IDS");
        ArrayList<Integer> ancestorsIds = neo4jSolr.getIntegerArray(obj, "ancestors IDS");

        openConnection("indexer");
        SolrInputDocument doc = new SolrInputDocument();

        if (resource_id == Integer.valueOf(ResourceHandler.getPropertyValue("DWHId"))) {
            if (!scientificName.equals("")) {
                doc.addField("scientific_name", scientificName);
            }
            if (!canonicalName.equals("")) {
                doc.addField("canonical_name", canonicalName);
            }
            doc.addField("canonical_synonyms", canonicalSynonyms);
            doc.addField("synonyms", synonyms);
            doc.addField("children_ids", childrenIds);
            doc.addField("ancestors_ids", ancestorsIds);
            doc.addField("other_canonical_synonyms", otherCanonicalSynonyms);
            doc.addField("other_synonyms", otherSynonyms);
        }
        else{
            doc.addField("other_canonical_synonyms", canonicalSynonyms);
            doc.addField("other_synonyms", synonyms);
        }

        doc.addField("id", node.getGeneratedNodeId());
        if (pageId != -1) {
            doc.addField("page_id", pageId);
        }
        if (!rank.equals("")) {
            doc.addField("rank", rank);
        }
        if (String.valueOf(is_hybrid) != null) {
            doc.addField("is_hybrid", is_hybrid);
        }



        logger.info("new added doc: " + doc);
        client.add(doc);
        client.close();

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


        openConnection("indexer");
        SolrInputDocument doc = new SolrInputDocument();

        SolrQuery q = new SolrQuery("id:" + generatedNodeId);
        QueryResponse r = client.query(q);

        SolrDocument oldDoc = r.getResults().get(0);
        doc.addField("id", generatedNodeId);

        //scientificName is empty string canonical will be empty string also and wo,t be inserted in  solr
        //pageId is -1 won't be inserted in solr
        //rank if empty string won't be inserted in solr
        //update other canonical synonyms and other synonyms include adding old one too
        //update canonical synonyms and synonyms not include adding old one

        if (oldDoc.getFieldValues("scientific_name") != null) {
            doc.addField("scientific_name", neo4jSolr.mapToDoc(oldDoc.getFieldValues("scientific_name")));
        }

        if (oldDoc.getFieldValues("rank") != null) {
            doc.addField("rank", neo4jSolr.mapToDoc(oldDoc.getFieldValues("rank")));
        }

        if (oldDoc.getFieldValues("page_id") != null) {
            doc.addField("page_id", neo4jSolr.mapToDoc(oldDoc.getFieldValues("page_id")));
        }

        if (oldDoc.getFieldValues("canonical_name") != null) {
            doc.addField("canonical_name", neo4jSolr.mapToDoc(oldDoc.getFieldValues("canonical_name")));
        }

        if (String.valueOf(oldDoc.getFieldValues("is_hybrid")) != null) {
            doc.addField("is_hybrid", neo4jSolr.mapToDoc(oldDoc.getFieldValues("is_hybrid")));
        }

        if (canonicalSynonyms.size() > 0 || oldDoc.getFieldValues("canonical_synonyms") != null) {
            if (oldDoc.getFieldValues("canonical_synonyms") != null) {
                ArrayList oldCanonicalSynonyms = (ArrayList) oldDoc.getFieldValues("canonical_synonyms");
                canonicalSynonyms.addAll(oldCanonicalSynonyms);
            }
            doc.addField("canonical_synonyms", neo4jSolr.mapToDoc(canonicalSynonyms));
        }

        if (otherCanonicalSynonyms.size() > 0 || oldDoc.getFieldValues("other_canonical_synonyms") != null) {
            if (oldDoc.getFieldValues("other_canonical_synonyms") != null) {
                ArrayList oldOtherCanonicalSynonyms = (ArrayList) oldDoc.getFieldValues("other_canonical_synonyms");
                otherCanonicalSynonyms.addAll(oldOtherCanonicalSynonyms);
            }
            doc.addField("other_canonical_synonyms", neo4jSolr.mapToDoc(otherCanonicalSynonyms));
        }

        if (synonyms.size() > 0 || oldDoc.getFieldValues("synonyms") != null) {
            if (oldDoc.getFieldValues("synonyms") != null) {
                ArrayList oldSynonyms = (ArrayList) oldDoc.getFieldValues("synonyms");
                synonyms.addAll(oldSynonyms);
            }
            doc.addField("synonyms", neo4jSolr.mapToDoc(synonyms));
        }

        if (otherSynonyms.size() > 0 || oldDoc.getFieldValues("other_synonyms") != null) {
            if (oldDoc.getFieldValues("other_synonyms") != null) {
                ArrayList oldOtherSynonyms = (ArrayList) oldDoc.getFieldValues("other_synonyms");
                otherSynonyms.addAll(oldOtherSynonyms);
            }
            doc.addField("other_synonyms", neo4jSolr.mapToDoc(otherSynonyms));
        }

        if (oldDoc.getFieldValues("children_ids") != null) {
            doc.addField("children_ids", neo4jSolr.mapToDoc(oldDoc.getFieldValues("children_ids")));
        }

        if (oldDoc.getFieldValues("ancestors_ids") != null) {
            doc.addField("ancestors_ids", neo4jSolr.mapToDoc(oldDoc.getFieldValues("ancestors_ids")));
        }

        client.add(doc);
        client.close();

    }

    public static void main(String[] args) {
        ResourceHandler.initialize("configs.properties");
        LogHandler.initializeHandler();
        SolrHandler sh = new SolrHandler();
        Node node = new Node("2", 2, "other tiger synonym", 485, "family", 5,
                "5", "6", 6, -1, -1, -1);
        try {
            sh.addDocument(node, 5);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
    }

}
