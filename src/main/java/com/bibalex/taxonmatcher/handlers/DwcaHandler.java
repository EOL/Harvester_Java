package com.bibalex.taxonmatcher.handlers;

import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFile;

import java.util.Set;

/**
 * Created by Amr.Morad
 */
public class DwcaHandler {

    public ArchiveFile getArchiveFile(Archive dwcArchive, String rowTypeURI) throws Exception {
        Set<ArchiveFile> extensions = dwcArchive.getExtensions();
        ArchiveFile archiveFile = null;
        for (ArchiveFile af : extensions) {
            if (af.getRowType().qualifiedName().equalsIgnoreCase(rowTypeURI)) {
                archiveFile = af;
                break;
            }
        }
        if (archiveFile == null) {
            if (dwcArchive.getCore().getRowType().qualifiedName().equalsIgnoreCase(rowTypeURI))
                archiveFile = dwcArchive.getCore();
            else
                throw new Exception("Archive file with row type " + rowTypeURI + " not found");
        }
        return archiveFile;
    }

}
