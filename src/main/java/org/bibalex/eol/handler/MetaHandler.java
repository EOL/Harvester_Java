package org.bibalex.eol.handler;

import org.bibalex.eol.utils.CommonTerms;
import org.bibalex.eol.utils.TermURIs;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.neo4j.cypher.internal.compiler.v2_3.No;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.io.File;
import java.io.IOException;

public class MetaHandler {

    public void addGeneratedAutoId(String path) {

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
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public void adjustMetaFileToBeReadableByLibrary(String DWCaPath) {
        String metaFilePath = getMetaFilePath(DWCaPath);
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(metaFilePath);

            adjustXMLTags(doc);
            addCoreIdToExtensions(doc);
            addCoreIdToCore(doc);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(metaFilePath));
            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void adjustXMLTags(Document doc) {

        System.out.println("convert table to extension and core");
        NodeList extensions = doc.getElementsByTagName("table");
        for (int i = 0; i < extensions.getLength(); i++) {
            System.out.println("fount table tag");
            Node extension = extensions.item(i);

            String rowtype = extension.getAttributes().getNamedItem("rowType").getNodeValue();
            if (rowtype.equals(TermURIs.taxonURI))
                doc.renameNode(extension, null, "core");
            else
                doc.renameNode(extension, null, "extension");
        }
        System.out.println("done conversion");
    }

    private void addCoreIdToExtensions(Document doc) {

        System.out.println("start adding coreID in extensions");
        NodeList extensions = doc.getElementsByTagName("extension");
        for (int i = 0; i < extensions.getLength(); i++) {
            Element extension = (Element) extensions.item(i);
            NodeList coreId = extension.getElementsByTagName("coreid");
            if(coreId.getLength()==0) {
                NodeList fields = extension.getElementsByTagName("field");
                for (int j = 0; j < fields.getLength(); j++) {
                    if (fields.item(j).getAttributes().getNamedItem("term").getNodeValue().equals(TermURIs.taxonID_URI)) {
                        System.out.println("found taxonID in extensions");
                        String index = fields.item(j).getAttributes().getNamedItem("index").getNodeValue();
                        Element field = doc.createElement("coreid");
                        field.setAttribute("index", index);
                        extension.appendChild(field);
                        break;
                    }
                }
            }
        }
    }

    private void addCoreIdToCore(Document doc) {

        System.out.println("start adding coreID in core");
        Element core = (Element) doc.getElementsByTagName("core").item(0);
        NodeList coreId = core.getElementsByTagName("coreid");
        if(coreId.getLength()==0) {
            NodeList fields = core.getElementsByTagName("field");
            for (int j = 0; j < fields.getLength(); j++) {
                if (fields.item(j).getAttributes().getNamedItem("term").getNodeValue().equals(TermURIs.taxonID_URI)) {
                    System.out.println("found");
                    String index = fields.item(j).getAttributes().getNamedItem("index").getNodeValue();
                    Element field = doc.createElement("coreid");
                    field.setAttribute("index", index);
                    core.appendChild(field);
                    break;
                }
            }
        }
    }

    private Archive openDwcAFolder(String path) {

        try {
            Archive dwcArchive;
            dwcArchive = ArchiveFactory.openArchive(new File(path));
            String metaFilePath = getMetaFilePath(dwcArchive.getLocation().getPath());
            if (metaFilePath == null) {
                System.out.println(path + ": Meta File not Found!");
            } else dwcArchive.setMetadataLocation(metaFilePath);
            return dwcArchive;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getMetaFilePath(String archivePath) {
        String metaFiles[] = {"metadata.xml", "meta.xml", "eml.xml"};
        boolean metaFileExists = false;
        File metaFile = new File(archivePath + "/" + metaFiles[0]);

        for (int i = 0; i < metaFiles.length; i++) {
            metaFile = new File(archivePath + "/" + metaFiles[i]);
            if (metaFile.exists()) {
                metaFileExists = true;
                break;
            }
        }
        if (metaFileExists == false) {
            System.out.println(archivePath + ": Meta File not Found!");
            return null;
        }
        return metaFile.getPath();
    }


    public static void main(String[] args) {
//            Archive dwcArchive = ArchiveFactory.openArchive(new File("/home/ba/eol_resources/small_dynamic"));
        MetaHandler metaHandler = new MetaHandler();
        metaHandler.adjustMetaFileToBeReadableByLibrary("/home/ba/eol_resources/dwca29108");
        metaHandler.addGeneratedAutoId("/home/ba/eol_resources/dwca29108");
    }
}
