package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import java.util.ArrayList;

/**
 * Created by Amr Morad
 */
public class Neo4jHandler {

    public ArrayList<Node> getChildren(int generatedNodeId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getChildren"), generatedNodeId,"generatedNodeId", null , null);
        System.out.println("===============================");
        System.out.println("returned children nodes " + response);
        System.out.println("===============================");
        ArrayList<Node> children = (ArrayList<Node>) response;
        return children;
    }

    public boolean hasChildren(int generatedNodeId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("hasChildren"), generatedNodeId,"generatedNodeId", null , null);
        System.out.println("===============================");
        System.out.println("has children ? " + response);
        System.out.println("===============================");
        boolean has_children = ((Boolean) response).booleanValue();
        return has_children;
    }

    public ArrayList<Node> getAncestors(int generatedNodeId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getAncestors"), generatedNodeId,"generatedNodeId" , null, null);
        System.out.println("===============================");
        System.out.println("returned ancestors nodes " + response);
        System.out.println("===============================");
        ArrayList<Node> ancestors = (ArrayList<Node>) response;
        return ancestors;
    }

    public int assignPageToNode(int generatedNodeId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("createPageIdtoNode"), generatedNodeId,"generatedNodeId",null,null );
        System.out.println("===============================");
        System.out.println("created page id " + Integer.valueOf(response.toString())+" to node id "+generatedNodeId+" ? "+ response);
        System.out.println("===============================");
        return Integer.valueOf(response.toString());
    }

    public boolean assignPageToNode(int generatedNodeId, int pageId){
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("addPageIdtoNode"), generatedNodeId,"generatedNodeId" , pageId, "pageId");
        System.out.println("===============================");
        System.out.println("assigned page id " + pageId +" to node id "+generatedNodeId+" ? "+ response);
        System.out.println("===============================");
        boolean flag = ((Boolean) response).booleanValue();
        return flag;
    }

    public ArrayList<Node> getNativeVirusNode(){
        //TODO implement
//        return new Node(5006, "Viruses");
        Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getNativeVirusNode"),null,null,null,null);
        System.out.println("===============================");
        System.out.println(" " + response);
        System.out.println("===============================");
        ArrayList<Node>  Nodes = (ArrayList<Node>)response;
        return  Nodes ;
    }

    public ArrayList<Node> getNodesFromIds(ArrayList<Integer> ids){
        if (ids.size()>0){
        Object response = RestClientHandler.doConnectionPost(ResourceHandler.getPropertyValue("getNodes"), ids);
        System.out.println("===============================");
        System.out.println("returned nodes using ids "+ response);
        System.out.println("===============================");
        ArrayList<Node>  Nodes = (ArrayList<Node>)response;
        return  Nodes ;
    }
        else return new ArrayList<Node>();
    }

    public ArrayList<Node> getRootNodes(int resourceId){
            Object response = RestClientHandler.doConnectionGet(ResourceHandler.getPropertyValue("getRootNodes"), resourceId, "resourceId" , null , null);
            System.out.println("===============================");
            System.out.println("returned root nodes " + response);
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

}
