package org.bibalex.eol.validator.functions;

import org.bibalex.eol.validator.rules.MetaFileValidationRule;
import org.gbif.dwc.terms.Term;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFile;

import java.util.Set;

public class MetaFileValidationFunctions {

    public static boolean checkFieldExists(Archive dwca, MetaFileValidationRule rule) {
        Set<ArchiveFile> extensions = dwca.getExtensions();
        boolean foundRowType = false;
        boolean allValid = true;
        boolean extensionExits = false;
        for (ArchiveFile af : extensions) {
            String rowType = af.getRowType().qualifiedName();
            if (rowType.equalsIgnoreCase(rule.getRowTypeURI())) {
                foundRowType = true;
                Set<Term> terms = af.getTerms();
                boolean fieldURI_exists = false;
                for (Term field : terms) {
                    if (field.qualifiedName().equalsIgnoreCase(rule.getFieldURI())) {
                        fieldURI_exists = true;
                        break;
                    }
                }
                if (!fieldURI_exists) allValid = false;
            }
        }
        if (!foundRowType || (foundRowType && allValid))
            return true;
        else
            return false;
    }


}
