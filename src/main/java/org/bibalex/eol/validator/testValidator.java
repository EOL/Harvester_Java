package org.bibalex.eol.validator;

import org.apache.commons.io.FilenameUtils;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;

import java.io.File;

public class testValidator {
    public static void main (String []args){
//        try {
//            DwcaValidator validator = new DwcaValidator("configs.properties");
//            String path = "/home/ba/eol_resources/4";
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(path));
//
//            validator.copyMetaFile(dwcArchive, "meta.xml" );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            DwcaValidator validator = new DwcaValidator("configs.properties");
            String path = "/home/ba/eol_resources/4.tar.gz";
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);

//            for (StarRecord rec : dwcArchive) {
//                System.out.println("henaaaa");
//
//            }
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(path));

            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
