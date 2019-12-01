package com.bibalex.taxonmatcher.handlers;

import com.bibalex.taxonmatcher.models.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bibalex.eol.parser.handlers.PropertiesHandler;

import java.util.*;

/**
 * Created by Amr.Morad on 3/9/2017.
 */
public class NodeHandler {

    public Node matchedAncestor(ArrayList<Node> ancestors, int depth, HashMap<Integer,Integer> nodesPages, int resourceId){
        System.out.println("matched ancestor");
        System.out.println(depth);
        int i = 0;
        if (!ancestors.isEmpty() && resourceId == Integer.valueOf(ResourceHandler.getPropertyValue("DWHId"))) {
            for (Node ancestor : ancestors) {
                if ((ancestor.getPageId() != 0 || nodesPages.get(ancestor.getGeneratedNodeId()) != null)&& i >= depth )
                    return ancestor;
                i++;
            }
        }
        System.out.println("matched ancestor: will return null");
        return null;
    }

    public ArrayList<Node> nativeVirus(){
        //TODO: implement native virus from DWH
        return new Neo4jHandler().getNativeVirusNode();
//        return new Node(1,1,9, "virus", "viruseen", "kingdom");
//        return new Node("1", "Virus", "kingdom", "viruseen", "viruses", "virussss");

    }

    public ArrayList<Node> nodeMapper(ArrayList<Node> beforeMapping)
    {
        ArrayList<Node> afterMapping = new ArrayList<Node>();
        ObjectMapper mapper = new ObjectMapper();
        for(int i = 0 ; i < beforeMapping.size(); i++)
        {
            Node n = mapper.convertValue(beforeMapping.get(i), Node.class);
            afterMapping.add(n);
        }
        return afterMapping;
    }

    public ArrayList<Integer> getPagesOfAncestors(ArrayList<Node> nodeAncestors, HashMap<Integer, Integer> nodePages){
        ArrayList<Integer> nodeAncestorsPages = new ArrayList<>();
        for (Node n : nodeAncestors) {
            if (n.getPageId() != 0 ) {
                nodeAncestorsPages.add(n.getPageId());
            }
            else if (nodePages.get(n.getGeneratedNodeId()) != null){
                nodeAncestorsPages.add(nodePages.get(n.getGeneratedNodeId()));
            }
        }
        return nodeAncestorsPages;
    }

}
