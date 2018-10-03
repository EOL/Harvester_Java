package org.bibalex.eol.handler;

import org.bibalex.eol.parser.handlers.PropertiesHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ScriptsHandler {
    public void runNeo4jInit() {
        System.out.println("run neo4j init");
        try {
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String)"scriptsPath") + "neo4j_init.sh");
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runPreProc(String taxaFilePath, String node_id_col, String parent_id_col, String scientific_name_col, String rank_col) {
        System.out.println("run preproc");
        try {
            System.out.println(taxaFilePath);
            System.out.println(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_preproc.sh");
            System.out.println(node_id_col);
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_preproc.sh", taxaFilePath, node_id_col, parent_id_col, scientific_name_col, rank_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runGenerateIds(String taxaFilepath) {
        System.out.println("run generate ids");
        try {
            System.out.println(taxaFilepath);
            System.out.println(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_generate_ids.sh");
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_generate_ids.sh", taxaFilepath);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runLoadNodes(String taxaFilePath, String resource_id, String node_id_col, String scientific_name_col, String rank_col, String generated_auto_id_col, String parent_id_col, String has_header, String page_id_col) {
        System.out.println("run load nodes");
        try {
            System.out.println(taxaFilePath);
            System.out.println(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_load_nodes_with_ids.sh");
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_load_nodes_with_ids.sh", taxaFilePath, resource_id, node_id_col, scientific_name_col, rank_col, generated_auto_id_col, parent_id_col, has_header, page_id_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runLoadRelations(String taxaFilePath, String resource_id, String node_id_col, String parent_id_col) {
        System.out.println("run load relations");
        try {
            ProcessBuilder pb = new ProcessBuilder(PropertiesHandler.getProperty((String)"scriptsPath") + "taxa_load_relationships.sh", taxaFilePath, resource_id, node_id_col, parent_id_col);
            Process p = null;
            p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}