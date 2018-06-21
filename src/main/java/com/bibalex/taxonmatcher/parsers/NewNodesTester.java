//package com.bibalex.taxonmatcher.parsers;
//
//import com.bibalex.taxonmatcher.handlers.ResourceHandler;
//import com.bibalex.taxonmatcher.models.Node;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//
///**
// * Created by Amr.Morad
// */
//public class NewNodesTester {
//
//    private int limit;
//
//    public NewNodesTester(){
//        limit = Integer.parseInt(ResourceHandler.getPropertyValue("fileLimit"));
//    }
//
//    public Node readFile(String filePath){
//        System.out.println("Read file");
//        BufferedReader bReader = null;
//        ArrayList<String> lines = new ArrayList<String>();
//        String line;
//        int i = 0;
//
//        try {
//            bReader = new BufferedReader(new FileReader(filePath));
//            //skip first line
//            bReader.readLine();
//            while ((line = bReader.readLine()) != null) {
//                lines.add(line);
//                if (i > limit){
//                    parseTabSeparatedLines(lines);
//                    lines = new ArrayList<String>();
//                    i = 0;
//                }
//            }
//            bReader.close();
//            return parseTabSeparatedLines(lines);
//        } catch (java.io.IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
////    private Node parseTabSeparatedLines(ArrayList<String> lines){
//////        System.out.println("parse tab sepaerated");
//////        int i = 0;
////////        for (String line : lines){
//////        String line = lines.get(0);
//////        String [] splittedLine = line.split("\t", -1);
//////            for(String k : splittedLine){
//////                System.out.print(k + " ");
//////            }
//////            System.out.println();
//////            //String id, String scientificName, String rank, ArrayList<String> synonyms, String kingdom,
//////            //String family, String genus, ArrayList<String> ancestors, ArrayList<String> children
//////
////////            Node testNode = new Node(splittedLine[0], splittedLine[1], splittedLine[2], new ArrayList(Arrays.asList(
////////                    splittedLine[3].split("\\|"))), splittedLine[4], splittedLine[5], splittedLine[6],
////////                    new ArrayList(Arrays.asList(splittedLine[7].split("\\|"))), new ArrayList(Arrays.asList(
////////                            splittedLine[8].split("\\|"))));
//////            return testNode;
//////        }
////    }
//
//
//}
