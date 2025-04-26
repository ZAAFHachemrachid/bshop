package com.example.b_shop.data.local.converters;

import androidx.room.TypeConverter;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class StringListConverter {
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (String item : list) {
                jsonArray.put(item);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        try {
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(value);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}