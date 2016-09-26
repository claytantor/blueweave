package com.dronze.blueweave.model;

import org.nd4j.linalg.dataset.DataSet;

import java.util.Map;

/**
 * Created by claytongraham on 9/24/16.
 */
public class DateSeriesDataSetModel {
    private DataSet dataSet;
    private Map<Integer,String> datumNames;
    private Map<Integer,String> seriesNames;
    private Map<Integer, Integer> labels;

    public DateSeriesDataSetModel(DataSet dataSet, Map<Integer, String> datumNames,
                                  Map<Integer, String> seriesNames, Map<Integer, Integer> labels) {
        this.dataSet = dataSet;
        this.datumNames = datumNames;
        this.seriesNames = seriesNames;
        this.labels = labels;
    }

    public Map<Integer, Integer> getLabels() {
        return labels;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public Map<Integer, String> getDatumNames() {
        return datumNames;
    }

    public Map<Integer, String> getSeriesNames() {
        return seriesNames;
    }
}
