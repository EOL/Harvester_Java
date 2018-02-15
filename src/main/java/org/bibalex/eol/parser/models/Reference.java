package org.bibalex.eol.parser.models;

/**
 * As this is the harvester, it will put all the fields in HBase. This, in turn, will be transformed to bytes array. No need for casting
 * because of that.
 */
public class Reference {
    String referenceId;
    String publicationType;
    String fullReference;
    String primaryTitle;
    String secondaryTitle;
    String pages;
    String pageStart;
    String pageEnd;
    String volume;
    String edition;
    String publisher;
    String authorsList;
    String editorsList;
    String dateCreated;
    String language;
    String url;
    String doi;
    String localityOfPublisher;
    String deltaStatus;
    String action;

    public Reference(String referenceId, String publicationType, String fullReference, String primaryTitle, String secondaryTitle,
                     String pages, String pageStart, String pageEnd, String volume, String edition, String publisher, String authorsList,
                     String editorsList, String dateCreated, String language, String url, String doi, String localityOfPublisher) {
        this.referenceId = referenceId;
        this.publicationType = publicationType;
        this.fullReference = fullReference;
        this.primaryTitle = primaryTitle;
        this.secondaryTitle = secondaryTitle;
        this.pages = pages;
        this.pageStart = pageStart;
        this.pageEnd = pageEnd;
        this.volume = volume;
        this.edition = edition;
        this.publisher = publisher;
        this.authorsList = authorsList;
        this.editorsList = editorsList;
        this.dateCreated = dateCreated;
        this.language = language;
        this.url = url;
        this.doi = doi;
        this.localityOfPublisher = localityOfPublisher;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(String publicationType) {
        this.publicationType = publicationType;
    }

    public String getFullReference() {
        return fullReference;
    }

    public void setFullReference(String fullReference) {
        this.fullReference = fullReference;
    }

    public String getPrimaryTitle() {
        return primaryTitle;
    }

    public void setPrimaryTitle(String primaryTitle) {
        this.primaryTitle = primaryTitle;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPageStart() {
        return pageStart;
    }

    public void setPageStart(String pageStart) {
        this.pageStart = pageStart;
    }

    public String getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(String pageEnd) {
        this.pageEnd = pageEnd;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthorsList() {
        return authorsList;
    }

    public void setAuthorsList(String authorsList) {
        this.authorsList = authorsList;
    }

    public String getEditorsList() {
        return editorsList;
    }

    public void setEditorsList(String editorsList) {
        this.editorsList = editorsList;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getLocalityOfPublisher() {
        return localityOfPublisher;
    }

    public void setLocalityOfPublisher(String localityOfPublisher) {
        this.localityOfPublisher = localityOfPublisher;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
