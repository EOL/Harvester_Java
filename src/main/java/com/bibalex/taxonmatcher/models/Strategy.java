package com.bibalex.taxonmatcher.models;

/**
 * Created by Amr.Morad on 3/12/2017.
 */
public class Strategy {

    String attribute;
    String index;
    String type;

    public Strategy(String attribute, String index, String type) {
        this.attribute = attribute;
        this.index = index;
        this.type = type;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public void setAttribute(String attribute) {

        this.attribute = attribute;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }
}
