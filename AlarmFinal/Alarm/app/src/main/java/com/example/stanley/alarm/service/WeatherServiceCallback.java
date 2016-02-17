package com.example.stanley.alarm.service;

import com.example.stanley.alarm.data.Channel;

/**
 * Created by Ayat Amin on 11/23/2015.
 */
public interface WeatherServiceCallback {

    void serviceSuccess(Channel channel);
    void serviceFailure(Exception exception);
}
