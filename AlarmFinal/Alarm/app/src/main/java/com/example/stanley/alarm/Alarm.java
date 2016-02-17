package com.example.stanley.alarm;


import android.media.Ringtone;

import java.io.Serializable;

/**
 * Created by Stanley on 10/25/2015.
 */
public class Alarm implements Serializable {
     public boolean activate;
     public int start_hour;
     public int start_minute;
     public int end_hour;
     public int end_minute;
     public boolean [] weekdays = new boolean[7];
     public boolean repeat_weekdays;
     public int ringtone_spinning_position;
     public String ringtone;
     public int game_type;          // 1 = puzzle, 2 = math questions
     public boolean weather;
     public boolean calendar;
}
