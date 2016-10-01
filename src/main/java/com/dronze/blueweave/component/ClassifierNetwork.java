package com.dronze.blueweave.component;

import com.google.common.collect.Table;

import java.util.Map;

/**
 * Created by claytongraham on 9/27/16.
 */
public interface ClassifierNetwork {
    public Table<Integer,String, Object> evaluate() throws ClassificationException;
}
