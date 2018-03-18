package com.deltacalculator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gbif.dwca.io.ArchiveFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.deltacalculator.DeltaCalculator.DWCADiff;


public class ActionFile {
    public enum Action {
        Update,
        Insert,
        Delete,
        Unchanged
    }


    Map<Action, String> actionMap = loadActionMap();
    private static final Logger logger = Logger.getLogger(ActionFile.class);

    File writeLineToActionFile(ArchiveFile diffFile, String recordId, String delimiter, String actionIndicator) {
        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();
        File actionFile = new File(DWCADiff + "/" + diffFile.getTitle() + "_action");
        try {
            if (!actionFile.exists()) {
                actionFile.createNewFile();
                System.out.println("Action File Name: " + actionFile.getPath());
                logger.info("Action File Name: " + actionFile.getPath());
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(actionFile, true));
            String record = recordId + delimiter + actionIndicator + "\n";
            bufferedWriter.write(record);
            archiveFileHandler.commit(bufferedWriter);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return actionFile;
    }

    String getRecordId(String record, String delimiter, int[] sortingColumnIndex) {
        int arraySize = sortingColumnIndex.length;
        String recordId = "";
        if (arraySize == 1)
            recordId = record.split(delimiter)[sortingColumnIndex[0]];
        else
            recordId = record.split(delimiter)[sortingColumnIndex[0]] + "+" + record.split(delimiter)[sortingColumnIndex[1]];
        return recordId;
    }

    String getActionIndicator(Action action) {
        return actionMap.get(action);
    }

    Map<Action, String> loadActionMap() {
        Map<Action, String> actionMap = new HashMap<>();
        actionMap.put(Action.Update, "U");
        actionMap.put(Action.Insert, "I");
        actionMap.put(Action.Delete, "D");
        actionMap.put(Action.Unchanged, "N");
        return actionMap;
    }


}
