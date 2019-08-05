package org.bibalex.eol.handler;

import org.bibalex.eol.utils.CommonTerms;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.Record;

import java.io.*;

public class ExtensionsHandler {

    public void duplicateCompositeKey(ArchiveFile extensionFile) {
        String newLines = "\n";
        if(extensionFile.hasTerm(DwcTerm.taxonID)) {
            try {
                BufferedReader objReader = new BufferedReader(new FileReader(extensionFile.getLocationFile().getPath()));
                if (extensionFile.getIgnoreHeaderLines() > 0)
                    objReader.readLine();
                for (Record record : extensionFile) {
                    String line = objReader.readLine();
                    String taxonId = record.value(DwcTerm.taxonID);
                    String[] taxonIds = new String[0];
                    if (taxonId.contains(";")||taxonId.contains("|")) {
                        if (taxonId.contains(";"))
                            taxonIds = taxonId.split(";");
                        if (taxonId.contains("|"))
                            taxonIds = taxonId.split("|");
                        for (String id : taxonIds) {
                            newLines = newLines + line.replace(taxonId, id) + "\n";
                        }

                    }
                }
                if (!newLines.equals("\n")) {
                    FileWriter fileWritter = new FileWriter(extensionFile.getLocationFile(), true);
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write(newLines);
                    bufferWritter.close();
                    fileWritter.close();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Archive archive = ArchiveFactory.openArchive(new File("/home/ba/eol_workspace/originals/2_org.out_valid"));
            ExtensionsHandler extensionsHandler = new ExtensionsHandler();
            ArchiveFile mediaFile = archive.getExtension(CommonTerms.mediaTerm);
            extensionsHandler.duplicateCompositeKey(mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
