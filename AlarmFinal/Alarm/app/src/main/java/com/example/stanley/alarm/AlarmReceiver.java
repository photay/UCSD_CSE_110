package com.example.stanley.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by yshui on 11/1/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private boolean alarm_bool = true;
    private Bundle receiverBundle = new Bundle();
    private Alarm alarm = new Alarm();
    private int curr_hour, curr_min, curr_week, sch_hour, sch_min, total_curr, total_sch, count;
    private AlarmManager alarmManager;
    private Calendar c;
    private Context context;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        this.context = context;
        receiverBundle = intent.getExtras();
        count = intent.getIntExtra("count", 0);
        alarm = (Alarm) receiverBundle.getSerializable("alarm");
        sch_hour = alarm.start_hour;
        sch_min = alarm.start_minute;
        total_sch = sch_hour*60 + sch_min;
        Calendar curr_time = Calendar.getInstance();
        curr_hour = curr_time.get(Calendar.HOUR_OF_DAY);
        curr_min = curr_time.get(Calendar.MINUTE);
        curr_week = curr_time.get(Calendar.DAY_OF_WEEK)-1;
        total_curr = curr_hour*60 + curr_min;

        if (total_sch >= total_curr-1 && alarm.weekdays[curr_week])
        {
            Intent service = new Intent(context, PopupAlarm.class);
            service.putExtra("alarm_index", count);
            service.putExtra("current_weekday", curr_week);
            service.putExtra("calendar", alarm.calendar);
            service.putExtra("weather", alarm.weather);
            service.putExtra("game_type", alarm.game_type);
            service.putExtra("alarm_end_hour", alarm.end_hour);
            service.putExtra("alarm_end_minute", alarm.end_minute);
            service.putExtra("ringtone", alarm.ringtone);
            context.startService(service);
        }
    }
}
