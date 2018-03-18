package org.bibalex.eol.parser.models;

/**
 * Created by Amr Morad
 * This class will be used to cover the kingdom phylum... file format
 */
public class AncestorNode {
    String rank;
    String scientificName;

    public AncestorNode(String rank, String scientificName) {
        this.rank = rank;
        this.scientificName = scientificName;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
}
