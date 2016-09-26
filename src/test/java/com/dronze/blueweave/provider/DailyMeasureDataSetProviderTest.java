package com.dronze.blueweave.provider;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created by claytongraham on 9/17/16.
 */

public class DailyMeasureDataSetProviderTest {

    private static Logger log = LoggerFactory.getLogger(DailyMeasureDataSetProviderTest.class);

    DailyMeasureDataSetProvider provider;


//    @Before
//    public void setup() throws ParseException, IOException {
//        provider = new DailyMeasureDataSetProvider();
//        mongoDailyMeasureDao = mock(MongoDailyMeasureDao.class);
//
//
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        IOUtils.copy(
//                DailyMeasureDataSetProviderTest.class.getResourceAsStream("/data/json/dailymeasures_gtable.json"),
//                byteArrayOutputStream);
//
//        Table<Date,String,Double> allIndicatorsForRange =
//                GsonFactory.fromJson(byteArrayOutputStream.toString(),
//                        HashBasedTable.class, GsonFactory.Type.DEFAULT);
//
//        when(mongoDailyMeasureDao.getTickersMeasuresForIndicatorWithinDateRange(
//                anyObject(), any(), any(),anyString(), anyString())).thenReturn(allIndicatorsForRange);
//
//        //provider.mongoDailyMeasureDao = mongoDailyMeasureDao;
//
//
//    }
//
//    @Test
//    public void testGetSeriesValuesForTickersList(){
//        try {
//            List<String> tickers = new ArrayList<>();
//            tickers.add("AAPL");
//            tickers.add("TTC");
//
//            //DataSet ds = provider.getSeriesValuesForTickersList(tickers, new Date(),  new Date(), "CLOSE");
//            DataSet ds = null;
//
////                Table<Date,String,Double> indicatorForDateRange =
////                        mongoDailyMeasureDao.getTickersMeasuresForIndicatorWithinDateRange(
////                                tickers, new Date(), new Date(), "ARD", "CLOSE");
////
////                Double val = indicatorForDateRange.get(DateUtils.DRONZE_DATE.parse("2015-08-25"),"AAPL");
////                Assert.assertEquals(Double.parseDouble(val.toString()), ds.getFeatures().getDouble(6), 1e-4);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//    }




}
