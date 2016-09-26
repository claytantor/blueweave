package com.dronze.blueweave.json.serialize;

import com.dronze.tradestation.util.DateUtils;
import com.google.common.collect.Table;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

/**
 * Created by claytongraham on 11/26/15.
 */
public class GuavaTableSerializer implements JsonSerializer<Table> {
    private static final SimpleDateFormat DRONZE_DATE = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public JsonElement serialize(Table src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        //columns
        JsonArray jsonColsArray = new JsonArray();
        jsonColsArray.add("DATE");
        for(Object col:src.columnKeySet()) {
            jsonColsArray.add(col.toString());
        }
        object.add("columns",jsonColsArray);

        //data
        JsonArray jsonRowsArray = new JsonArray();
        Set<Date> rowKeys = src.rowKeySet();

        for(Object rkey: rowKeys){

            JsonArray rowCols = new JsonArray();

            if(rkey instanceof Date)
                rowCols.add(DateUtils.DRONZE_DATE.format(rkey));
            else if(rkey instanceof String)
                rowCols.add(rkey.toString());

            //offset 1 for the date column
            for (int j = 1; j < jsonColsArray.size(); j++) {
                Object o = src.get(rkey, jsonColsArray.get(j).toString().replace("\"",""));
                if(o!=null){
                    if (o.getClass() == Integer.class) {
                        rowCols.add((Integer) o);
                    }
                    else if (o.getClass() == String.class) {
                        rowCols.add(o.toString());
                    }
                    else if (o.getClass() == Float.class) {
                        rowCols.add((Float)o);
                    }
                    else if (o.getClass() == Double.class) {
                        rowCols.add((Double)o);
                    }
                    else if (o.getClass() == BigDecimal.class) {
                        rowCols.add((BigDecimal)o);
                    }
                    else if (o.getClass() == Date.class) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        rowCols.add(sdf.format(o));
                    }
                } else {
                    rowCols.add((String)null);
                }

            }
            jsonRowsArray.add(rowCols);

        }

        object.add("data",jsonRowsArray);

        return object;

    }


}

