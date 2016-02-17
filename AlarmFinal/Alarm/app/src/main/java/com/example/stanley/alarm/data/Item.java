package com.example.stanley.alarm.data;

import org.json.JSONObject;

/**
 * Created by Ayat Amin on 11/23/2015.
 */
public class Item implements JSONPopulator {
    private Condition condition;

    public Condition getCondition() {
        return condition;
    }

    @Override
    public void populate(JSONObject data) {
        condition = new Condition();
        condition.populate(data.optJSONObject("condition"));
    }
}
