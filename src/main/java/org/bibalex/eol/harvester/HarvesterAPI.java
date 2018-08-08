package org.bibalex.eol.harvester;

import com.bibalex.taxonmatcher.controllers.RunTaxonMatching;
import org.apache.commons.io.FilenameUtils;
import org.bibalex.eol.parser.DwcaParser;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.models.Neo4jTaxon;
import org.bibalex.eol.validator.DwcaValidator;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

@Service
public class HarvesterAPI {
    @PersistenceContext
    private EntityManager entityManager;

    public int getCount() {
        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("getRoots");
        query.registerStoredProcedureParameter("p_taxon_id", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_resource_id", Integer.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_parent_id", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_scientific_name", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_rank", String.class, ParameterMode.OUT);
        query.registerStoredProcedureParameter("p_page_id", String.class, ParameterMode.OUT);

        query.execute();
//        System.out.println(query.getResultList().get(0));
        System.out.println("=========================");
        System.out.println("=========================");
        System.out.println(query.getOutputParameterValue("p_resource_id"));
        System.out.println("=========================");
        System.out.println("=========================");
        Integer outCount = (Integer) query.getOutputParameterValue("p_resource_id");
        return outCount;
//        getRoots.execute();
//        System.out.println("here");
//        List<Neo4jTaxon> taxons = (List<Neo4jTaxon>) getRoots.getResultList();
//        System.out.println("here2");
//        for(Neo4jTaxon taxon: taxons)
//            System.out.println(taxon.getParent_id());
//    }

    }

    public boolean callValidation(String path, int resourceID, boolean newResource){
        try {
            DwcaValidator validator = new DwcaValidator("configs.properties");
            File myArchiveFile = new File(path);
            File extractToFolder = new File(FilenameUtils.removeExtension(path) + ".out");
            Archive dwcArchive = ArchiveFactory.openArchive(myArchiveFile, extractToFolder);
//            Archive dwcArchive = ArchiveFactory.openArchive(new File(path));
            System.out.println("call validationnnnnnnnnnnnnn");
            validator.validateArchive(dwcArchive.getLocation().getPath(), dwcArchive);
//            return true;
            boolean done = callParser(FilenameUtils.removeExtension(path)+".out_valid", resourceID, newResource);
            return done;
        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
            return false;
        } catch (Exception e) {
//            System.out.println("exceptionnnnnnnnnnnnnnnnnnnnn");
//            e.printStackTrace();
            return false;
        }

    }

    private boolean callParser(String path, int resourceID, boolean newResource){
        Archive dwcArchive = null;
        try {
            PropertiesHandler.initializeProperties();
            dwcArchive = ArchiveFactory.openArchive(new File(path));
            DwcaParser dwcaP = new DwcaParser(dwcArchive, newResource, entityManager);
            dwcaP.prepareNodesRecord(resourceID);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
