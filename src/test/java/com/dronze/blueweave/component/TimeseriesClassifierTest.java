package com.dronze.blueweave.component;

import com.dronze.blueweave.json.GsonFactory;
import com.dronze.blueweave.model.SequenceNetworkModel;
import com.dronze.blueweave.util.DateUtils;
import com.dronze.blueweave.util.MustacheUtils;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Created by claytongraham on 9/27/16.
 */
public class TimeseriesClassifierTest {

    private static Logger log = LoggerFactory.getLogger(TimeseriesClassifierTest.class);

    @Test
    public void testTimeseriesClassifierBasic(){


        try {
            Table<Date, String, Double> trainingTable = GsonFactory.fromJson(
                    IOUtils.toString(
                            getClass().getResourceAsStream("/data/01/train/trainTable01.json"), "UTF-8"),
                    TreeBasedTable.class, GsonFactory.Type.DEFAULT);

            //make the training model
            SequenceNetworkModel trainingModel = GsonFactory.fromJson(
                    IOUtils.toString(
                            getClass().getResourceAsStream("/data/01/train/trainModel01.json"), "UTF-8"),
                    SequenceNetworkModel.class, GsonFactory.Type.DEFAULT);


            //get the training model
            Table<Date, String, Double> testingTable = GsonFactory.fromJson(
                    IOUtils.toString(
                            getClass().getResourceAsStream("/data/01/test/testTable01.json"), "UTF-8"),
                    TreeBasedTable.class, GsonFactory.Type.DEFAULT);


            //get the config from the classpath
            Config conf = ConfigFactory.load();

            ClassifierNetwork network = new TimeseriesClassifierNetwork.TimeseriesClassifierNetworkBuilder()
                    .setNetworkClasses(trainingModel.getNetworkClasses())
                    .setTrainClassifications(trainingModel.getNetworkClassifications())
                    .setTrainTable(trainingTable)
                    .setTestTable(testingTable)
                    .setConfig(conf,"TimeseriesClassifierNetwork")
                    .build();

            Map<String,Object> model = new HashedMap();
            model.put("startDate", DateUtils.min(testingTable.rowKeySet()));
            model.put("endDate", DateUtils.max(testingTable.rowKeySet()));

            Table<Integer, String, Object> result = network.evaluate();

            log.info(MustacheUtils.merge("start:{{startDate}}, end:{{endDate}}", model));
            result.rowKeySet().forEach(rowId->{
                log.info(MustacheUtils.merge("{{seriesNumber}},{{seriesName}},{{classificationName}}", result.row(rowId)));
            });


        } catch (Exception e){
            log.error(e.getMessage(),e);
            Assert.fail(e.getMessage());
        }

    }

}
