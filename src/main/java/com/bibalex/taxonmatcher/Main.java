package com.bibalex.taxonmatcher;

import com.bibalex.taxonmatcher.controllers.NodeMapper;
import com.bibalex.taxonmatcher.controllers.RunTaxonMatching;
import com.bibalex.taxonmatcher.handlers.LogHandler;
import com.bibalex.taxonmatcher.handlers.Neo4jHandler;
import com.bibalex.taxonmatcher.handlers.ResourceHandler;
import com.bibalex.taxonmatcher.models.Node;

import java.util.ArrayList;
//import com.bibalex.taxonmatcher.parsers.NewNodesTester;

public class Main {

    public static void main(String[] args) {
	// write your code here


        RunTaxonMatching test = new RunTaxonMatching();
        test.RunTaxonMatching(1);
//        ResourceHandler.initialize("config.properties");
//        LogHandler.initializeHandler();
//        Neo4jHandler neo4jHandler = new Neo4jHandler();
//        NodeMapper nodeMapper = new NodeMapper();
//        nodeMapper.mapAllNodesToPages(neo4jHandler.getRootNodes(1));
//        ArrayList<Integer> e= new ArrayList<Integer>();
//        e.add(20);
//        e.add(1);
//        neo4jHandler.getNodesFromIds(e);


//          neo4jHandler.getsynonyms(3);
//        neo4jHandler.getRootNodes(363);
//        neo4jHandler.getChildren(130);
//        neo4jHandler.getAncestors(20);
//        neo4jHandler.hasChildren(3);
//        neo4jHandler.assignPageToNode(10,3);





//        NodeMapper nm = new NodeMapper();
//        NewNodesTester nnt = new NewNodesTester();
//        nm.mapIfNeeded(nnt.readFile(ResourceHandler.getPropertyValue("newNodesFile")));

//        ArrayList<Node> children = new ArrayList<Node>();
//        String nodeId, int resourceId, String scientificName, int generatedNodeId, String rank, int parentGeneratedNodeId,
//        String parentNodeId, String acceptedNodeId, int acceptedNodeGeneratedId, int pageId

//        =====================================
//        Node child = new Node("t_2", 1, "test2",2,"phylum", 1,"t_1", "t_2", 2, 2);
//        Node parent = new Node("t_1", 1, "test1",1,"kingdom", -1,"", "t_1", 1, 1);
//
//        ArrayList<Node> test = new ArrayList<Node>();
//        test.add(parent);
//
//        child.setParentNodeId(parent.getParentNodeId());
//        child.setParentGeneratedNodeId(parent.getParentGeneratedNodeId());
//        nodeMapper.mapAllNodesToPages(test);
//        ===========================


//        Node anotherChild = new Node(3, 1,1,"test3","testen3","kingdom");
//        Node newChild = new Node(4,1,1,"Parus major Linnaeus, 1788",
//                "Homo sapiens Linnaeus", "kingdom");
//        children.add(child);
//        children.add(anotherChild);
//        children.add(newChild);
//
//        ArrayList<Node> ancestors = new ArrayList<Node>();
//        Node n = new Node(1, 1,0,"test1","testen1","kingdom");
//        ancestors.add(n);
//
//        n.setChildren(children);
//        child.setAncestors(ancestors);
//        NodeMapper nm = new NodeMapper();
//        nm.mapIfNeeded(n);
    }
}
