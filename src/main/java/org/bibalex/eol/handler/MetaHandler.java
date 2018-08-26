package org.bibalex.eol.handler;

import com.deltacalculator.ArchiveHandler;
import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MetaHandler {
    public void editMetaFile(String path){
        Archive dwca = openDwcAFolder(path);
        int index = dwca.getCore().getFields().size();
        String metaFilePath = dwca.getMetadataLocation();
        if (dwca.getCore().hasTerm(CommonTerms.generatedAutoIdTerm))
            return;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(metaFilePath);
            Node Archive = doc.getFirstChild();
            Node core = doc.getElementsByTagName("core").item(0);
            Element field = doc.createElement("field");
            field.setAttribute("index", String.valueOf(index));
            field.setAttribute("term", TermURIs.generated_auto_id);
            core.appendChild(field);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(metaFilePath));
            transformer.transform(source, result);

            System.out.println("Done");
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    Archive openDwcAFolder(String path) {
        try {
            Archive dwcArchive;
            dwcArchive = ArchiveFactory.openArchive(new File(path));
            String metaFiles[] = {"metadata.xml", "meta.xml", "eml.xml"};
            int i;
            boolean metaFileExists = false;
            File metaFile = new File(dwcArchive.getLocation().getPath() + "/" + metaFiles[0]);

            for (i = 0; i < metaFiles.length; i++) {
                metaFile = new File(dwcArchive.getLocation().getPath() + "/" + metaFiles[i]);
                if (metaFile.exists()) {
                    metaFileExists = true;
                    break;
                }
            }
            if (metaFileExists == false) {
                System.out.println(path+": Meta File not Found!");
            } else dwcArchive.setMetadataLocation(metaFile.getPath());
            return dwcArchive;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main (String []args){
//            Archive dwcArchive = ArchiveFactory.openArchive(new File("/home/ba/eol_resources/small_dynamic"));
//            MetaHandler metaHandler = new MetaHandler();
//            metaHandler.editMetaFile("/home/ba/eol_workspace/originals/179_org.out");
    }
}
