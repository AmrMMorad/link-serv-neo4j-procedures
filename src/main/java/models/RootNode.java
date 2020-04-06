package models;

import constants.Constants;

import java.util.Map;

public class RootNode {
    public String nodeId;
    public String parentName;
    public String versionName;

    public RootNode(Map<String, Object> data) {
        this.nodeId = String.valueOf(data.get("ID(v)"));
        this.parentName = String.valueOf(data.get("parent." + Constants.nameProperty));
        this.versionName = String.valueOf(data.get("v." + Constants.versionProperty));
    }

}