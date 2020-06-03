package org.bibalex.eol.parser.models;

import java.util.ArrayList;

public class Article {
    String taxonId;
    String mediaId;
    String resourceId;
    String subject;
    String title;
    String description;
    String furtherInformationURI;
    String derivedFrom;
    String createDate;
    String modified;
    String language;
    String rating;
    String audience;
    String license;
    String rights;
    String owner;
    String bibliographicCitation;
    String publisher;
    String contributor;
    String creator;
    String referenceId;
    String deltaStatus;
    ArrayList<Agent> agents;
    String storageLayerPath;
    String storageLayerThumbnailPath;
    String controlSection;
    String format;

    public Article(String taxonId, String mediaId, String resourceId, String subject,
                   String title, String description, String furtherInformationURI, String derivedFrom, String createDate,
                   String modified, String language, String rating, String audience, String license, String rights,
                   String owner, String bibliographicCitation, String publisher, String contributor,
                   String creator, String referenceId, String deltaStatus,String storageLayerPath,
                   String storageLayerThumbnailPath, String controlSection,String format ) {
        this.taxonId = taxonId;
        this.mediaId = mediaId;
        this.resourceId = resourceId;
        this.subject = subject;
        this.title = title;
        this.description = description;
        this.furtherInformationURI = furtherInformationURI;
        this.derivedFrom = derivedFrom;
        this.createDate = createDate;
        this.modified = modified;
        this.language = language;
        this.rating = rating;
        this.audience = audience;
        this.license = license;
        this.rights = rights;
        this.owner = owner;
        this.bibliographicCitation = bibliographicCitation;
        this.publisher = publisher;
        this.contributor = contributor;
        this.creator = creator;
        this.referenceId = referenceId;
        this.deltaStatus = deltaStatus;
        this.storageLayerPath = storageLayerPath;
        this.storageLayerThumbnailPath = storageLayerThumbnailPath;
        this.controlSection = controlSection;
        if(this.format != null) {
            this.format = this.format.replace("/", "_");
            this.format = this.format.replace("+", "$");
            this.format = this.format.replace("-", "_$");

        }
    }

    public String getTaxonId() {
        return taxonId;
    }

    public void setTaxonId(String taxonId) {
        this.taxonId = taxonId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFurtherInformationURI() {
        return furtherInformationURI;
    }

    public void setFurtherInformationURI(String furtherInformationURI) {
        this.furtherInformationURI = furtherInformationURI;
    }

    public String getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getBibliographicCitation() {
        return bibliographicCitation;
    }

    public void setBibliographicCitation(String bibliographicCitation) {
        this.bibliographicCitation = bibliographicCitation;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public ArrayList<Agent> getAgents() {
        return agents;
    }

    public void setAgents(ArrayList<Agent> agents) {
        this.agents = agents;
    }


    public String getControlSection() {
        return controlSection;
    }

    public void setControlSection(String controlSection) {
        this.controlSection = controlSection;
    }

}
