package org.bibalex.eol.validator;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.record.DarwinCoreRecord;
import org.gbif.dwca.record.Record;
import org.gbif.dwca.record.StarRecord;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by darteam on 8/16/16.
 */
public class TempTestingClass {

    public static boolean readingDarwinCoreArchive(String inputPath, String outputPath) {
        try {
            File myArchiveFile = new File(inputPath);
            File extractToFolder = new File(outputPath);
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);

            // loop over core darwin core records and display scientificName
            Iterator<DarwinCoreRecord> it = dwcArchive.iteratorDwc();
            int counter = 0;
            while (it.hasNext()) {
                DarwinCoreRecord dwc = it.next();
                counter++;
                System.out.println(dwc.getScientificName());
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive");
            return false;
        }
    }

    public static boolean readDwcaWithExtensions(String archivePath) {
        try {
            //WARNING: StarRecord requires underlying data files(including extensions) to be sorted by the coreid column
            Archive dwcArchive = ArchiveFactory.openArchive(new File(archivePath));
            System.out.println("Archive rowtype: " + dwcArchive.getCore().getRowType() + ", "
                    + dwcArchive.getExtensions().size() + " extension(s)");
            // loop over core darwin core star records
            for (StarRecord rec : dwcArchive) {
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<< " + rec.core().id() + " scientificName: " + rec.core().value(DwcTerm.scientificName));
                if (rec.hasExtension(GbifTerm.VernacularName)) {
                    for (Record extRec : rec.extension(GbifTerm.VernacularName)) {
                        System.out.println(" -" + extRec.value(DwcTerm.vernacularName));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive");
            e.printStackTrace();
            return false;
        }
    }

//    public static boolean readDwcaWithExtensions2(String archivePath) {
//
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter("/home/darteam/eol/darwincore_archive_samples/3_analysis"));
//            //WARNING: StarRecord requires underlying data files(including extensions) to be sorted by the coreid column
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(archivePath));
//            System.out.println("Archive rowtype: " + dwcArchive.getCore().getRowTypeURI() + ", "
//                    + dwcArchive.getExtensions().size() + " extension(s)");
//            // loop over core darwin core star records
//            for (StarRecord rec : dwcArchive) {
////                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<< " + rec.core().id() + " scientificName: " + rec.core().value(DwcTerm.scientificName) + " >>>>>>>>>>>>>>>>>>>>>>>>");
//                bw.write("<<<<<<<<<<<<<<<<<<<<<<<<< " + rec.core().id() + " scientificName: " + rec.core().value(DwcTerm.scientificName) + " >>>>>>>>>>>>>>>>>>>>>>>>\n");
//                Map<Term, List<Record>> extensions = rec.extensions();
//                Set<Term> keys = extensions.keySet();
//                for (Term extKey : keys) {
//                    for (Record extRec : rec.extension(extKey)) {
//                        Set<Term> recordTerms = extRec.terms();
//                        for (Term st : recordTerms) {
//                            bw.write("      " + st.qualifiedName() + " : " + extRec.value(st));
//                            bw.newLine();
//                        }
//                    }
////                    List<Record> list = extensions.get(extKey);
////
////                    bw.write("    " + extKey.qualifiedName() + " : " + extKey.simpleName() + "\n");
////                    for (Record r : list) {
////                        Set<Term> recordTerms = r.terms();
////                        for (Term st : recordTerms) {
////                            bw.write("      "+st.qualifiedName() +" : "+r.value(st));
////                            bw.newLine();
////                        }
////                    }
//                }
//            }
//            bw.close();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Failed in parsing the darwin core archive");
//            return false;
//        }
//    }

    public static void printExtensionWithItsFields(String inputPath, String outputPath) {
        try {
            File myArchiveFile = new File(inputPath);
            File extractToFolder = new File(outputPath);
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);

            Set<ArchiveFile> extenions = dwcArchive.getExtensions();
            for (ArchiveFile af : extenions) {
                System.out.println(af.getRowType().qualifiedName() + " : " + af.getTitle());
                Set<Term> terms = af.getTerms();
                for (Term field : terms)
                    System.out.println("--- " + field.qualifiedName());
            }
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive : " + e.getMessage());
        }
    }

    public static void printFieldValueInExtension(String inputPath, String outputPath) {
        try {
            File myArchiveFile = new File(inputPath);
            File extractToFolder = new File(outputPath);
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);

            Set<ArchiveFile> extensions = dwcArchive.getExtensions();

            ArchiveFile descAF = null;
            for (ArchiveFile af : extensions) {
                if (af.getRowType().qualifiedName().equalsIgnoreCase("http://rs.gbif.org/terms/1.0/Description")) {
                    descAF = af;
                    break;
                }
            }
            if (descAF == null) {
                System.err.println("Description archive file not found");
                return;
            }
            Set<Term> afTerms = descAF.getTerms();
            Term mediaTerm = null;
            for (Term term : afTerms) {
                if (term.qualifiedName().equalsIgnoreCase("http://purl.org/dc/terms/description")) {
                    mediaTerm = term;
                    break;
                }
            }
            for (Record record : descAF) {
                if (mediaTerm.qualifiedName().equalsIgnoreCase("http://purl.org/dc/terms/description")) {
                    System.out.println(record.value(mediaTerm));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive : " + e.getMessage());
        }
    }

    public static void printFieldValueInExtension2(String inputPath, String outputPath) {
        Archive dwcArchive = null;
        try {
            File myArchiveFile = new File(inputPath);
            File extractToFolder = new File(outputPath);
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive : " + e.getMessage());
            return;
        }
        Set<ArchiveFile> extensions = dwcArchive.getExtensions();

        TermFactory termFactory = TermFactory.instance();
        ArchiveFile descAF = dwcArchive.getExtension(termFactory.findTerm("http://rs.gbif.org/terms/1.0/Description"));
        if (descAF == null) {
            System.err.println("Description archive file not found");
            return;
        }
        Term mediaTerm = termFactory.findTerm("http://purl.org/dc/terms/description");
        if (mediaTerm == null) {
            System.err.println("Description archive file not contain mediaTerm ");
            return;
        }
        for (Record record : descAF) {
            System.out.println(record.value(mediaTerm));
        }
    }

    public static String printCoreRecord(Record coreRecord) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<Term> list = coreRecord.terms();
        for (Term t : list)
            stringBuilder.append(t.qualifiedName() + " : " + coreRecord.value(t) + " , ");
        return stringBuilder.toString();
    }

    public static void testCoreIdOrder(String inputPath, String outputPath) {
        Archive dwcArchive = null;
        try {
            File myArchiveFile = new File(inputPath);
            File extractToFolder = new File(outputPath);
            dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
        } catch (Exception e) {
            System.err.println("Failed in parsing the darwin core archive : " + e.getMessage());
            return;
        }
        for (StarRecord item : dwcArchive) {
            Record coreRecord = item.core();
            System.out.println(printCoreRecord(coreRecord));
            Map<Term, List<Record>> extensions = item.extensions();
            Set<Term> keySet = extensions.keySet();
            for (Term terms : keySet) {
                System.out.println("--Ext :  " + terms.qualifiedName() + " : ");
                List<Record> records = extensions.get(terms);
                for (Record rec : records) {
                    System.out.println("------ Row :  " + printCoreRecord(rec));
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {
        String inputPath = "D:\\Users\\mina.edward\\WorkPlace\\EOL\\DarwinCoreArchive samples\\3.tar.gz";
//        String outputPath = "D:\\Users\\mina.edward\\WorkPlace\\EOL\\DarwinCoreArchive samples\\3";
        String outputPath = "/home/ba/4";


        String inputPath2="D:\\VirtualBoxShare\\CustomDwca\\3";
        readDwcaWithExtensions(outputPath);
//        testCoreIdOrder(inputPath, outputPath);

//        if (!readingDarwinCoreArchive(inputPath, outputPath)) {
//            System.exit(1);
//        }

//        if (!readingDarwinCoreArchive(inputPath, outputPath)) {
//            System.exit(1);
//        }

    }
}
