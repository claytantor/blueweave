package com.dronze.blueweave.json.deserialize;

import com.dronze.blueweave.util.DateUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by claytongraham on 9/17/16.
 */
public class GuavaTableDeserializer implements JsonDeserializer<Table> {

    @Override
    public Table deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        Table<Date, String, Double> table = HashBasedTable.create();

        try {

            JsonObject parent = (JsonObject) json;
            JsonArray colArray = parent.get("columns").getAsJsonArray();

            JsonArray rowArray = parent.get("data").getAsJsonArray();
            for (int i = 0; i < rowArray.size(); i++) {
                JsonArray row = rowArray.get(i).getAsJsonArray();
                Date rowDate = null;
                for (int j = 0; j < row.size(); j++) {
                    if (j == 0) {
                        //table.put("","",row.get(j));
                        rowDate = DateUtils.DRONZE_DATE.parse(row.get(j).toString().replace("\"", ""));

                    } else if (row.get(j) != null && !row.get(j).toString().equals("null"))  {
                        table.put(rowDate, colArray.get(j).toString().replace("\"", ""),
                                Double.parseDouble(row.get(j).toString()));
                    }

                }

            }
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }

        return table;
    }
}
