package org.bibalex.eol.parser.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
//import org.bibalex.eol.scheduler.content_partner.ContentPartner;
//import org.bibalex.eol.scheduler.harvest.Harvest;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

//@Entity
//@Table(name = "taxons")
//@NamedStoredProcedureQueries({
//        @NamedStoredProcedureQuery(
//                name = "getcount_sp",
//                procedureName = "getcount",
//                resultClasses = { Integer.class })
//        })

public class Neo4jTaxon {
//    @Id
//    @Column(name = "taxon_id")
    String taxon_id;
//    @Id
//    @Column(name = "resource_id")
    int resource_id;
//    @Column(name = "parent_id")
    String parent_id;
//    @Column(name = "scientific_name")
    String scientific_name;
//    @Column(name = "rank")
    String rank;
//    @Column(name = "page_id")
    int page_id;
    boolean created;

    public Neo4jTaxon(){

    }

    public Neo4jTaxon(String taxon_id, int resource_id, String parent_id, String scientific_name, String rank, int page_id, boolean created){
        this.taxon_id=taxon_id;
        this.resource_id=resource_id;
        this.parent_id=parent_id;
        this.scientific_name=scientific_name;
        this.rank=rank;
        this.page_id=page_id;
        this.created=created;
    }

    public void setTaxon_id(String taxon_id) {
        this.taxon_id = taxon_id;
    }

    public String getTaxon_id() {
        return taxon_id;
    }

    public void setResource_id(int resource_id) {
        this.resource_id = resource_id;
    }

    public int getResource_id() {
        return resource_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRank() {
        return rank;
    }

    public void setPage_id(int page_id) {
        this.page_id = page_id;
    }

    public int getPage_id() {
        return page_id;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public boolean isCreated() {
        return created;
    }

    //    public enum Type {
//        url,
//        file
//    }
//    public enum HarvestFrequency{
//        once,
//        weekly,
//        monthly,
//        bimonthly,
//        quarterly
//    }
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String name;
//    @Column(name="origin_url")
//    private String originUrl;
//    @Column(name="uploaded_url")
//    private String uploadedUrl;
//    @Enumerated(EnumType.STRING)
//    private Resource.Type type;
//    private String path;
//    @Column(name="last_harvested_at")
//    private Date lastHarvestedAt;
//    @Convert(converter = HarvestFreqConverter.class)
//    @Column(name="harvest_frequency")
//    private Resource.HarvestFrequency harvestFrequency;
//    @Range(min = 0, max = 31)
//    @Column(name="day_of_month")
//    private int dayOfMonth=0;
//    @Column(name="nodes_count")
//    private int nodesCount;
//    private int position = -1;
//    @Column(name="is_paused")
//    private boolean paused = false;
//    @Column(name="is_harvest_inprogress")
//    private boolean harvestInprogress = false;
//    @Column(name="is_approved")
//    private boolean approved = false;
//    @Column(name="is_trusted")
//    private boolean trusted = false;
//    @Column(name="forced_internally")
//    private boolean forcedInternally = false;
//    @Column(name="is_autopublished")
//    private boolean autopublished = false;
//    @Column(name="is_forced")
//    private boolean forced = false;
//    @Column(name="dataset_license")
//    private int datasetLicense = 47;
//    @Column(name="dataset_rights_statement")
//    private String datasetRightsStatement;
//    @Column(name="dataset_rights_holder")
//    private String datasetRightsHolder;
//    @Column(name="default_license_string")
//    private int defaultLicenseString;
//    @Column(name="defaultRightsStatement")
//    private String defaultRightsStatement;
//    @Column(name="default_rights_holder")
//    private String defaultRightsHolder;
//    @Column(name="default_language_id")
//    private int defaultLanguageId = 152;
////    @Column(name="created_at")
////    private Date createdAt;
////    @Column(name="updated_at")
////    private Date updatedAt;
//
//    @ManyToOne
//    @JoinColumn (name="content_partner_id")
//    @JsonBackReference
//    private ContentPartner contentPartner;
//    @OneToMany(mappedBy ="resource")
//    private Set<Harvest> harvests = new HashSet<>();
//
//    public Neo4jTaxon(){
//    }
//
//    public Neo4jTaxon(long id){
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//
//    public String getOriginUrl() {
//        return originUrl;
//    }
//
//    public String getUploadedUrl() {
//        return uploadedUrl;
//    }
//
//    public Resource.Type getType() {
//        return type;
//    }
//
//    public String getPath() {
//        return path;
//    }
//
//    public Date getLastHarvestedAt() {
//        return lastHarvestedAt;
//    }
//
//    public Resource.HarvestFrequency getHarvestFrequency() {
//        return harvestFrequency;
//    }
//
//    public int getDayOfMonth() {
//        return dayOfMonth;
//    }
//
//    public int getNodesCount() {
//        return nodesCount;
//    }
//
//    public int getPosition() {
//        return position;
//    }
//
//    public boolean isPaused() {
//        return paused;
//    }
//
//    public boolean isHarvestInprogress() {
//        return harvestInprogress;
//    }
//
//    public boolean isApproved() {
//        return approved;
//    }
//
//    public boolean isTrusted() {
//        return trusted;
//    }
//
//    public boolean isForcedInternally() {
//        return forcedInternally;
//    }
//
//    public boolean isAutopublished() {
//        return autopublished;
//    }
//
//    public boolean isForced() {
//        return forced;
//    }
//
//    public int getDatasetLicense() {
//        return datasetLicense;
//    }
//
//    public String getDatasetRightsStatement() {
//        return datasetRightsStatement;
//    }
//
//    public String getDatasetRightsHolder() {
//        return datasetRightsHolder;
//    }
//
//    public int getDefaultLicenseString() {
//        return defaultLicenseString;
//    }
//
//    public String getDefaultRightsStatement() {
//        return defaultRightsStatement;
//    }
//
//    public String getDefaultRightsHolder() {
//        return defaultRightsHolder;
//    }
//
//    public int getDefaultLanguageId() {
//        return defaultLanguageId;
//    }
//
////    public Date getCreatedAt() {
////        return createdAt;
////    }
////
////    public Date getUpdatedAt() {
////        return updatedAt;
////    }
//
//    public ContentPartner getContentPartner() {
//        return contentPartner;
//    }
//
//    public Set<Harvest> getHarvests() {
//        return harvests;
//    }
//
//    public void setOriginUrl(String originUrl) {
//        this.originUrl = originUrl;
//    }
//
//    public void setUploadedUrl(String uploadedUrl) {
//        this.uploadedUrl = uploadedUrl;
//    }
//
//    public void setType(Resource.Type type) {
//        this.type = type;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
//
//    public void setLastHarvestedAt(Date lastHarvestedAt) {
//        this.lastHarvestedAt = lastHarvestedAt;
//    }
//
//    public void setHarvestFrequency(Resource.HarvestFrequency harvestFrequency) {
//        this.harvestFrequency = harvestFrequency;
//    }
//
//    public void setDayOfMonth(int dayOfMonth) {
//        this.dayOfMonth = dayOfMonth;
//    }
//
//    public void setNodesCount(int nodesCount) {
//        this.nodesCount = nodesCount;
//    }
//
//    public void setPosition(int position) {
//        this.position = position;
//    }
//
//    public void setPaused(boolean paused) {
//        this.paused = paused;
//    }
//
//    public void setHarvestInprogress(boolean harvestInprogress) {
//        this.harvestInprogress = harvestInprogress;
//    }
//
//    public void setApproved(boolean isApproved) {
//        this.approved = isApproved;
//    }
//
//    public void setTrusted(boolean trusted) {
//        this.trusted = trusted;
//    }
//
//    public void setForcedInternally(boolean forcedInternally) {
//        this.forcedInternally = forcedInternally;
//    }
//
//    public void setAutopublished(boolean autopublished) {
//        this.autopublished = autopublished;
//    }
//
//    public void setForced(boolean forced) {
//        this.forced = forced;
//    }
//
//    public void setDatasetLicense(int datasetLicense) {
//        this.datasetLicense = datasetLicense;
//    }
//
//    public void setDatasetRightsStatement(String datasetRightsStatement) {
//        this.datasetRightsStatement = datasetRightsStatement;
//    }
//
//    public void setDatasetRightsHolder(String datasetRightsHolder) {
//        this.datasetRightsHolder = datasetRightsHolder;
//    }
//
//    public void setDefaultLicenseString(int defaultLicenseString) {
//        this.defaultLicenseString = defaultLicenseString;
//    }
//
//    public void setDefaultRightsStatement(String defaultRightsStatement) {
//        this.defaultRightsStatement = defaultRightsStatement;
//    }
//
//    public void setDefaultRightsHolder(String defaultRightsHolder) {
//        this.defaultRightsHolder = defaultRightsHolder;
//    }
//
//    public void setDefaultLanguageId(int defaultLanguageId) {
//        this.defaultLanguageId = defaultLanguageId;
//    }
//
////    public void setCreatedAt(Date createdAt) {
////        this.createdAt = createdAt;
////    }
////
////    public void setUpdatedAt(Date updatedAt) {
////        this.updatedAt = updatedAt;
////    }
//
//    public void setContentPartner(ContentPartner contentPartner) {
//        this.contentPartner = contentPartner;
//    }
//
//    public void setHarvests(Set<Harvest> harvests) {
//        this.harvests = harvests;
//    }
}
