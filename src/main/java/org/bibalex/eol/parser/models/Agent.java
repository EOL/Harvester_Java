package org.bibalex.eol.parser.models;


public class Agent {
    String agentId;
    String fullName;
    String firstName;
    String familyName;
    String role;
    String email;
    String homepage;
    String logoURL;
    String project;
    String organization;
    String accountName;
    String openId;
    String deltaStatus;

    public Agent(String agentId, String fullName, String firstName, String familyName, String role,
                 String email, String homepage, String logoURL, String project, String organization, String accountName, String openId) {
        this.agentId = agentId;
        this.fullName = fullName;
        this.firstName = firstName;
        this.familyName = familyName;
        this.role = role;
        this.email = email;
        this.homepage = homepage;
        this.logoURL = logoURL;
        this.project = project;
        this.organization = organization;
        this.accountName = accountName;
        this.openId = openId;
    }

    public String getAgentId() {

        return agentId;
    }

    public String getDeltaStatus() {
        return deltaStatus;
    }

    public void setDeltaStatus(String deltaStatus) {
        this.deltaStatus = deltaStatus;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
