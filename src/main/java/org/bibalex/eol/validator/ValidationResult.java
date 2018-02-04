package org.bibalex.eol.validator;

import java.util.*;


public class ValidationResult {
    private List<String> structuralErrors;
    private String state;
    private HashMap<String, List<String>> rowByRowErrors;
    private List<String> structuralWarnings;
    private HashMap<String, List<String>> rowByRowWarnings;
    private String resourceName;


    public ValidationResult(String resourceName) {
        this.resourceName = resourceName;
        this.structuralErrors = new LinkedList<String>();
        this.rowByRowErrors = new HashMap<String, List<String>>();
        this.structuralWarnings = new LinkedList<String>();
        this.rowByRowWarnings = new HashMap<String, List<String>>();
    }

    public void addStructuralError(String error) {
        this.structuralErrors.add(error);
    }

//    public void addRowByRowError(String error) {
//        this.rowByRowErrors.add(error);
//    }

    public void addRowByRowError(String rowType, String error) {
        List<String> errorsList;
        if (this.rowByRowErrors.containsKey(rowType))
            errorsList = rowByRowErrors.get(rowType);
        else
            errorsList = new ArrayList<String>();
        errorsList.add(error);
        this.rowByRowErrors.put(rowType, errorsList);
    }

    public void addStructuralWarning(String warnning) {
        this.structuralWarnings.add(warnning);
    }

//    public void addRowByRowWarning(String error) {
//        this.rowByRowWarnings.add(error);
//    }

    public void addRowByRowWarning(String rowType, String error) {
        List<String> warningsList;
        if (this.rowByRowWarnings.containsKey(rowType))
            warningsList = rowByRowWarnings.get(rowType);
        else
            warningsList = new ArrayList<String>();
        warningsList.add(error);
        this.rowByRowWarnings.put(rowType, warningsList);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<String> getStructuralErrors() {
        return structuralErrors;
    }

    public List<String> getRowByRowErrors() {
        return flattenMap(rowByRowErrors);
    }

    public List<String> getStructuralWarnings() {
        return structuralWarnings;
    }

    public List<String> getRowByRowWarnings() {
        return flattenMap(rowByRowWarnings);
    }

    private List<String> flattenMap(HashMap<String, List<String>> map) {
        List<String> result = new ArrayList<String>();
        Set<String> keys = map.keySet();
        for (String k : keys) {
            List<String> list = map.get(k);
            for (String element : list) {
                result.add(k + "  ->  " + element);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Row by row Errors : \n");
        if (rowByRowErrors.isEmpty()) {
            sb.append(" *  No rowByRowErrors found\n");
        } else {
            Set<String> rowTypes = rowByRowErrors.keySet();
            for (String rt : rowTypes) {
                List<String> list = rowByRowErrors.get(rt);
                sb.append(" *  File : " + rt + " : \n");
                for (String element : list) {
                    sb.append("    -   "+element+"\n");
                }
            }
        }

        sb.append("Row by row Warnings : \n");
        if (rowByRowWarnings.isEmpty()) {
            sb.append(" *  No rowByRowWarnings found\n");
        } else {
            Set<String> rowTypes = rowByRowWarnings.keySet();
            for (String rt : rowTypes) {
                List<String> list = rowByRowWarnings.get(rt);
                sb.append(" *  File : " + rt + " : \n");
                for (String element : list) {
                    sb.append("    -   "+element+"\n");
                }
            }
        }
        sb.append("Structural Errors : \n");
        if (structuralErrors.isEmpty()) {
            sb.append(" *  No structuralErrors found\n");
        } else {
            for (String structuralError : structuralErrors) {
                sb.append(" *  " + structuralError + " \n");
            }
        }
        sb.append("Structural Warnings : \n");
        if (structuralWarnings.isEmpty()) {
            sb.append(" *  No structuralWarnings found\n");
        } else {
            for (String structuralWarn : structuralWarnings) {
                sb.append(" *  " + structuralWarn + " \n");
            }
        }


        return sb.toString();
    }
}