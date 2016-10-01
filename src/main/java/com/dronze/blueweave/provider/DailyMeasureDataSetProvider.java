package com.dronze.blueweave.provider;

import com.dronze.blueweave.model.DateSeriesDataSetModel;
import com.dronze.blueweave.util.DateUtils;
import com.google.common.collect.Table;
import org.deeplearning4j.datasets.iterator.ExistingDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by claytongraham on 9/17/16.
 */
public class DailyMeasureDataSetProvider {

    private static Logger log = LoggerFactory.getLogger(DailyMeasureDataSetProvider.class);


    @PostConstruct
    public void init() throws DataSetProviderException, ParseException {
    }

    /**
     * creates a fully mapped model of the date series table as a dataset that can be later mapped
     * and correlated back to to original model for classification. No information is lost, so pointers
     * and references back to the source metadata dont need to be maintained.
     *
     * @param indicatorForDateRange the value table for each series
     * @param trainingClassifiers The label map for the dataset, needed for training
     * @return
     * @throws DataSetProviderException
     */
    public DateSeriesDataSetModel getSeriesValuesForTable(Table<Date,String,Double> indicatorForDateRange,
                                                          Map<String,Integer> trainingClassifiers)
            throws DataSetProviderException
    {

        //labels without classifiers, overwrite with training classification if exists
        Map<Integer,Integer> labels = makeEmptyLabels(
            indicatorForDateRange.columnKeySet().size(), indicatorForDateRange.rowKeySet().size());

        Map<Integer,String> seriesNames = new HashMap<>(); //series
        Map<Integer,String> datumNames = new HashMap<>(); //dates

        Map<String,List<Double>> dataListMap = new HashMap<>();

        //make the buffer for each
        indicatorForDateRange.columnKeySet().forEach(ticker->{
            dataListMap.put(ticker,new ArrayList<>());
        });

        // we dont know what dates exist for each value so we need a
        // way to normalize values in the series
        Set<Date> commonDates = new HashSet<>();
        List<Date> dates = new ArrayList<Date>(indicatorForDateRange.rowKeySet());
        Collections.sort(dates);

        dates.forEach(date->{
            List<String> commonTickersForDate = indicatorForDateRange.columnKeySet().stream()
                    .filter(ticker -> indicatorForDateRange.contains(date,ticker))
                    .collect(Collectors.toList());

            // enforce date entries be common across tickers, this is because pagination
            // needs to be ientical across the series
            if(commonTickersForDate.size() != indicatorForDateRange.columnKeySet().size()){
                log.error("NOT ALL SERIES CONTAIN VALUES FOR THIS DATUM! Omitting row from model. "+date.toString());
            } else {

                commonDates.add(date);

                // sort it to insure the page mapping is consistent
                Collections.sort(commonTickersForDate);
                commonTickersForDate.forEach(ticker->{
                    List<Double> dataForTicker = dataListMap.get(ticker);
                    dataForTicker.add(indicatorForDateRange.get(date,ticker));

                });
            }
        });


        // this feels like brute force
        // make the map of things we are mapping to the ds
        dates = new ArrayList<>(commonDates);
        Collections.sort(dates);
        for (int i = 0; i < dates.size(); i++) {
            datumNames.put(i, DateUtils.DRONZE_DATE.format(dates.get(i)));
        }

        //make the row map
        List<String> sortedTickers = new ArrayList<>(indicatorForDateRange.columnKeySet());
        Collections.sort(sortedTickers);
        for (int i = 0; i < sortedTickers.size(); i++) {
            seriesNames.put(i,sortedTickers.get(i));
        }

        //label classes
        final int[] numLabelClasses = {1};
        if(trainingClassifiers != null && trainingClassifiers.size()>0){
            numLabelClasses[0] = new HashSet<>(trainingClassifiers.values()).size();
        }

        //make one list for data
        int[] labelindex = {0};
        List<Double> alldata = new ArrayList<Double>();
        dataListMap.keySet().forEach(ticker->{
            List<Double> tickerData = dataListMap.get(ticker);
            for (int i = 0; i < tickerData.size(); i++) {
                // we have to do this to make sure we have a labelmap
                // there has to be a better way
                alldata.add(tickerData.get(i));
                if(trainingClassifiers.get(ticker) !=null &&
                        trainingClassifiers != null &&
                        trainingClassifiers.keySet().size()>0){
                    for (int j = 0; j < numLabelClasses[0]; j++) {
                        labels.put(labelindex[0], trainingClassifiers.get(ticker));
                        labelindex[0]+=1;
                    }

                }

            }

        });

        INDArray tickersArray = Nd4j.create(
                this.convert(alldata),
                new int[]{
                        indicatorForDateRange.columnKeySet().size(),
                        1,
                        commonDates.size()});


        float[] labelFloats = new float[labels.keySet().size()];
        for (int i = 0; i < labels.keySet().size() ; i++) {
            labelFloats[i] = labels.get(i).floatValue();
        }

        INDArray labelsArray = Nd4j.create(labelFloats, new int[]{
                indicatorForDateRange.columnKeySet().size(),
                numLabelClasses[0],
                commonDates.size()});

        DataSet ds = new DataSet(tickersArray, labelsArray);

        return new DateSeriesDataSetModel(ds,datumNames,seriesNames, labels);

    }

    public Map<Integer,Integer> makeEmptyLabels(int seriesCount, int datumCount){
        // make fake labels
        Map<Integer,Integer> labels = new HashMap<>();
        int sizeall = seriesCount*datumCount;
        for (int i = 0; i <sizeall ; i++) {
            labels.put(i,1);
        }
        return labels;
    }

    public Map<Integer,String> makeLabelsForData(Map<String,Integer> trainingClassifiers,
                                                 Table<Date,String,Double> indicatorForDateRange){
        Map<Integer,String> labels = new HashMap<>();
        int sizeall = indicatorForDateRange.columnKeySet().size()*indicatorForDateRange.rowKeySet().size();
        //for

        return null;
    }

    public double[] convert(List<Double> dataList){
        //convert to double array
        double[] data = new double[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            data[i] = dataList.get(i);
        }
        return data;
    }

    public DataSetIterator makeDataSetIterator(DataSet dataSet){
        List<DataSet> trainDataSetList = new ArrayList<>();
        trainDataSetList.add(dataSet);
        DataSetIterator dataSetIterator = new ExistingDataSetIterator(trainDataSetList);
        return dataSetIterator;
    }


}
