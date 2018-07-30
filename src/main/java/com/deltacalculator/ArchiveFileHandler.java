package com.deltacalculator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bibalex.eol.utils.CommonTerms;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;
import org.gbif.dwca.record.StarRecord;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.deltacalculator.DeltaCalculator.DWCADiff;



public class ArchiveFileHandler {
    Map<Term, Term> rowTypeId = setRowTypeId();

    public ArchiveFileHandler() {
    }
    private static final Logger logger = Logger.getLogger(ArchiveFileHandler.class);

    void copyMetaFile(File metaFile, String archivePathName) {
        File targetMetaFile = new File(archivePathName + "/" + metaFile.getName());
        if (!targetMetaFile.exists())
            try {
                Files.copy(metaFile.toPath(), targetMetaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            }
    }

    public String getHeader(File file) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file.getPath()));
            String fileHeader = bufferedReader.readLine();
            return (fileHeader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e);
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
            return "";
        }
    }

    public File addHeader(File file, String header) {
        File temp = new File(file.getPath() + "temp");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp, true));
            bufferedWriter.write(header + "\n");
            String line = bufferedReader.readLine();
            while (line != null) {
                bufferedWriter.write(line + "\n");
                line = bufferedReader.readLine();
            }
            commit(bufferedWriter);
            temp.renameTo(file);
            return (temp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.debug(e);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e);
            return null;
        }

    }

    public void commit(BufferedWriter bufferedWriter) {
        try {
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }

    }

    public void readFromFileWriteToFile(File inputFile, boolean mode, String targetArchive, ArchiveFile archiveFile) {
        BufferedReader readInput = null;
        try {
            readInput = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.error(e);
        }
        File outputFile = new File(targetArchive + "/" + inputFile.getName());
        String actionIndicator = "";
        if (!outputFile.exists())
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            }

        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();

        ActionFile actionFile = new ActionFile();

        BufferedWriter writeOutput = null;
        try {
            writeOutput = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e);
        }
        String inputLine = "",
                delimiter = archiveFile.getFieldsTerminatedBy();
        try {
            while ((inputLine = readInput.readLine()) != null) {
                if (mode) {
                    writeOutput.write(lineInsert(inputLine));
                    actionIndicator = actionFile.getActionIndicator(ActionFile.Action.Insert);
                } else {
                    writeOutput.write(lineDelete(inputLine));
                    actionIndicator = actionFile.getActionIndicator(ActionFile.Action.Delete);
                }
                String recordId = actionFile.getRecordId(inputLine, delimiter, archiveFileHandler.getSortingColumnIndex(archiveFile));
                actionFile.writeLineToActionFile(archiveFile, recordId, delimiter, actionIndicator);

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e);
        }
        commit(writeOutput);
    }

    public String lineInsert(String inputLine) {
        inputLine = inputLine.substring(1, inputLine.length());
        inputLine = inputLine.trim();
        if (!StringUtils.isBlank(inputLine)) {
            String insertedLine = inputLine + "\n";
            return insertedLine;
        } else
            return ("");
    }

    public String lineDelete(String inputLine) {
        String deletedLine = inputLine.trim().substring(0, inputLine.length() - 2) + "\n";
        return deletedLine;
    }

    public String lineUpdate(String inputLine) {
        String updatedLine = inputLine.substring(inputLine.indexOf('|') + 1, inputLine.length()).trim() + "\n";
        return updatedLine;
    }

    void compareContent(Archive version1, Archive version2, ArrayList<Term> archiveRowTypesArrayList) {

        ArchiveFileHandler archiveFileHandler = new ArchiveFileHandler();
        CommandExecutor commandExecutor = new CommandExecutor();


        for (int i = 0; i < archiveRowTypesArrayList.size(); i++) {
            try {
                if (archiveRowTypesArrayList.get(i) != DwcTerm.Taxon) {
                    ArchiveFile archiveFile1 = version1.getExtension(archiveRowTypesArrayList.get(i)),
                            archiveFile2 = version2.getExtension(archiveRowTypesArrayList.get(i));
//                    File file1 = new File(version1.getLocation().getPath() + "/" + (archiveFile1.getTitle())),
//                            file2 = new File(version2.getLocation().getPath() + "/" + (archiveFile2.getTitle()));
                    File file1 = new File(archiveFile1.getLocationFile().getPath()),
                            file2 = new File(archiveFile2.getLocationFile().getPath());
                    String fileHeader = "";
                    fileHeader = archiveFileHandler.getHeader(file2);

                    try {
                        boolean isVernacular = archiveFile1.getRowType().equals(GbifTerm.VernacularName);
                        File sortedFile1 = commandExecutor.executeSort(file1, (archiveFile1.getFieldsTerminatedBy()), getSortingColumnIndex(archiveFile1)),
                                sortedFile2 = commandExecutor.executeSort(file2, (archiveFile2.getFieldsTerminatedBy()), getSortingColumnIndex(archiveFile2)),
                                differenceFile = commandExecutor.executeDiff(sortedFile1.getPath(), sortedFile2.getPath(), sortedFile2.getName(), archiveFile2, isVernacular);

                        if ((version2.getExtension(archiveRowTypesArrayList.get(i)).getIgnoreHeaderLines()) == 1 && !archiveFile2.getRowType().equals(CommonTerms.occurrenceTerm)
                        && archiveFile2.getRowType().equals(CommonTerms.associationTerm) && archiveFile2.getRowType().equals(DwcTerm.MeasurementOrFact))
                            archiveFileHandler.addHeader(differenceFile, fileHeader);
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    }

                } else {
                    ArchiveFile archiveCoreFile1 = version1.getCore(),
                            archiveCoreFile2 = version2.getCore();
                    File coreFile1 = new File(archiveCoreFile1.getLocationFile().getPath()),
                            coreFile2 = new File(archiveCoreFile2.getLocationFile().getPath());
                    System.out.println("Core Files: " + coreFile1.getPath() + ", " + coreFile2.getPath());
                    logger.info("Core Files: " + coreFile1.getPath() + ", " + coreFile2.getPath());
                    String fileHeader = "";
                    fileHeader = archiveFileHandler.getHeader(coreFile2);

                    try {
                        File sortedFile1 = commandExecutor.executeSort(coreFile1, (archiveCoreFile1.getFieldsTerminatedBy()), getSortingColumnIndex(archiveCoreFile1)),
                                sortedFile2 = commandExecutor.executeSort(coreFile2, (archiveCoreFile2.getFieldsTerminatedBy()), getSortingColumnIndex(archiveCoreFile2)),
                                differenceFile = commandExecutor.executeDiff(sortedFile1.getPath(), sortedFile2.getPath(), sortedFile2.getName(), (version2.getCore()), false);
                        if ((archiveCoreFile2.getIgnoreHeaderLines()) == 1)
                            archiveFileHandler.addHeader(differenceFile, fileHeader);
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                logger.error(e + ": Extension File Not Found");
                System.out.println(e + ": Extension File Not Found");
            }
        }
    }

    Map<Term, Term> setRowTypeId() {

        Map<Term, Term> rowTypeId = new HashMap<>();
        rowTypeId.put(CommonTerms.mediaTerm, CommonTerms.identifierTerm);
        rowTypeId.put(CommonTerms.occurrenceTerm, CommonTerms.occurrenceID);
        rowTypeId.put(CommonTerms.agentTerm, CommonTerms.identifierTerm);
        rowTypeId.put(CommonTerms.associationTerm, CommonTerms.associationIDTerm);
        rowTypeId.put(CommonTerms.referenceTerm, CommonTerms.identifierTerm);
        rowTypeId.put(DwcTerm.Taxon, DwcTerm.taxonID);
        rowTypeId.put(DwcTerm.MeasurementOrFact, DwcTerm.measurementID);
        rowTypeId.put(GbifTerm.VernacularName, DwcTerm.vernacularName);
        return rowTypeId;
    }

    void setRowType(Archive archive) {
        Map<Term, Term> rowTypeId = setRowTypeId();
        for (ArchiveFile archiveFile : archive.getExtensions()) {
            Term idTerm = rowTypeId.get(archiveFile.getRowType());
            List<ArchiveField> fieldsSorted = archiveFile.getFieldsSorted();
            ArrayList<Term> termsSorted = new ArrayList<Term>();
            for (ArchiveField archiveField : fieldsSorted) {
                termsSorted.add(archiveField.getTerm());
            }
            int index = termsSorted.indexOf(idTerm);
            System.out.println("Archive File: " + archiveFile.getTitle() + " - " + index);
        }
    }

    int[] getSortingColumnIndex(ArchiveFile archiveFile) {
        //return array of two elements in case of vernacular files, one otherwise
        Term idTerm = rowTypeId.get(archiveFile.getRowType());
        int[] index = new int[1];
        List<ArchiveField> fieldsSorted = archiveFile.getFieldsSorted();
        ArrayList<Term> termsSorted = new ArrayList<>();
        for (ArchiveField archiveField : fieldsSorted) {
            termsSorted.add(archiveField.getTerm());
        }
        if (idTerm == DwcTerm.vernacularName) {
            index = new int[2];
            index[1] = termsSorted.indexOf(CommonTerms.languageTerm);
        }
        index[0] = termsSorted.indexOf(idTerm);
        return index;
    }


    public void addIdToArrayList(ArchiveFile archiveFile, String id) {
        ModelsIds modelsIds = ModelsIds.getModelsIds();
        if (archiveFile.getRowType().equals(CommonTerms.agentTerm)) {
            modelsIds.addAgentId(id);
        } else if (archiveFile.getRowType().equals(CommonTerms.mediaTerm)) {
            modelsIds.addMediumId(id);
        } else if (archiveFile.getRowType().equals(DwcTerm.Taxon)) {
            modelsIds.addTaxaId(id);
        } else if (archiveFile.getRowType().equals(DwcTerm.MeasurementOrFact)) {
            modelsIds.addMeasurementId(id);
        } else if (archiveFile.getRowType().equals(CommonTerms.associationTerm)) {
            modelsIds.addAssociationId(id);
        } else if (archiveFile.getRowType().equals(CommonTerms.occurrenceTerm)) {
            modelsIds.addOccurrenceId(id);
        } else if (archiveFile.getRowType().equals(CommonTerms.referenceTerm)) {
            modelsIds.addReferenceId(id);
        } else if (archiveFile.getRowType().equals(GbifTerm.VernacularName)) {
            modelsIds.addVernacularId(id);
        }
    }

    public void setMediaOfChangedAgents(Archive updatedArchive) {
        ArchiveFile mediaFile = updatedArchive.getExtension(CommonTerms.mediaTerm);

        if (mediaFile != null) {
            HashSet<String> agentIds = ModelsIds.getModelsIds().getAgentIds();
            ActionFile actionFile = new ActionFile();

            for (Record record : mediaFile) {
                if (record.value(CommonTerms.agentIDTerm) != null) {
                    String[] recordAgents = record.value(CommonTerms.agentIDTerm).split(";");

                    for (int i = 0; i < recordAgents.length; i++) {
                        if (agentIds.contains(recordAgents[i])) {
                            HashSet<String> mediumIds = ModelsIds.getModelsIds().getMediaIds();
                            if (!mediumIds.contains(record.value(CommonTerms.identifierTerm))) {
                                ModelsIds.getModelsIds().addMediumId(record.value(CommonTerms.identifierTerm));
                                actionFile.writeLineToActionFile(mediaFile, record.value(CommonTerms.identifierTerm), mediaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                                writeRecordToArchiveFile(record, mediaFile);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setMediaOfChangedReference(Archive updatedArchive) {
        ArchiveFile mediaFile = updatedArchive.getExtension(CommonTerms.mediaTerm);

        if (mediaFile != null) {
            HashSet<String> referenceIds = ModelsIds.getModelsIds().getReferenceIds();
            ActionFile actionFile = new ActionFile();

            for (Record record : mediaFile) {
                if (record.value(CommonTerms.referenceIDTerm) != null) {
                    String[] recordReference = record.value(CommonTerms.referenceIDTerm).split(";");
                    for (int i = 0; i < recordReference.length; i++) {
                        if (referenceIds.contains(recordReference[i])) {
                            HashSet<String> mediumIds = ModelsIds.getModelsIds().getMediaIds();
                            if (!mediumIds.contains(record.value(CommonTerms.identifierTerm))) {
                                ModelsIds.getModelsIds().addMediumId(record.value(CommonTerms.identifierTerm));
                                actionFile.writeLineToActionFile(mediaFile, record.value(CommonTerms.identifierTerm), mediaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                                writeRecordToArchiveFile(record, mediaFile);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setMeasurementOfChangedReference(Archive updatedArchive) {
        ArchiveFile measurementFile = updatedArchive.getExtension(DwcTerm.MeasurementOrFact);

        if (measurementFile != null) {
            HashSet<String> referenceIds = ModelsIds.getModelsIds().getReferenceIds();
            ActionFile actionFile = new ActionFile();

            for (Record record : measurementFile) {
                if (record.value(CommonTerms.referenceIDTerm) != null) {
                    String[] recordReference = record.value(CommonTerms.referenceIDTerm).split(";");
                    for (int i = 0; i < recordReference.length; i++) {
                        if (referenceIds.contains(recordReference[i])) {
                            HashSet<String> measurementIds = ModelsIds.getModelsIds().getMeasurementIds();
                            if (!measurementIds.contains(record.value(DwcTerm.measurementID))) {
                                ModelsIds.getModelsIds().addMeasurementId(record.value(DwcTerm.measurementID));
                                actionFile.writeLineToActionFile(measurementFile, record.value(DwcTerm.measurementID), measurementFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                                writeRecordToArchiveFile(record, measurementFile);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setAssociationOfChangedReference(Archive updatedArchive) {
        ArchiveFile associationFile = updatedArchive.getExtension(CommonTerms.associationTerm);

        if (associationFile != null) {
            HashSet<String> referenceIds = ModelsIds.getModelsIds().getReferenceIds();
            ActionFile actionFile = new ActionFile();

            for (Record record : associationFile) {
                if (record.value(CommonTerms.referenceIDTerm) != null) {
                    String[] recordReference = record.value(CommonTerms.referenceIDTerm).split(";");
                    for (int i = 0; i < recordReference.length; i++) {
                        if (referenceIds.contains(recordReference[i])) {
                            HashSet<String> associationIds = ModelsIds.getModelsIds().getAssociationIds();
                            if (!associationIds.contains(record.value(CommonTerms.associationIDTerm))) {
                                ModelsIds.getModelsIds().addAssociationId(record.value(CommonTerms.associationIDTerm));
                                actionFile.writeLineToActionFile(associationFile, record.value(DwcTerm.measurementID), associationFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                                writeRecordToArchiveFile(record, associationFile);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setOccurrenceOfChangedMeasurements(Archive updatedArchive) {
        ArchiveFile measurementFile = updatedArchive.getExtension(DwcTerm.MeasurementOrFact);
        if (updatedArchive.getExtension(CommonTerms.occurrenceTerm) == null) {
            logger.info("No Occurrence File Found; aborting getUnchangedRecords");
            return;
        }
        if (measurementFile != null) {

            ArchiveFile occurrenceFile = updatedArchive.getExtension(CommonTerms.occurrenceTerm);
            HashSet<String> measurementIds = ModelsIds.getModelsIds().getMeasurementIds();
            HashSet<String> occurrenceIds = ModelsIds.getModelsIds().getOccurrenceIds();
            HashSet<String> notUpdatedOccurrenceIds = new HashSet<>(occurrenceIds);
            ActionFile actionFile = new ActionFile();

            for (Record record : measurementFile) {
                if (measurementIds.contains(record.value(DwcTerm.measurementID)) && !occurrenceIds.contains(record.value(CommonTerms.occurrenceID))) {
                    ModelsIds.getModelsIds().addOccurrenceId(record.value(CommonTerms.occurrenceID));
                }
            }

            for (Record record : occurrenceFile) {
                if (!notUpdatedOccurrenceIds.contains(record.value(CommonTerms.occurrenceID)) &&
                        occurrenceIds.contains(record.value(CommonTerms.occurrenceID))) {

                    actionFile.writeLineToActionFile(occurrenceFile, record.value(CommonTerms.occurrenceID), occurrenceFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                    writeRecordToArchiveFile(record, occurrenceFile);
                }
            }
        }
    }

    public void setOccurrenceOfChangedAssociations(Archive updatedArchive) {

        ArchiveFile associationFile = updatedArchive.getExtension(CommonTerms.associationTerm);

        if (associationFile != null) {
            ArchiveFile occurrenceFile = updatedArchive.getExtension(CommonTerms.occurrenceTerm);
            HashSet<String> associationIds = ModelsIds.getModelsIds().getAssociationIds();
            HashSet<String> occurrenceIds = ModelsIds.getModelsIds().getOccurrenceIds();
            HashSet<String> notUpdatedOccurrenceIds = new HashSet<>(occurrenceIds);
            ActionFile actionFile = new ActionFile();

            for (Record record : associationFile) {
                if (associationIds.contains(record.value(CommonTerms.associationIDTerm)) && !occurrenceIds.contains(record.value(CommonTerms.occurrenceID))) {
                    ModelsIds.getModelsIds().addOccurrenceId(record.value(CommonTerms.occurrenceID));
                }
            }

            for (Record record : occurrenceFile) {
                if (!notUpdatedOccurrenceIds.contains(record.value(CommonTerms.occurrenceID)) &&
                        occurrenceIds.contains(record.value(CommonTerms.occurrenceID))) {

                    actionFile.writeLineToActionFile(occurrenceFile, record.value(CommonTerms.occurrenceID), occurrenceFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                    writeRecordToArchiveFile(record, occurrenceFile);
                }
            }
        }
    }

    public void setTaxaOfChangedMedia(Archive updatedArchive, Archive oldArchive) {
        HashSet<String> mediaIds = ModelsIds.getModelsIds().getMediaIds();
        HashSet<String> occurrenceIds = ModelsIds.getModelsIds().getOccurrenceIds();
        HashSet<String> vernacularIds = ModelsIds.getModelsIds().getVernacularIds();
        HashSet<String> taxaIds = ModelsIds.getModelsIds().getTaxaIds();
        ArchiveFile taxaFile = updatedArchive.getCore();
        ActionFile actionFile = new ActionFile();

//        try {
            for (StarRecord starRecord : updatedArchive) {
                if (starRecord.extension(CommonTerms.mediaTerm) != null) {
                    for (Record record : starRecord.extension(CommonTerms.mediaTerm)) {
                        if (mediaIds.contains(record.value(CommonTerms.identifierTerm)) && !taxaIds.contains(starRecord.core().id())) {
                            ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                            actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                            writeRecordToArchiveFile(starRecord.core(), taxaFile);
                        }
                    }
                }
                if (starRecord.extension(CommonTerms.occurrenceTerm) != null) {
                    for (Record record : starRecord.extension(CommonTerms.occurrenceTerm)) {
                        if (occurrenceIds.contains(record.value(CommonTerms.occurrenceID)) && !taxaIds.contains(starRecord.core().id())) {
                            ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                            actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                            writeRecordToArchiveFile(starRecord.core(), taxaFile);
                        }
                    }
                }
                if (starRecord.extension(GbifTerm.VernacularName) != null) {
                    for (Record record : starRecord.extension(GbifTerm.VernacularName)) {
//                    System.out.println(record.toString());
                        String id = record.value(DwcTerm.vernacularName) + "+" + record.value(CommonTerms.languageTerm);
                        if (vernacularIds.contains(id) && !taxaIds.contains(starRecord.core().id())) {
                            ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                            actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                            writeRecordToArchiveFile(starRecord.core(), taxaFile);
                        }
                    }
                }

            }


        for (StarRecord starRecord : oldArchive) {
            if (starRecord.extension(CommonTerms.mediaTerm) != null) {
                for (Record record : starRecord.extension(CommonTerms.mediaTerm)) {
                    if (mediaIds.contains(record.value(CommonTerms.identifierTerm)) && !taxaIds.contains(starRecord.core().id())) {
                        ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                        actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                        writeRecordToArchiveFile(starRecord.core(), taxaFile);
                    }
                }
            }
            if (starRecord.extension(CommonTerms.occurrenceTerm) != null) {
                for (Record record : starRecord.extension(CommonTerms.occurrenceTerm)) {
                    if (occurrenceIds.contains(record.value(CommonTerms.occurrenceID)) && !taxaIds.contains(starRecord.core().id())) {
                        ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                        actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                        writeRecordToArchiveFile(starRecord.core(), taxaFile);
                    }
                }
            }
            if (starRecord.extension(GbifTerm.VernacularName) != null) {
                for (Record record : starRecord.extension(GbifTerm.VernacularName)) {
//                    System.out.println(record.toString());
                    String id = record.value(DwcTerm.vernacularName) + "+" + record.value(CommonTerms.languageTerm);
                    if (vernacularIds.contains(id) && !taxaIds.contains(starRecord.core().id())) {
                        ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
                        actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                        writeRecordToArchiveFile(starRecord.core(), taxaFile);
                    }
                }
            }

        }
//        } catch (NullPointerException e) {
//            logger.info(e + ": No media file found");
//        }
    }

//    public void setTaxaOfChangedOccurrences(Archive updatedArchive) {
//        HashSet<String> occurrenceIds = ModelsIds.getModelsIds().getOccurrenceIds();
//        HashSet<String> taxaIds = ModelsIds.getModelsIds().getTaxaIds();
//        ArchiveFile taxaFile = updatedArchive.getCore();
//        ActionFile actionFile = new ActionFile();
//        try {
//            for (StarRecord starRecord : updatedArchive) {
//                if (starRecord.extension(CommonTerms.occurrenceTerm) != null) {
//                    for (Record record : starRecord.extension(CommonTerms.occurrenceTerm)) {
//                        if (occurrenceIds.contains(record.value(CommonTerms.occurrenceID)) && !taxaIds.contains(starRecord.core().id())) {
//                            ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
//                            actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
//                            writeRecordToArchiveFile(starRecord.core(), taxaFile);
//                        }
//                    }
//                }
//            }
//        } catch (NullPointerException e) {
//            logger.info(e + ": No Occurrence File Found");
//        }
//    }

    public void setTaxaOfChangedReference(Archive updatedArchive) {
        HashSet<String> referenceIds = ModelsIds.getModelsIds().getReferenceIds();
        ArchiveFile taxaFile = updatedArchive.getCore();
        ActionFile actionFile = new ActionFile();
        try {

            for (Record record : taxaFile) {
                if (record.value(CommonTerms.referenceIDTerm) != null) {
                    String[] recordReference = record.value(CommonTerms.referenceIDTerm).split(";");
                    for (int i = 0; i < recordReference.length; i++) {
                        if (referenceIds.contains(recordReference[i])) {
                            HashSet<String> taxaIds = ModelsIds.getModelsIds().getTaxaIds();
                            if (!taxaIds.contains(record.value(DwcTerm.taxonID))) {
                                ModelsIds.getModelsIds().addTaxaId(record.value(DwcTerm.taxonID));
                                actionFile.writeLineToActionFile(taxaFile, record.value(DwcTerm.measurementID), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
                                writeRecordToArchiveFile(record, taxaFile);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            logger.info(e + ": No Reference File Found");
        }
    }

//    public void setTaxaOfChangedVernacular(Archive updatedArchive) {
//        HashSet<String> vernacularIds = ModelsIds.getModelsIds().getVernacularIds();
//        HashSet<String> taxaIds = ModelsIds.getModelsIds().getTaxaIds();
//        ArchiveFile taxaFile = updatedArchive.getCore();
//        ActionFile actionFile = new ActionFile();
//
//        try {
//            for (StarRecord starRecord : updatedArchive) {
//                if (starRecord.extension(GbifTerm.VernacularName) != null) {
//                    for (Record record : starRecord.extension(GbifTerm.VernacularName)) {
////                    System.out.println(record.toString());
//                        String id = record.value(DwcTerm.vernacularName) + "+" + record.value(CommonTerms.languageTerm);
//                        if (vernacularIds.contains(id) && !taxaIds.contains(starRecord.core().id())) {
//                            ModelsIds.getModelsIds().addTaxaId(starRecord.core().id());
//                            actionFile.writeLineToActionFile(taxaFile, starRecord.core().id(), taxaFile.getFieldsTerminatedBy(), actionFile.getActionIndicator(ActionFile.Action.Unchanged));
//                            writeRecordToArchiveFile(starRecord.core(), taxaFile);
//                        }
//                    }
//                }
//            }
//        } catch (NullPointerException e) {
//            logger.info(e + ": No Vernacular File Found");
//        }
//    }

    private void writeRecordToArchiveFile(Record record, ArchiveFile archiveFile) {
        try {
            List<ArchiveField> fieldsSorted = archiveFile.getFieldsSorted();
            ArrayList<Term> termsSorted = new ArrayList<Term>();
            for (ArchiveField archiveField : fieldsSorted) {
                termsSorted.add(archiveField.getTerm());
            }

            OwnDwcaWriter dwcaWriter = new OwnDwcaWriter(archiveFile.getArchive().getCore().getRowType(), DWCADiff);
            Map<Term, String> termStringMap = dwcaWriter.recordToMap(record, archiveFile);

            if (record.id() != null)
                dwcaWriter.newRecord(record.id());
            else
                dwcaWriter.newRecord("");

            dwcaWriter.addExtensionRecord(termsSorted, archiveFile.getRowType(), termStringMap, archiveFile.getTitle(),
                    archiveFile.getFieldsTerminatedBy(), archiveFile.getLinesTerminatedBy(), archiveFile.getEncoding());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}