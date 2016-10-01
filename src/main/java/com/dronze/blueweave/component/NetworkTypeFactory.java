package com.dronze.blueweave.component;

import com.typesafe.config.Config;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.layers.FeedForwardLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Created by claytongraham on 10/1/16.
 */
public class NetworkTypeFactory {

    enum Type {
        GravesLSTM,
        RnnOutputLayer
    }

    public static FeedForwardLayer makeLayer(Config layerConfig){

        Type layerType = Type.valueOf(layerConfig.getString("type"));
        switch (layerType) {
            case GravesLSTM:
                return new GravesLSTM.Builder()
                        .activation(layerConfig.getString("activation"))
                        .nIn(layerConfig.getInt("nIn"))
                        .nOut(layerConfig.getInt("nOut")).build();

            case RnnOutputLayer:
                return new RnnOutputLayer.Builder()
                        .activation(layerConfig.getString("activation"))
                        .lossFunction(LossFunctions.LossFunction.valueOf(layerConfig.getString("lossFunction")))
                        .nIn(layerConfig.getInt("nIn"))
                        .nOut(layerConfig.getInt("nOut")).build();

            default:
                throw new RuntimeException("UNAVAILABLE LAYER TYPE CONFIG.");
        }



    }

}