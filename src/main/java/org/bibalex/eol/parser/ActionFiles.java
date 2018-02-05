package org.bibalex.eol.parser;

import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveFile;

import java.io.*;
import java.util.*;

public class ActionFiles {

    public static Map<String, Map<String, String>> loadActionFiles(Archive dwcaArchive){
        Map<String, Map<String, String>> pathsAndIDsWithAction = new HashMap<String, Map<String, String>>();
        //TODO call core file
        for (ArchiveFile archiveFile : dwcaArchive.getExtensions()){
            String actionFilePath = archiveFile.getLocationFile()+"_action";
            System.out.println(actionFilePath);
            Map<String, String> IDsActions = new HashMap<String, String>();
            try {
                FileReader fileReader = new FileReader(actionFilePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                while((line = bufferedReader.readLine()) != null){
                    String [] coulmns = line.split(archiveFile.getFieldsTerminatedBy());
                    IDsActions.put(coulmns[0], coulmns[1]);
                }
                pathsAndIDsWithAction.put(archiveFile.getLocation()+"_action", IDsActions);
            } catch (FileNotFoundException e) {
//                e.printStackTrace();
                continue;
            } catch (IOException e) {
//                e.printStackTrace();
                continue;
            }
        }
        System.out.println(pathsAndIDsWithAction);
        return pathsAndIDsWithAction;
    }

    public static void main (String [] args){
        ActionFiles actionFiles= new ActionFiles();
        ArrayList<String> paths = new ArrayList<String>();
        paths.add("/home/ba/eol_resources/1.txt");
        paths.add("/home/ba/eol_resources/2.txt");
        paths.add("/home/ba/eol_resources/3.txt");
        String path = "/home/ba/eol_resources/8";
        Archive archive = null;
        try {
            archive = ArchiveFactory.openArchive(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        actionFiles.loadActionFiles(archive);
//        actionFiles.loadActionFiles(paths, "\t");
    }
}
