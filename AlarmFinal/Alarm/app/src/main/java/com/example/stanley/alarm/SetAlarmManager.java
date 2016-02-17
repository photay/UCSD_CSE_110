package com.example.stanley.alarm;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import java.util.Calendar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;

/**
 * Created by yshui on 11/16/15.
 */
public class SetAlarmManager
{
    private Context context;
    private Calendar c;
    private AlarmManager alarmManager = null;
    private Bundle b;
    private Intent intent;
    private int count = 0;
    public SetAlarmManager(Context context, Calendar cal, Intent intent, int count)
    {
        this.c = cal;
        this.context = context;
        this.intent = intent;
        this.count = count;
    }

    public void setAlarmManager()
    {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, count, intent, 0);

        if(c.getTimeInMillis() <= System.currentTimeMillis())
        {
            c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
        }

    }
    public void resetAlarmManager()
    {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, count, intent, 0);
        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }
}