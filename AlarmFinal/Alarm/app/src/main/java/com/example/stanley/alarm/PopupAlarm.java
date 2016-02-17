package com.example.stanley.alarm;

import android.content.Intent;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.app.Service;
import android.view.View;

import java.util.Random;

public class PopupAlarm extends Service//ppCompatActivity
{
    private View mView;
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId)
    {
        super.onStartCommand(intent, flag, startId);

        int game_type = intent.getIntExtra("game_type", 0);

        if(game_type == 0){
            Intent in = new Intent(this, PopupAlarmPuzzleActivity.class);
            in.putExtra("weather", intent.getBooleanExtra("weather", false));
            in.putExtra("calendar", intent.getBooleanExtra("calendar", false));
            in.putExtra("alarm_index", intent.getIntExtra("alarm_index", 0));
            in.putExtra("current_weekday", intent.getIntExtra("current_weekday", 0));
            in.putExtra("alarm_end_hour", intent.getIntExtra("alarm_end_hour", 0));
            in.putExtra("alarm_end_minute", intent.getIntExtra("alarm_end_minute", 0));
            String ringtone = intent.getStringExtra("ringtone");
            in.putExtra("ringtone", ringtone);
            Random randomGenerator = new Random();
            boolean[] button_color = new boolean[9];
            int randInt;
            for (int i = 0; i < 9; i++) {
                randInt = randomGenerator.nextInt(2);
                if (randInt == 1) button_color[i] = true;
            }
            in.putExtra("button_color0", button_color[0]);
            in.putExtra("button_color1", button_color[1]);
            in.putExtra("button_color2", button_color[2]);
            in.putExtra("button_color3", button_color[3]);
            in.putExtra("button_color4", button_color[4]);
            in.putExtra("button_color5", button_color[5]);
            in.putExtra("button_color6", button_color[6]);
            in.putExtra("button_color7", button_color[7]);
            in.putExtra("button_color8", button_color[8]);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        }
        else if(game_type == 1) {
            Intent in = new Intent(this, PopupAlarmMathProblemActivity.class);
            Random rnum = new Random();

            int n1 = rnum.nextInt(101);
            int n2 = rnum.nextInt(101);
            int oper = rnum.nextInt(3);
            in.putExtra("weather", intent.getBooleanExtra("weather", false));
            in.putExtra("calendar", intent.getBooleanExtra("calendar", false));
            in.putExtra("alarm_index", intent.getIntExtra("alarm_index", 0));
            in.putExtra("current_weekday", intent.getIntExtra("current_weekday", 0));
            in.putExtra("alarm_end_hour", intent.getIntExtra("alarm_end_hour", 0));
            in.putExtra("alarm_end_minute", intent.getIntExtra("alarm_end_minute", 0));
            String ringtone = intent.getStringExtra("ringtone");
            in.putExtra("ringtone", ringtone);
            in.putExtra("number1", n1);
            in.putExtra("number2", n2);
            in.putExtra("operator", oper);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
        }
        return flag;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).removeView(mView);
    }
}
