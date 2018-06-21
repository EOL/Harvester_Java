package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;

/**
 * Created by Amr.Morad
 */
public class SolrHandler {

    private String zkHostString;
    private String defaultCollection;
    private CloudSolrClient solr;
    private static Logger logger;

    public SolrHandler(){
        zkHostString = ResourceHandler.getPropertyValue("zookeeperHost");
        defaultCollection = ResourceHandler.getPropertyValue("defaultCollection");
        solr = new CloudSolrClient(zkHostString);
//        solr= new CloudSolrClient.Builder().withZkHost(zkHostString).build();
        solr.setDefaultCollection(defaultCollection);
        logger = LogHandler.getLogger(NodeMapper.class.getName());
    }

    public SolrDocumentList performQuery(String queryString){
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

    public void commitDocument(SolrInputDocument doc){
        try {
            solr.add(doc);
            solr.commit();
        } catch (SolrServerException e) {
            logger.error("SolrServerException in commit document " + e.getStackTrace());
        } catch (IOException e) {
            logger.error("IOException in commit document " + e.getStackTrace());
        }
    }



    public static void main(String [] args){
        SolrHandler sh = new SolrHandler();

    }

}
