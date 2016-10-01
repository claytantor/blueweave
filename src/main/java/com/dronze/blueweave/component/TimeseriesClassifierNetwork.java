package com.dronze.blueweave.component;

import com.dronze.blueweave.model.DateSeriesDataSetModel;
import com.dronze.blueweave.model.NetworkClass;
import com.dronze.blueweave.model.NetworkClassification;
import com.dronze.blueweave.provider.DailyMeasureDataSetProvider;
import com.dronze.blueweave.provider.DataSetProviderException;
import com.dronze.blueweave.util.NDArrayUtils;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import org.apache.commons.collections.map.HashedMap;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by claytongraham on 9/25/16.
 */
public class TimeseriesClassifierNetwork extends BaseClassifierNetwork implements ClassifierNetwork {

    private static Logger log = LoggerFactory.getLogger(TimeseriesClassifierNetwork.class);

    Map<Integer,String> trainClasses;
    Map<String,Integer> trainClassifications;
    Table<Date,String,Double> trainTable;
    Table<Date,String,Double> testTable;

    DailyMeasureDataSetProvider dailyMeasureDataSetProvider;

    private TimeseriesClassifierNetwork(){
        dailyMeasureDataSetProvider = new DailyMeasureDataSetProvider();
    }

    private TimeseriesClassifierNetwork(Map<Integer, String> trainClasses, Map<String, Integer> trainClassifications,
                                       Table<Date, String, Double> trainTable, Table<Date, String, Double> testTable,
                                        Config configuration,
                                        MultiLayerConfiguration multiLayerConfig) {
        this();
        this.trainClasses = trainClasses;
        this.trainClassifications = trainClassifications;
        this.trainTable = trainTable;
        this.testTable = testTable;
        super.config = configuration;
        super.multiLayerConfiguration = multiLayerConfig;

    }

    public Table<Integer, String, Object> evaluate()
            throws ClassificationException
    {

        try {

            DateSeriesDataSetModel trainDataSet = dailyMeasureDataSetProvider.getSeriesValuesForTable(
                    trainTable,
                    trainClassifications);

            int numLabelClasses = trainClasses.size();

            //Normalize the training data
            DataNormalization normalizer = new NormalizerStandardize();
            DataSetIterator trainData = dailyMeasureDataSetProvider.makeDataSetIterator(trainDataSet.getDataSet());
            normalizer.fit(trainData);              //Collect training data statistics
            trainData.reset();

            //Use previously collected statistics to normalize on-the-fly. Each DataSet returned by 'trainData' iterator will be normalized
            trainData.setPreProcessor(normalizer);

            //testing labels can be empty
            DateSeriesDataSetModel testDataSet = dailyMeasureDataSetProvider.getSeriesValuesForTable(
                    testTable,
                    trainClassifications);

            DataSetIterator testData = dailyMeasureDataSetProvider.makeDataSetIterator(testDataSet.getDataSet());

            //build a model we can use to correlate classifications
            Map<Integer,Map<String,Object>> sequences = makeFeatureModelForTesting(testDataSet);

            //reset it because we dont know if using next altered it prior to normalization
            testData.reset();

            // normailze
            testData.setPreProcessor(normalizer);   //Note that we are using the exact same normalization process as the training data

            //            // ----- Configure the network -----
//            MultiLayerConfiguration nconf = new NeuralNetConfiguration.Builder()
//                    .seed(123)    //Random number generator seed for improved repeatability. Optional.
//                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
//                    .weightInit(WeightInit.XAVIER)
//                    .updater(Updater.NESTEROVS).momentum(0.85)
//                    .learningRate(0.02)
//                    .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
//                    .gradientNormalizationThreshold(0.45)
//                    .list()
//                    .layer(0, new GravesLSTM.Builder().activation("tanh").nIn(1).nOut(10).build())
//                    .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
//                            .activation("softmax").nIn(10).nOut(numLabelClasses).build())
//                    .pretrain(false).backprop(true).build();

            MultiLayerNetwork net = new MultiLayerNetwork(multiLayerConfiguration);
            net.init();

            net.setListeners(new ScoreIterationListener(10));   //Print the score (loss function value) every 20 iterations

            // ----- Train the network, evaluating the test set performance at each epoch -----
            int nEpochs = 100;
            String str = "Test set evaluation at epoch %d: Accuracy = %.2f, F1 = %.2f";
            for (int i = 0; i < nEpochs; i++) {
                net.fit(trainData);

                //Evaluate on the test set:
                Evaluation evaluation = net.evaluate(testData);
                log.info(String.format(str, i, evaluation.accuracy(), evaluation.f1()));

                testData.reset();
                trainData.reset();
            }

            INDArray classificationsByRow = Nd4j.argMax(Nd4j.argMax(
                    net.output(testData.next().getFeatureMatrix()), 2),1);


            //map sequences to the resultant table.
            Table<Integer, String, Object> resultTable = TreeBasedTable.create();
            int[] ival = {0};
            for (int i = 0; i < classificationsByRow.rows() ; i++) {
                ival[0] = i;
                Map<String,Object> sequenceToClassify = sequences.get(i);
                sequenceToClassify.keySet().forEach(name->{
                    resultTable.put(ival[0], name, sequenceToClassify.get(name));
                });
                resultTable.put(ival[0], "classificationId", classificationsByRow.getInt(i));
                resultTable.put(ival[0], "classificationName",
                        trainClasses.get(classificationsByRow.getInt(i)));
            }

            return resultTable;


        } catch (DataSetProviderException e) {
            log.error(e.getMessage(),e);
            throw new ClassificationException(e);
        }

    }


    private  Map<Integer,Map<String,Object>> makeFeatureModelForTesting(DateSeriesDataSetModel ds) {

        Map<Integer,Map<String,Object>> items = new HashMap<>();

        //make a copy
        INDArray features = ds.getDataSet().copy().getFeatureMatrix();

        try {
            List<List<Double>> rows = NDArrayUtils.makeRowsFromNDArray(features,6);
            for (int i = 0; i < rows.size(); i++) {
                List<Double> row = rows.get(i);
                Map<String,Object> itemModel = new HashedMap();
                itemModel.put("seriesNumber",i);
                itemModel.put("seriesData",row);
                itemModel.put("seriesName", ds.getSeriesNames().get(i));
                items.put(i,itemModel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static class TimeseriesClassifierNetworkBuilder {


        private List<NetworkClass> networkClasses;
        private List<NetworkClassification> networkClassifications;
        private Table<Date,String,Double> trainTable;
        private Table<Date,String,Double> testTable;
        private Config configuration;
        private MultiLayerConfiguration multiLayerConfiguration;


        public TimeseriesClassifierNetworkBuilder setNetworkClasses(List<NetworkClass> networkClasses) {
            this.networkClasses = networkClasses;
            return this;
        }

        public TimeseriesClassifierNetworkBuilder setTrainClassifications(List<NetworkClassification> trainClassifications) {
            this.networkClassifications = trainClassifications;
            return this;
        }

        public TimeseriesClassifierNetworkBuilder setTrainTable(Table<Date, String, Double> trainTable) {
            this.trainTable = trainTable;
            return this;
        }

        public TimeseriesClassifierNetworkBuilder setTestTable(Table<Date, String, Double> testTable) {
            this.testTable = testTable;
            return this;
        }

        public TimeseriesClassifierNetworkBuilder setConfig(Config tsconfig, String networkConfigPrefix) {
            this.configuration = tsconfig;
            NeuralNetConfiguration.Builder networkConfigurationBuilder = new NeuralNetConfiguration.Builder()
                    .seed(configuration.getInt(networkConfigPrefix+".seed"))
                    .optimizationAlgo(OptimizationAlgorithm.valueOf(configuration.getString(networkConfigPrefix+".optimizationAlgo.type")))
                        .iterations(configuration.getInt(networkConfigPrefix+".optimizationAlgo.iterations"))
                    .weightInit(WeightInit.valueOf(
                            configuration.getString(networkConfigPrefix+".weightInit")))
                    .updater(Updater.valueOf(configuration.getString(networkConfigPrefix+".updater.type")))
                        .momentum(configuration.getDouble(networkConfigPrefix+".updater.momentum"))
                    .learningRate(configuration.getDouble(networkConfigPrefix+".learningRate"))
                    .gradientNormalization(GradientNormalization.valueOf(
                            configuration.getString(networkConfigPrefix+".gradientNormalization.type")))
                    .gradientNormalizationThreshold(
                            configuration.getDouble(networkConfigPrefix+".gradientNormalization.threshold"));

            ConfigList layers = configuration.getList(networkConfigPrefix+".layers");

            if(layers != null && layers.size()>0){
                NeuralNetConfiguration.ListBuilder networkConfigurationListBuilder =
                        networkConfigurationBuilder.list();

                for (int i = 0; i < layers.size(); i++) {
                    ConfigValue value = layers.get(i);
                    switch (value.valueType()) {
                        case OBJECT:

                            Config configObject = ((ConfigObject) value).toConfig();

                            networkConfigurationListBuilder
                                    .layer(configObject.getInt("number"),
                                            NetworkTypeFactory.makeLayer(configObject));

                            break;
                        case NULL:
                            throw new RuntimeException("LAYER LIST ITEM CANT BE NULL.");
                        default:
                            throw new RuntimeException("LAYER TYPE MUST BE A CONFIG OBJECT");
                    }
                }

                this.multiLayerConfiguration = networkConfigurationListBuilder
                        .pretrain(false).backprop(true).build();

            } else {
                throw new RuntimeException("NO NETWORK LIST FOUND.");
            }







            //            // ----- Configure the network -----
//            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//                    .seed(123)    //Random number generator seed for improved repeatability. Optional.
//                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
//                    .weightInit(WeightInit.XAVIER)
//                    .updater(Updater.NESTEROVS).momentum(0.85)
//                    .learningRate(0.02)
//                    .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
//                    .gradientNormalizationThreshold(0.45)
//                    .list()
//                    .layer(0, new GravesLSTM.Builder().activation("tanh").nIn(1).nOut(10).build())
//                    .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
//                            .activation("softmax").nIn(10).nOut(numLabelClasses).build())
//                    .pretrain(false).backprop(true).build();



            return this;
        }

        private Map<Integer,String> makeNetworkClassTypesMap(){
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

        public ClassifierNetwork build(){

            return new TimeseriesClassifierNetwork(
                    makeNetworkClassTypesMap(),
                    makeNetworkClassificationMap(),
                    trainTable,
                    testTable,
                    configuration,
                    multiLayerConfiguration);

        }
    }


}
