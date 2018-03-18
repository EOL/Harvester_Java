package org.bibalex.eol.validator;


public class ArchiveFileState {

    private int totalNumberOfLines;
    private int linesViolatingRule;
    private boolean allLinesViolating = false;
    private boolean allLinesComplying = false;

    public ArchiveFileState() {
    }

    public ArchiveFileState(int lines, int violatingLines) {
        this.totalNumberOfLines = lines;
        this.linesViolatingRule = violatingLines;
//        if (totalNumberOfLines == linesViolatingRule)
//            allLinesViolating = true;
    }

    public ArchiveFileState(boolean allLinesViolating) {
        this.allLinesViolating = allLinesViolating;
    }


    public boolean isAllLinesViolating() {
        return allLinesViolating;
    }

    public void setAllLinesViolating(boolean allLinesViolating) {
        this.allLinesViolating = allLinesViolating;
    }


    public int getLinesViolatingRule() {
        return linesViolatingRule;
    }

    public void setLinesViolatingRule(int linesViolatingRule) {
        this.linesViolatingRule = linesViolatingRule;
    }

    public int getTotalNumberOfLines() {
        return totalNumberOfLines;
    }

    public void setTotalNumberOfLines(int totalNumberOfLines) {
        this.totalNumberOfLines = totalNumberOfLines;
    }

    public boolean isAllLinesComplying() {
        return allLinesComplying;
    }

    public void setAllLinesComplying(boolean allLinesComplying) {
        this.allLinesComplying = allLinesComplying;
    }
}

