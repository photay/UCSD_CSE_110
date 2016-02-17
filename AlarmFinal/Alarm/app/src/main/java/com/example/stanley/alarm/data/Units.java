package com.example.stanley.alarm.data;

import org.json.JSONObject;

/**
 * Created by Ayat Amin on 11/23/2015.
 */
public class Units implements JSONPopulator {
    private String temperature;

    public String getTemperature() {
        return temperature;
    }

    @Override
    public void populate(JSONObject data) {
        temperature = data.optString("temperature");

    }
}
