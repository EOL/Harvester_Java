package org.bibalex.eol.handler;

import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptsHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScriptsHandler.class);

    public void runNeo4jInit() {
//        System.out.println("run neo4j init");
        logger.info("Running");

        try {
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "neo4j_init.sh");
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }

    public void runPreProc(String taxaFilePath, String node_id_col, String parent_id_col, String scientific_name_col, String rank_col) {
//        System.out.println("run preproc");
        logger.info("Calling runPreProc");
        try {
//            System.out.println(taxaFilePath);
//            System.out.println(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_preproc.sh");
//            System.out.println(node_id_col);
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_preproc.sh", taxaFilePath, node_id_col, parent_id_col, scientific_name_col, rank_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }

    public void runGenerateIds(String taxaFilepath, String acceptedNameUsageId_col, String taxonomic_status_col, String parent_col_id, String node_id_col, String has_header, String separator) {
//        System.out.println("run generate ids");
        logger.info("Calling runGeneratedIds");
        try {
//            System.out.println(taxaFilepath);
            logger.info("Taxa File Path: " + taxaFilepath);
//            System.out.println(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_generate_ids.sh");
            logger.info("Script Path: " + PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_generate_ids.sh");
            if(separator.contains("\t")){
                separator = "\\t";
            }
//            System.out.println(separator);
            logger.info("Separator: "+ separator);
            acceptedNameUsageId_col = (acceptedNameUsageId_col!= null && !acceptedNameUsageId_col.equals("0")) ? acceptedNameUsageId_col:"-1";
            taxonomic_status_col = (taxonomic_status_col != null && !taxonomic_status_col.equals("0"))? taxonomic_status_col : "-1";
            parent_col_id = (parent_col_id != null && !parent_col_id.equals("0"))? parent_col_id : "-1";
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_generate_ids.sh", taxaFilepath, acceptedNameUsageId_col, taxonomic_status_col,
                    parent_col_id, node_id_col, has_header, separator);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }

    public void runLoadNodesParentFormat(String taxaFilePath, String resource_id, String node_id_col, String scientific_name_col, String rank_col, String generated_auto_id_col,
                                         String parent_id_col, String has_header, String page_id_col, String is_accepted_col, String accepted_parent_col) {
//        System.out.println("run load nodes");
        logger.info("Calling runLoadNodesParentFormat");
        try {
//            System.out.println(taxaFilePath);
            logger.info("Taxa File Path: " + taxaFilePath);
//            System.out.println(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_with_ids.sh");
            logger.info("Script Path: " + PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_with_ids.sh");

            page_id_col = (page_id_col != null && !page_id_col.equals("0")) ? page_id_col : "-1";
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_with_ids.sh", taxaFilePath, resource_id, node_id_col, scientific_name_col, rank_col, generated_auto_id_col, parent_id_col, has_header, page_id_col, is_accepted_col, accepted_parent_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }

    public void runLoadRelationsParentFormat(String taxaFilePath, String resource_id, String node_id_col, String parent_id_col, String accepted_parent_col) {
        logger.info("Calling runLoadRelationsParentFormat");
        try {
            logger.info("Taxa File Path: " + taxaFilePath);
            logger.info("Script Path: " + PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_relationships.sh");

            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_relationships.sh", taxaFilePath, resource_id, node_id_col, parent_id_col, accepted_parent_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }

    public void runLoadNodesAncestryFormat(String taxaFilePath, String resource_id, String ancestors, String ranks, String node_id_col,
                                           String generated_auto_id_col, String scientific_name_col, String rank_col, String page_id_col,
                                           String is_accepted_col, String accepted_parent_col, String has_header){
        logger.info("runLoadNodesAncestryFormat");

        try {
//            System.out.println(taxaFilePath);
//            System.out.println(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_ancestry.sh");
            logger.info("Taxa File Path: " + taxaFilePath);
            logger.info("Script Path: " + PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_ancestry.sh");

            page_id_col = (page_id_col != null && !page_id_col.equals("0")) ? page_id_col : "-1";
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String) "scriptsPath") + "taxa_load_nodes_ancestry.sh", taxaFilePath, resource_id,
                    ancestors,ranks,node_id_col,generated_auto_id_col,scientific_name_col,rank_col,page_id_col,is_accepted_col,accepted_parent_col,has_header);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("IOException: ", e);
        }
    }
}