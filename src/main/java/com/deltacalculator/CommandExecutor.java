package com.deltacalculator;

import org.apache.log4j.Logger;
import org.gbif.dwca.io.ArchiveFile;
import java.io.*;
import static com.deltacalculator.DeltaCalculator.DWCADiff;

public class CommandExecutor {
    private static final Logger logger = Logger.getLogger(CommandExecutor.class);


    void removeDirectory(String directory) {
        System.out.println("Removing Directory: " + directory);
        logger.info("Removing Directory: " + directory);
        Process removeDir = null;
        try {
            removeDir = Runtime.getRuntime().exec("rm -r " + directory);
            removeDir.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error(e);
        }

    }

    void compress(File dwcADir) {
        System.out.println("Compressing Archive: " + dwcADir.getPath());
        logger.info("Compressing Archive: " + dwcADir.getPath());
        try {
            Process compress = Runtime.getRuntime().exec("tar -czf " + dwcADir.getPath() + ".tar.gz" + " " + dwcADir.getPath());
            compress.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);

        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error(e);
        }

    }

    public File executeDiff(String file1, String file2,String diffFileName, ArchiveFile archiveFile) {
        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();
        String file1SDIFF = file1,
                file2SDIFF = file2;
        System.out.println("Comparing Files: " + file1 + ", " + file2);
        logger.info("Comparing Files: " + file1 + ", " + file2);
        if (!DWCADiff.exists())
            DWCADiff.mkdir();
        File diffFile = new File(DWCADiff + "/" + diffFileName);

        try {
            if (!diffFile.exists())
                diffFile.createNewFile();
            if (file1.contains(" "))
                file1SDIFF = removeSpaceFromFileName(file1);
            if (file2.contains(" "))
                file2SDIFF = removeSpaceFromFileName(file2);
            String command = "sdiff " + file1SDIFF + " " + file2SDIFF + " -s" + " -H" + " -w" + "6144";
            System.out.println("Executing Command: " + command);
            logger.info("Executing Command: " + command);
            Process sDiff = Runtime.getRuntime().exec(command);
//            sDiff.wait();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sDiff.getInputStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(diffFile));
            ActionFile actionFile = new ActionFile();
            int i = 0;
            String line = "",
                    actionIndicator = "",
                    outputLine = "",
                    delimiter = archiveFile.getFieldsTerminatedBy();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(">")) {
                    outputLine = archiveFileHandler.lineInsert(line);
                    actionIndicator = actionFile.getActionIndicator(ActionFile.Action.Insert);
                } else if (line.endsWith("<")) {
                    outputLine = archiveFileHandler.lineDelete(line);
                    actionIndicator = actionFile.getActionIndicator(ActionFile.Action.Delete);
                } else {
                    outputLine = archiveFileHandler.lineUpdate(line);
                    actionIndicator = actionFile.getActionIndicator(ActionFile.Action.Update);
                }
                bufferedWriter.write(outputLine);
                String recordId = actionFile.getRecordId(outputLine, delimiter, archiveFileHandler.getSortingColumnIndex(archiveFile));
                actionFile.writeLineToActionFile(archiveFile, recordId, delimiter, actionIndicator);
                archiveFileHandler.addIdToArrayList(archiveFile, recordId);
                i++;
            }
            System.out.println("i = " + i);
            logger.info(diffFile.getName() + ": Number of modified records = " + i);
            archiveFileHandler.commit(bufferedWriter);
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        return diffFile;
    }

    private String removeSpaceFromFileName(String fileName) throws IOException {
        File oldFile = new File(fileName);
        String newFileName = fileName.replace(" ", "_");
        File newFile = new File(newFileName);
        if (!newFile.exists())
            newFile.createNewFile();
        oldFile.renameTo(newFile);
        return newFileName;
    }

    public File executeSort(File inputFile, String archiveFileDelimiter, int[] sortingColumnIndex) throws IOException, InterruptedException {
        ExternalSort.delimiter = archiveFileDelimiter;
        ExternalSort.size = sortingColumnIndex.length;
        ExternalSort.sortingColumnIndex = sortingColumnIndex;
        System.out.println("Sorting File: " + inputFile.getPath());
        logger.info("Sorting File: " + inputFile.getPath());
        File outputFile = new File(inputFile.getPath());
        ExternalSort.sort(inputFile, outputFile);
        outputFile.renameTo(inputFile);
        return (outputFile);
    }
}