package org.bibalex.eol.parser;

import com.bibalex.taxonmatcher.handlers.GlobalNamesHandler;
import org.bibalex.eol.parser.handlers.PropertiesHandler;
import org.bibalex.eol.parser.models.*;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import scala.Int;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MysqlConnector {

    private EntityManager entityManager;
    private int resourceID;

    public MysqlConnector(EntityManager entityManager, int resourceID){
        this.entityManager=entityManager;
        this.resourceID=resourceID;
    }

    public int insertRankToMysql(NodeRecord tableRecord){
        System.out.println("insert new rank");
        StoredProcedureQuery insertRank = entityManager
                .createStoredProcedureQuery("insertRank")
                .registerStoredProcedureParameter(
                        "name_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "rank_id", Integer.class, ParameterMode.OUT);
        insertRank.setParameter("name_p", tableRecord.getTaxon().getTaxonRank());
        insertRank.setParameter("created_at_p", new Date());
        insertRank.setParameter("updated_at_p", new Date());

        try {
            insertRank.execute();
            return (int) insertRank.getOutputParameterValue("rank_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }

    }

    public int insertNodeToMysql(NodeRecord tableRecord, int rank_id){
        System.out.println("insert new node");
        GlobalNamesHandler globalNamesHandler = new GlobalNamesHandler();
        Taxon taxon = tableRecord.getTaxon();
        StoredProcedureQuery insertNode = entityManager
                .createStoredProcedureQuery("insertNode")
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "scientific_name_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "canonical_form_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "generated_node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "resource_pk_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "rank_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_id", Integer.class, ParameterMode.OUT);

        insertNode.setParameter("resource_id_p", resourceID);
        insertNode.setParameter("scientific_name_p", taxon.getScientificName());
        insertNode.setParameter("canonical_form_p", globalNamesHandler.getCanonicalForm(taxon.getScientificName()));
        insertNode.setParameter("generated_node_id_p", Integer.valueOf(tableRecord.getGeneratedNodeId()));
        insertNode.setParameter("resource_pk_p", taxon.getIdentifier());
        insertNode.setParameter("rank_id_p", rank_id);
        insertNode.setParameter("created_at_p", new Date());
        insertNode.setParameter("updated_at_p", new Date());

        try {
            insertNode.execute();
            return (int) insertNode.getOutputParameterValue("node_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    public void insertPageToMysql(NodeRecord tableRecord, int node_id){
        System.out.println("insert new page");
        StoredProcedureQuery insertPage = entityManager
                .createStoredProcedureQuery("insertPage")
                .registerStoredProcedureParameter(
                        "id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN);

        insertPage.setParameter("id_p", Integer.valueOf(tableRecord.getTaxon().getPageEolId()));
        insertPage.setParameter("node_id_p", node_id);
        insertPage.setParameter("created_at_p", new Date());
        insertPage.setParameter("updated_at_p", new Date());

        try {
            insertPage.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    public void insertPagesNodesToMysql(int node_id, int page_id) {
        System.out.println("insert new page_node");
        StoredProcedureQuery insertPagesNode = entityManager
                .createStoredProcedureQuery("insertPagesNode")
                .registerStoredProcedureParameter(
                        "page_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "is_native_p", Boolean.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN);

        insertPagesNode.setParameter("page_id_p", page_id);
        insertPagesNode.setParameter("node_id_p", node_id);
        if(resourceID == Integer.valueOf(PropertiesHandler.getProperty("DWHId")))
            insertPagesNode.setParameter("is_native_p", true);
        else
            insertPagesNode.setParameter("is_native_p", false);
        insertPagesNode.setParameter("created_at_p", new Date());
        insertPagesNode.setParameter("updated_at_p", new Date());

        try {
            insertPagesNode.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    public void insertScientificNameToMysql(NodeRecord tableRecord, int node_id) {
        System.out.println("insert new scientific name");
        Taxon taxon = tableRecord.getTaxon();
        GlobalNamesHandler globalNamesHandler = new GlobalNamesHandler();
        StoredProcedureQuery insertScientificName = entityManager
                .createStoredProcedureQuery("insertScientificName")
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "canonical_form_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "italicized_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_resource_pk_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "generated_node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "page_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN);

        insertScientificName.setParameter("resource_id_p", resourceID);
        insertScientificName.setParameter("canonical_form_p", globalNamesHandler.getCanonicalForm(taxon.getScientificName()));
        insertScientificName.setParameter("italicized_p", taxon.getScientificName());
        insertScientificName.setParameter("node_resource_pk_p", taxon.getIdentifier());
        insertScientificName.setParameter("generated_node_id_p", Integer.valueOf(tableRecord.getGeneratedNodeId()));
        insertScientificName.setParameter("page_id_p", Integer.valueOf(tableRecord.getTaxon().getPageEolId()));
        insertScientificName.setParameter("node_id_p", node_id);
        insertScientificName.setParameter("created_at_p", new Date());
        insertScientificName.setParameter("updated_at_p", new Date());

        try {
            insertScientificName.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    public void insertVernacularsToMysql(NodeRecord tableRecord, int node_id) {
        ArrayList<VernacularName> vernaculars = tableRecord.getVernaculars();
        for(VernacularName vernacular : vernaculars){
            int language_id = insertLanguageToMysql(vernacular.getLanguage());
            insertVernacularToMysql(vernacular, language_id, node_id, Integer.valueOf(tableRecord.getTaxon().getPageEolId()), Integer.valueOf(tableRecord.getGeneratedNodeId()));
        }
    }

    private void insertVernacularToMysql(VernacularName vernacular, int language_id, int node_id, int page_id, int generated_node_id){

        System.out.println("insert new vernacular");
        StoredProcedureQuery insertVernacular = entityManager
                .createStoredProcedureQuery("insertVernacular")
                .registerStoredProcedureParameter(
                        "string_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "is_prefered_by_resource_p", Boolean.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "generated_node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "page_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "node_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "language_id_p", Integer.class, ParameterMode.IN);

        insertVernacular.setParameter("string_p", vernacular.getName());
        insertVernacular.setParameter("resource_id_p", resourceID);
        insertVernacular.setParameter("is_prefered_by_resource_p", vernacular.getIsPreferred()=="1"? true:false);
        insertVernacular.setParameter("generated_node_id_p", generated_node_id);
        insertVernacular.setParameter("page_id_p", page_id);
        insertVernacular.setParameter("node_id_p", node_id);
        insertVernacular.setParameter("created_at_p", new Date());
        insertVernacular.setParameter("updated_at_p", new Date());
        insertVernacular.setParameter("language_id_p", language_id);


        try {
            insertVernacular.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    public void insertMediaToMysql(NodeRecord tableRecord){
        ArrayList<Media> media = tableRecord.getMedia();
        for(Media medium : media){
            int language_id = insertLanguageToMysql(medium.getLanguage());
            int license_id = insertLicenseToMysql(medium.getLicense());
            int location_id = medium.getLocationCreated() != null ? insertLocationToMysql(medium) : -1;
            int medium_id = insertMediumToMysql(medium, language_id, license_id, location_id);
            insertPageContentToMysql(Integer.valueOf(tableRecord.getTaxon().getPageEolId()), medium_id);
            if(medium.getAgents()!= null)
                insertAgentstoMysql(medium.getAgents(), medium_id, medium.getMediaId());
            if(tableRecord.getReferences() != null && medium.getReferenceId() != null)
                insertReferencesToMysql(tableRecord.getReferences(), medium.getReferenceId(), medium_id);
        }
    }

    private int insertMediumToMysql(Media medium, int language_id, int license_id, int location_id){

        System.out.println("insert new medium");
        StoredProcedureQuery insertMedium = entityManager
                .createStoredProcedureQuery("insertMedium")
                .registerStoredProcedureParameter(
                        "format_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "description_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "owner_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "guid_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "resource_pk_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "source_page_url_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "language_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "license_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "location_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "base_url_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "medium_id", Integer.class, ParameterMode.OUT);

        insertMedium.setParameter("format_p", medium.getFormat());
        insertMedium.setParameter("description_p", medium.getDescription());
        insertMedium.setParameter("owner_p", medium.getOwner());
        insertMedium.setParameter("resource_id_p", resourceID);
        insertMedium.setParameter("guid_p", String.valueOf(generateMediaGUID()));
        insertMedium.setParameter("resource_pk_p", medium.getMediaId());
        insertMedium.setParameter("source_page_url_p", PropertiesHandler.getProperty("storageLayerIp")+medium.getFurtherInformationURI());
        insertMedium.setParameter("language_id_p", language_id);
        insertMedium.setParameter("license_id_p", license_id);
        insertMedium.setParameter("location_id_p", location_id);
        insertMedium.setParameter("base_url_p", medium.getStorageLayerPath());
        insertMedium.setParameter("created_at_p", new Date());
        insertMedium.setParameter("updated_at_p", new Date());

        try {
            insertMedium.execute();
            return (int) insertMedium.getOutputParameterValue("medium_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    private void insertPageContentToMysql(int page_id, int medium_id){

        System.out.println("insert new page_content");
        StoredProcedureQuery insertPageContent = entityManager
                .createStoredProcedureQuery("insertPageContent")
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "page_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "source_page_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "content_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "content_type_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN);

        insertPageContent.setParameter("resource_id_p", resourceID);
        insertPageContent.setParameter("page_id_p", page_id);
        insertPageContent.setParameter("source_page_id_p", page_id);
        insertPageContent.setParameter("content_id_p", medium_id);
        insertPageContent.setParameter("content_type_p", "Medium");
        insertPageContent.setParameter("created_at_p", new Date());
        insertPageContent.setParameter("updated_at_p", new Date());

        try {
            insertPageContent.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    private void insertAgentstoMysql(ArrayList<Agent> agents, int medium_id, String content_resource_fk){

        for(Agent agent : agents){
            System.out.println("insert new agent");
            String role_name = agent.getRole() == null ? "roletest" : agent.getRole();
            StoredProcedureQuery insertAgent = entityManager
                    .createStoredProcedureQuery("insertAgent")
                    .registerStoredProcedureParameter(
                            "resource_id_p", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "content_id_p", Integer.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "content_type_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "role_name_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "url_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "resource_pk_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "value_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "content_resource_fk_p", String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "created_at_p", Date.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(
                            "updated_at_p", Date.class, ParameterMode.IN);

            insertAgent.setParameter("resource_id_p", resourceID);
            insertAgent.setParameter("content_id_p", medium_id);
            insertAgent.setParameter("content_type_p", "Medium");
            insertAgent.setParameter("role_name_p", role_name);
            insertAgent.setParameter("url_p", agent.getHomepage());
            insertAgent.setParameter("resource_pk_p", agent.getAgentId());
            insertAgent.setParameter("value_p", agent.getFullName());
            insertAgent.setParameter("content_resource_fk_p", content_resource_fk);
            insertAgent.setParameter("created_at_p", new Date());
            insertAgent.setParameter("updated_at_p", new Date());

            try {
                insertAgent.execute();
            }catch (Exception e){
                System.out.println("duplicate line");
            }
        }
    }

    private int insertLanguageToMysql(String code){
        System.out.println("insert new language");
        if(code == null)
            code="eng";
        StoredProcedureQuery insertLanguage = entityManager
                .createStoredProcedureQuery("insertLanguage")
                .registerStoredProcedureParameter(
                        "code_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "group_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "language_id", Integer.class, ParameterMode.OUT);

        insertLanguage.setParameter("code_p", code);
        insertLanguage.setParameter("group_p", code);
        insertLanguage.setParameter("created_at_p", new Date());
        insertLanguage.setParameter("updated_at_p", new Date());

        try {
            insertLanguage.execute();
            return (int) insertLanguage.getOutputParameterValue("language_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    private int insertLicenseToMysql(String source_url){
        System.out.println("insert new license");
        if(source_url == null)
            source_url="test";
        StoredProcedureQuery insertLicense = entityManager
                .createStoredProcedureQuery("insertLicense")
                .registerStoredProcedureParameter(
                        "source_url_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "name_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "license_id", Integer.class, ParameterMode.OUT);

        insertLicense.setParameter("source_url_p", source_url);
        insertLicense.setParameter("name_p", "license");
        insertLicense.setParameter("created_at_p", new Date());
        insertLicense.setParameter("updated_at_p", new Date());

        try {
            insertLicense.execute();
            return (int) insertLicense.getOutputParameterValue("license_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    private int insertLocationToMysql(Media medium){

        System.out.println("insert new location");
        StoredProcedureQuery insertLocation = entityManager
                .createStoredProcedureQuery("insertLocation")
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "location_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "longitude_p", Float.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "latitude_p", Float.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "altitude_p", Float.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "spatial_location_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "location_id", Integer.class, ParameterMode.OUT);

        insertLocation.setParameter("resource_id_p", resourceID);
        insertLocation.setParameter("location_p", medium.getLocationCreated());
        insertLocation.setParameter("longitude_p", medium.getLongitude());
        insertLocation.setParameter("latitude_p", Float.valueOf(medium.getLatitude()));
        insertLocation.setParameter("altitude_p", Float.valueOf(medium.getAltitude()));
        insertLocation.setParameter("spatial_location_p", Float.valueOf(medium.getGenericLocation()));
        insertLocation.setParameter("created_at_p", new Date());
        insertLocation.setParameter("updated_at_p", new Date());

        try {
            insertLocation.execute();
            return (int) insertLocation.getOutputParameterValue("location_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    private void insertReferencesToMysql(ArrayList<Reference> references, String reference_id, int medium_id) {
        String [] media_references = reference_id.split(";");
        for(int i=0; i< media_references.length; i++){
            for(Reference reference : references){
                if(reference.getReferenceId().equals(media_references[i])){
                    String body = reference.getPrimaryTitle()+" "+reference.getSecondaryTitle()+" "+reference.getPages()+" "+reference.getPageStart()+" "+
                            reference.getPageEnd()+" "+reference.getVolume()+" "+reference.getEditorsList()+" "+reference.getPublisher()+" "+
                            reference.getAuthorsList()+" "+reference.getEditorsList()+" "+reference.getDateCreated()+" "+reference.getDoi();
                    int referent_id = insertReferentToMysql(body);
                    insertReferenceToMysql(referent_id, medium_id);
                }
            }
        }
    }

    private int insertReferentToMysql(String body) {
        System.out.println("insert new referent");
        StoredProcedureQuery insertReferent = entityManager
                .createStoredProcedureQuery("insertReferent")
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "body_p", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "referent_id", Integer.class, ParameterMode.OUT);

        insertReferent.setParameter("resource_id_p", resourceID);
        insertReferent.setParameter("body_p", body);
        insertReferent.setParameter("created_at_p", new Date());
        insertReferent.setParameter("updated_at_p", new Date());

        try {
            insertReferent.execute();
            return (int) insertReferent.getOutputParameterValue("referent_id");
        }catch (Exception e){
            System.out.println("duplicate line");
            return -1;
        }
    }

    private void insertReferenceToMysql(int referent_id, int medium_id) {
        System.out.println("insert new reference");
        StoredProcedureQuery insertReference = entityManager
                .createStoredProcedureQuery("insertReference")
                .registerStoredProcedureParameter(
                        "parent_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "referent_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "resource_id_p", Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "parent_type_id", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "created_at_p", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter(
                        "updated_at_p", Date.class, ParameterMode.IN);

        insertReference.setParameter("parent_id_p", medium_id);
        insertReference.setParameter("referent_id_p", referent_id);
        insertReference.setParameter("resource_id_p", resourceID);
        insertReference.setParameter("parent_type_id", "Medium");
        insertReference.setParameter("created_at_p", new Date());
        insertReference.setParameter("updated_at_p", new Date());

        try {
            insertReference.execute();
        }catch (Exception e){
            System.out.println("duplicate line");
        }
    }

    private UUID generateMediaGUID()
    {
        return UUID.randomUUID();
    }
}
