package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amr Morad
 */
public class Neo4jHandler {

    public ArrayList<Node> getChildren(int generatedNodeId){
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getChildren"), generatedNodeId,"generatedNodeId", null , null);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println("returned children nodes " + response);
        System.out.println("===============================");
        ArrayList<Node> children = (ArrayList<Node>) response;
        return children;
    }

    public boolean hasChildren(int generatedNodeId){
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("hasChildren"), generatedNodeId,"generatedNodeId", null , null);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println("has children ? " + response);
        System.out.println("===============================");
        boolean has_children = ((Boolean) response).booleanValue();
        return has_children;
    }

    public ArrayList<Node> getAncestors(int generatedNodeId){
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getAncestors"), generatedNodeId,"generatedNodeId" , null, null);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println("returned ancestors nodes " + response);
        System.out.println("===============================");
        ArrayList<Node> ancestors = (ArrayList<Node>) response;
        return ancestors;
    }

    public int assignPageToNode(int generatedNodeId){
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("createPageIdtoNode"), generatedNodeId,"generatedNodeId",null,null );
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println("created page id " + Integer.valueOf(response.toString())+" to node id "+generatedNodeId+" ? "+ response);
        System.out.println("===============================");
        return Integer.valueOf(response.toString());
    }

    public boolean assignPageToNode(int generatedNodeId, int pageId){
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("addPageIdtoNode"), generatedNodeId,"generatedNodeId" , pageId, "pageId");
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println("assigned page id " + pageId +" to node id "+generatedNodeId+" ? "+ response);
        System.out.println("===============================");
        boolean flag = ((Boolean) response).booleanValue();
        return flag;
    }

    public boolean assignPageToNodes(HashMap<Integer, Integer> nodesPages){
        if (nodesPages.size()>0){
//            long startTime = System.nanoTime();
            Object response = RestClientHandler.doConnectionPost(ResourceHandler.getPropertyValue("addPagesToNodes"), nodesPages);
//            long endTime = System.nanoTime();
//            long duration = (endTime - startTime);
            System.out.println("===============================");
            System.out.println("assigned pages to nodes: "+ response);
            System.out.println("===============================");
            boolean flag = ((Boolean) response).booleanValue();
            return flag;
        }
        else return false;
    }

    public ArrayList<Node> getNativeVirusNode(){
        //TODO implement
//        return new Node(5006, "Viruses");
//        long startTime = System.nanoTime();
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getNativeVirusNode"),null,null,null,null);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
        System.out.println("===============================");
        System.out.println(" " + response);
        System.out.println("===============================");
        ArrayList<Node>  Nodes = (ArrayList<Node>)response;
        return  Nodes ;
    }

    public ArrayList<Node> getNodesFromIds(ArrayList<Integer> ids){
        if (ids.size()>0){
//            long startTime = System.nanoTime();
            Object response = RestClientHandler.doConnectionPost(ResourceHandler.getPropertyValue("getNodes"), ids);
//            long endTime = System.nanoTime();
//            long duration = (endTime - startTime);
            System.out.println("===============================");
            System.out.println("returned nodes using ids "+ response);
            System.out.println("===============================");
            ArrayList<Node>  Nodes = (ArrayList<Node>)response;
            return  Nodes ;
        }
        else return new ArrayList<Node>();
    }

    public ArrayList<Node> getRootNodes(int resourceId){
            long startTime = System.nanoTime();
            Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getRootNodes"), resourceId, "resourceId" , null , null);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("===============================");
            System.out.println("returned root nodes " + response+" and duration : " +duration);
            System.out.println("===============================");
            ArrayList<Node> roots = (ArrayList<Node>) response;
        return roots;
    }

    public ArrayList<Node> getsynonyms(int generatedNodeId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getSynonyms"), generatedNodeId,"generatedNodeId" , null, null);
        System.out.println("===============================");
        System.out.println("returned synonyms nodes " + response);
        System.out.println("===============================");
        ArrayList<Node>synonyms = (ArrayList<Node>) response;
        return synonyms;
    }

    public static ArrayList<JSONObject> getJSonObject(int[] generatedNodeIds)  {
//        long startTime = System.nanoTime();
        ArrayList<JSONObject> returnedJSon = RestClientHandler.httpConnect(ResourceHandler.getPropertyValue("getNodesjson"),generatedNodeIds);
//        long endTime = System.nanoTime();
//        long duration = (endTime - startTime);
//        System.out.println("returned json and duration : " +duration);
        return returnedJSon;
    }


}
