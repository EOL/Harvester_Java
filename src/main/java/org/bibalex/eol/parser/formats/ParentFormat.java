package org.bibalex.eol.parser.formats;

import org.bibalex.eol.parser.handlers.Neo4jHandler;
import org.bibalex.eol.parser.handlers.SynonymNodeHandler;
import org.bibalex.eol.parser.models.Neo4jTaxon;
import org.bibalex.eol.parser.models.Taxon;
import org.apache.log4j.Logger;
import org.bibalex.eol.utils.CommonTerms;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.record.StarRecord;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Amr Morad
 */

public class ParentFormat extends Format {

    private int resourceId;
    private Neo4jHandler neo4jHandler;
    private static final Logger logger = Logger.getLogger(ParentFormat.class);
    private HashSet<String> missingParents;
    private EntityManager entityManager;

    public ParentFormat(int resourceId) {
        this.resourceId = resourceId;
        this.neo4jHandler = new Neo4jHandler();
        this.missingParents = new HashSet<>();
    }

    public void handleLines(ArrayList<Taxon> nodes, boolean normalResource) {
        System.out.println("start handling");
        for (Taxon node : nodes) {
            if (handleLine(node, normalResource)) {
                logger.debug("Handling line with taxon id: " + node.getIdentifier() + " is successful");
                System.out.println("Handling line with taxon id: " + node.getIdentifier() + " is successful");
            } else {
                logger.debug("Error in handling line with taxon id: " + node.getIdentifier());
                System.out.println("Error in handling line with taxon id: " + node.getIdentifier());
            }
        }
    }

    private boolean handleLine(Taxon node, boolean normalResource) {
//        int parentGeneratedNodeId = createParentIfNotExist(node.getParentTaxonId());
        int originalGeneratedNodeId =0;
        if(node.getPageEolId() != null)
            originalGeneratedNodeId = createOriginalNode(node.getIdentifier(), node.getScientificName(),
                node.getTaxonRank(), node.getTaxonomicStatus(), node.getAcceptedNodeId(), normalResource, node.getParentTaxonId(), Integer.valueOf(node.getPageEolId()));
        else
            originalGeneratedNodeId = createOriginalNode(node.getIdentifier(), node.getScientificName(),
                    node.getTaxonRank(), node.getTaxonomicStatus(), node.getAcceptedNodeId(), normalResource, node.getParentTaxonId(), 0);
        if (originalGeneratedNodeId > 0) {
            logger.debug("Successfully created the original node");
            System.out.println("Successfully created the original node");
            return true;
        } else {
            logger.debug("failure in creation of original node");
            System.out.println("failure in creation of original node");
            return false;
        }
    }

    private int createParentIfNotExist(String parentUsageId) {
        int parentGeneratedNodeId = neo4jHandler.getNodeIfExist(parentUsageId, resourceId);
        System.out.println("In create parent: " + parentGeneratedNodeId);
        if (parentGeneratedNodeId > 0) {
            logger.debug("parent exists");
        } else {
            logger.debug("parent does not exist");
            missingParents.add(parentUsageId);
            parentGeneratedNodeId = neo4jHandler.createParentWithPlaceholder(resourceId, parentUsageId);
        }
        return parentGeneratedNodeId;
    }

    private int createOriginalNode(String nodeId, String scientificName, String rank, String taxonomicStatus,
                                   String acceptedNodeId, boolean normalResource, String parentUsageId, int pageId) {
        int generatedNodeId;
        if (deleteFromMissingParentsIfExist(nodeId)) {
            int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
            generatedNodeId = handleParentExists(nodeId, scientificName, rank, resourceId, parentGeneratedNodeId, neo4jHandler, pageId);
            return generatedNodeId;
        } else {
            if (normalResource) {
                int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
                if (acceptedNodeId != null && !acceptedNodeId.equalsIgnoreCase(nodeId)) {
                    //synonym node
                    logger.debug("The node is synonym");
                    SynonymNodeHandler synonymNodeHandler = SynonymNodeHandler.getSynonymNodeHandler(resourceId, neo4jHandler);
                    generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, acceptedNodeId);
                } else {
                    //accepted node
                    logger.debug("The node is not synonym");
                    generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                            neo4jHandler, pageId);
                }
            } else {
                if (isSynonym(taxonomicStatus)) {
                    // as it synonym and we don't have acceptedNameUsageID so we send parentUsageId instead of acceptedNameUsageId
                    logger.debug("The node is synonym");
                    SynonymNodeHandler synonymNodeHandler = SynonymNodeHandler.getSynonymNodeHandler(resourceId, neo4jHandler);
                    generatedNodeId = synonymNodeHandler.handleSynonymNode(nodeId, scientificName, rank, parentUsageId);
                } else {
                    int parentGeneratedNodeId = createParentIfNotExist(parentUsageId);
                    logger.debug("The node is not synonym");
                    generatedNodeId = handleNonSynonymNode(scientificName, rank, nodeId, resourceId, parentGeneratedNodeId,
                            neo4jHandler, pageId);
                }
            }

            return generatedNodeId;
        }
    }

    private int handleParentExists(String nodeId, String scientificName, String rank, int resourceId, int parentGeneratedNodeId, Neo4jHandler neo4jHandler, int pageId) {
        int generatedAutoId = neo4jHandler.updateParent(resourceId, nodeId, scientificName,
                rank, parentGeneratedNodeId, pageId);
        return generatedAutoId;
    }

    private boolean deleteFromMissingParentsIfExist(String nodeId) {
        if (missingParents.contains(nodeId)) {
            missingParents.remove(nodeId);
            return true;
        }
        return false;
    }

    @Override
    public int deleteTaxon(String nodeID, int resourceId, String scientificName) {
        System.out.println("deleteFromTaxonFile");
        int generatedNodeId= neo4jHandler.deleteNodeParentFormat(nodeID, scientificName, resourceId);
        return generatedNodeId;
    }

    @Override
    public void updateTaxon(Taxon taxon) {
        int response =  neo4jHandler.updateTaxonParentFormat(taxon.getIdentifier(), resourceId, taxon.getScientificName(), taxon.getTaxonRank(), taxon.getParentTaxonId());
        if(response == 400)
            missingParents.add(taxon.getIdentifier());
    }


    public void insertTaxontoMysql(Archive dwca, EntityManager EM){
        this.entityManager=EM;
        int i=0;
        for (StarRecord record : dwca) {
            i++;
            System.out.println("insert record to database "+i);

            StoredProcedureQuery insertTaxon = entityManager
                    .createStoredProcedureQuery("insertTaxon")
                    .registerStoredProcedureParameter(
                            "taxon_id", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "resource_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "parent_id", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "scientific_name", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "rank", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "page_id", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "created", Boolean.class, ParameterMode.IN);

            insertTaxon.setParameter("taxon_id", record.core().value(DwcTerm.taxonID));
            insertTaxon.setParameter("resource_id", resourceId);
            if (record.core().value(DwcTerm.parentNameUsageID) == null) {
                insertTaxon.setParameter("parent_id", "0");
            } else {
                insertTaxon.setParameter("parent_id", record.core().value(DwcTerm.parentNameUsageID));
            }
            insertTaxon.setParameter("scientific_name", record.core().value(DwcTerm.scientificName));
            insertTaxon.setParameter("rank", record.core().value(DwcTerm.taxonRank));
            if (record.core().value(CommonTerms.eolPageTerm) == null) {
                insertTaxon.setParameter("page_id", -1);
            } else {
                insertTaxon.setParameter("page_id", Integer.valueOf(record.core().value(CommonTerms.eolPageTerm)));
            }
            insertTaxon.setParameter("created", false);
            try {
                insertTaxon.execute();
            }catch (Exception e){
                System.out.println("duplicate line");
            }
        }
        System.out.println("get roots");
        StoredProcedureQuery getRoots =
                entityManager.createStoredProcedureQuery("getRoots_2");
        getRoots.registerStoredProcedureParameter("p_resource_id", Integer.class, ParameterMode.IN);
        getRoots.setParameter("p_resource_id", resourceId);
        getRoots.execute();

        ArrayList<Neo4jTaxon> roots = new ArrayList<>();
        getRoots.getResultList()
                .forEach(taxon -> {
                    Object[] object_taxon = (Object[]) taxon;
                    roots.add(new Neo4jTaxon((String)object_taxon[0], (Integer)object_taxon[1], (String)object_taxon[2],
                            (String)object_taxon[3], (String)object_taxon[4], (Integer)object_taxon[5], (Boolean)object_taxon[6]));

                });
        buildGraphRecursive(roots, 0);

        //get missing parents
        getOrphans();
    }

    private void getOrphans() {
        StoredProcedureQuery getOrphans =
                entityManager.createStoredProcedureQuery("getOrphans");
        getOrphans.registerStoredProcedureParameter("p_resource_id", Integer.class, ParameterMode.IN);
        getOrphans.setParameter("p_resource_id", resourceId);
        getOrphans.execute();

        ArrayList<Neo4jTaxon> orphans = new ArrayList<>();
        HashSet<String> ids = new HashSet<>();
        getOrphans.getResultList()
                .forEach(taxon -> {
                    Object[] object_taxon = (Object[]) taxon;
                    orphans.add(new Neo4jTaxon((String)object_taxon[0], (Integer)object_taxon[1], (String)object_taxon[2],
                            (String)object_taxon[3], (String)object_taxon[4], (Integer)object_taxon[5], (Boolean)object_taxon[6]));
                    ids.add((String)object_taxon[0]);

                });
        createOrphans(orphans, ids);
    }

    private void createOrphans(ArrayList<Neo4jTaxon> orphans, HashSet<String> ids) {
        if(orphans.size()==0)
            return;
        for(Neo4jTaxon orphan:orphans){
            if(!ids.contains(orphan.getParent_id())){
                Neo4jHandler neo4jHandler = new Neo4jHandler();
                int parentGeneratedNodeId=neo4jHandler.createParentWithPlaceholder(orphan.getResource_id(), orphan.getParent_id());
                ArrayList<Neo4jTaxon> children =getChildren(orphan.getParent_id(), orphan.getResource_id());
                buildGraphRecursive(children, parentGeneratedNodeId);
            }
            break;
        }
        getOrphans();
    }

    public void buildGraphRecursive(ArrayList<Neo4jTaxon> parents, int parentGeneratedNodeId){
        System.out.println("build graph of parent "+parentGeneratedNodeId);
        if(parents.size()==0)
            return;
        else{
            Neo4jHandler neo4jHandler = new Neo4jHandler();
            int globalparentGeneratedNodeId = parentGeneratedNodeId;
            for(Neo4jTaxon parent: parents) {

                parentGeneratedNodeId = neo4jHandler.createAcceptedNode(parent.getResource_id(), parent.getTaxon_id(), parent.getScientific_name(),
                        parent.getRank(), globalparentGeneratedNodeId, parent.getPage_id());
                if(parentGeneratedNodeId != -1){
                    StoredProcedureQuery updateTaxon =
                            entityManager.createStoredProcedureQuery("updateTaxon");
                    updateTaxon.registerStoredProcedureParameter("p_taxon_id", String.class, ParameterMode.IN);
                    updateTaxon.registerStoredProcedureParameter("p_resource_id", Integer.class, ParameterMode.IN);

                    updateTaxon.setParameter("p_taxon_id", parent.getTaxon_id());
                    updateTaxon.setParameter("p_resource_id", parent.getResource_id());
                    updateTaxon.execute();
                }

                ArrayList<Neo4jTaxon> children = getChildren(parent.getTaxon_id(), parent.getResource_id());


                buildGraphRecursive(children, parentGeneratedNodeId);
            }
        }

    }

    public ArrayList<Neo4jTaxon> getChildren(String taxon_id, int resource_id){
        System.out.println("get children of taxon "+taxon_id);
        ArrayList<Neo4jTaxon> children =new ArrayList<>();
        StoredProcedureQuery getChildren =
                entityManager.createStoredProcedureQuery("getChildren");
        getChildren.registerStoredProcedureParameter("p_parent_id", String.class, ParameterMode.IN);
        getChildren.registerStoredProcedureParameter("p_resource_id", Integer.class, ParameterMode.IN);

        getChildren.setParameter("p_parent_id", taxon_id);
        getChildren.setParameter("p_resource_id", resource_id);
        getChildren.execute();


        getChildren.getResultList()
                .forEach(taxon -> {
                    Object[] object_taxon = (Object[]) taxon;
                    children.add(new Neo4jTaxon((String)object_taxon[0], (Integer)object_taxon[1], (String)object_taxon[2],
                            (String)object_taxon[3], (String)object_taxon[4], (Integer)object_taxon[5], (Boolean)object_taxon[6]));

                });
        return children;
    }

}