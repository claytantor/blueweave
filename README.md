# blueweave

Guava Enabled Recurrent Sequence Classification Networks in Java. **bluewave** is intended to simplify 
the configuration and use of neural networks.

## Building The Network
**blueweave** uses a builder pattern to setup the netowrk:

```
//get the config from the classpath
Config conf = ConfigFactory.load();

//build the network
ClassifierNetwork network = new TimeseriesClassifierNetwork.TimeseriesClassifierNetworkBuilder()
        .setNetworkClasses(trainingModel.getNetworkClasses())
        .setTrainClassifications(trainingModel.getNetworkClassifications())
        .setTrainTable(trainingTable)
        .setTestTable(testingTable)
        .setConfig(conf,"TimeseriesClassifierNetwork")
        .build();

//the classification results as a table        
Table<Integer, String, Object> result = network.evaluate();        
```
## Configurations With Typesafe

**blueweave** uses typesafe to make setting up multi layer networks easier. 

```
{
  "TimeseriesClassifierNetwork": {
    "layers": [{
      "number":0,
      "type":"GravesLSTM",
      "activation": "tanh",
      "nIn": 1,
      "nOut": 10
    }, {
      "number":1,
      "type":"RnnOutputLayer",
      "lossFunction": "MCXENT",
      "activation": "softmax",
      "nIn": 10,
      "nOut": 5
    }],
    "optimizationAlgo": {
      "type": "STOCHASTIC_GRADIENT_DESCENT",
      "iterations": 1
    },
    "seed": 123,
    "learningRate": 0.02,
    "gradientNormalization": {
      "threshold": 0.45,
      "type": "ClipElementWiseAbsoluteValue"
    },
    "updater": {
      "type": "NESTEROVS",
      "momentum": 0.85
    },
    "weightInit": "XAVIER"
  }
}
```


## Data Model For Testing and Training
Currently implementations are limited to 
recurrent series, but more table types and network classifiers are intended. The data for both 
trainging and testing can be represented.

| Date        | Series1 | Series1   |
|-------------|:-------:|:---------:|
| 2016-04-01  | 0.12    | 0.22      | 
| 2016-04-02  | 0.23    | 0.12      |
| 2016-04-03  | 0.46    | 0.10      |
| 2016-04-04  | 0.51    | 0.10      |
| 2016-04-05  | 1.46    | 0.11      |

### Training Model

The training model is used to define the mapping of each series to the possible classification 
types that will be used to train the network. This is an example of training a network to classify
a set of number series.

```
{
  "startDate":"2016-04-01",
  "endDate":"2016-08-01",
  "networkClasses":[
    {"id":0, "name":"cyclic"},
    {"id":1, "name":"upward-trend"},
    {"id":2, "name":"downward-trend"},
    {"id":3, "name":"upward-shift"},
    {"id":4, "name":"downward-shift"}
  ],
  "networkClassifications":[
    {"name":"PACW", "classId":0},
    {"name":"PAG",  "classId":0},
    {"name":"PAHC", "classId":4},
    {"name":"PANW", "classId":2},
    {"name":"PATK", "classId":3},
    {"name":"PATR", "classId":3},
    {"name":"PAY",  "classId":4},
    {"name":"PAYC",  "classId":1},
    {"name":"PAYX",  "classId":3}
  ]
}
```

And to deserialize that model for use by the builder:

```
//make the training model
SequenceNetworkModel trainingModel = GsonFactory.fromJson(
        IOUtils.toString(
                TimeseriesClassifierTest.class.getResourceAsStream("/data/01/train/trainModel01.json"), "UTF-8"),
        SequenceNetworkModel.class, GsonFactory.Type.DEFAULT);
```

### Data Models

The data model for both training and testing is simple tables. An example of a serialized model 
for training could be:

```
{
  "columns": [
    "DATE",
    "PACW",
    "PAG",
    "PAHC",
    "PANW",
    "PATK",
    "PAY",
    "PAYC",
    "PAYX"
  ],
  "data": [
    [
      "2016-04-01",
      37.07,
      36.7,
      27.39,
      161.12,
      45.92,
      28.2,
      35.43,
      54.17
    ],
    [
      "2016-04-04",
      37.11,
      35.82,
      27.29,
      161.59,
      45.37,
      27.78,
      35.8,
      53.45
    ],
    [
      "2016-04-05",
      36.25,
      35.68,
      26.66,
      151.92,
      46.05,
      27.76,
      34.43,
      53.11
    ],
    [
      "2016-04-06",
      36.76,
      35.74,
      27.26,
      158.22,
      46.64,
      28.23,
      35.52,
      53.64
    ]
  ]
}
```

And deserializing it is as simple as using the provided factory:

```
Table<Date, String, Double> trainingTable = GsonFactory.fromJson(
    IOUtils.toString( TimeseriesClassifierTest.class.getResourceAsStream(
               "/data/01/train/trainTable01.json"), "UTF-8"),
    TreeBasedTable.class, GsonFactory.Type.DEFAULT);
```








