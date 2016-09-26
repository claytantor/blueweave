package com.dronze.blueweave.model;

/**
 * Created by claytongraham on 9/25/16.
 */
public class NetworkClassification {
    String name;
    Integer classId;

    public NetworkClassification(){}
    public NetworkClassification(String name, Integer classId) {
        this.name = name;
        this.classId = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getClassId() {
        return classId;
    }

    public void setClassId(Integer classId) {
        this.classId = classId;
    }
}
