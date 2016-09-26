package com.dronze.blueweave.model;

import com.google.common.collect.Table;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by claytongraham on 9/25/16.
 * {
 "startDate":"2016-04-01",
 "endDate":"2016-08-01",
 "classes":[
 {"id":0,"name":"cyclic"},
 {"id":1,"name":"upward-trend"},
 {"id":2,"name":"downward-trend"},
 {"id":3,"name":"upward-shift"},
 {"id":4,"name":"downward-shift"}
 ],
 "classifications":[
 {"name":"PACW", "class":0},
 {"name":"PAG",  "class":0},
 {"name":"PAHC", "class":4},
 {"ticker":"PANW", "class":2},
 {"name":"PATK", "class":3},
 {"name":"PATR", "class":3},
 {"name":"PAY",  "class":4},
 {"name":"PAYC",  "class":1},
 {"name":"PAYX",  "class":3}
 ]
 }
 */
public class SequenceNetworkModel {
    List<NetworkClass> networkClasses;
    List<NetworkClassification> networkClassifications;
    Table<Date,String,Double> sequenceData;

    public SequenceNetworkModel(){}

    public SequenceNetworkModel(Table<Date,String,Double> sequenceData,
                                List<NetworkClass> classes, List<NetworkClassification> classifications) {
        this.sequenceData = sequenceData;
        this.networkClasses = classes;
        this.networkClassifications = classifications;
    }

    public Map<Integer,String> makeNetworkClassTypesMap(){
        Map<Integer,String> map = new TreeMap<>();
        networkClasses.forEach(classValue->{
            map.put(classValue.getId(),classValue.getName());
        });
        return map;
    }

    public Map<String,Integer> makeNetworkClassificationMap(){
        Map<String,Integer> map = new TreeMap<>();
        networkClassifications.forEach(classification->{
            map.put(classification.getName(),classification.getClassId());
        });
        return map;
    }


    public Table<Date, String, Double> getSequenceData() {
        return sequenceData;
    }

    public void setSequenceData(Table<Date, String, Double> sequenceData) {
        this.sequenceData = sequenceData;
    }

    public List<NetworkClass> getNetworkClasses() {
        return networkClasses;
    }

    public void setNetworkClasses(List<NetworkClass> networkClasses) {
        this.networkClasses = networkClasses;
    }

    public List<NetworkClassification> getNetworkClassifications() {
        return networkClassifications;
    }

    public void setNetworkClassifications(List<NetworkClassification> networkClassifications) {
        this.networkClassifications = networkClassifications;
    }
}
