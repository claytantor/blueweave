package com.dronze.blueweave.model;

/**
 * Created by claytongraham on 9/25/16.
 */
public class NetworkClass {
    private Integer id;
    private String name;

    public NetworkClass(){}
    public NetworkClass(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
