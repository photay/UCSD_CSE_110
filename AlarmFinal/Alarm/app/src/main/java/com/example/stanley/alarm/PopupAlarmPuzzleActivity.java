package com.example.stanley.alarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by yshui on 11/16/15.
 */
public class PopupAlarmPuzzleActivity extends AppCompatActivity
{
    private Button confirm_btn, snooze_btn;
    private Button [] b = new Button[9];
    private Boolean b_weather, b_calendar;
    private boolean [] color_set = new boolean[9];
    private TextView message;
    private String play_ringtone;
    private int current_alarm_index;
    private int current_weekday;
    private Ringtone alarm_ringtone;
    private boolean b_puzzle_solved = false;
    private boolean b_snoozing = false;
    private boolean b_last_snooze = false;
    private boolean b_exit = false;
    private int alarm_end_hour, alarm_end_minute;
    private PowerManager.WakeLock wakeLock;

    private Handler handler = new Handler();

    private Runnable myTask = new Runnable() {
        public void run() {
            alarm_ringtone.play();
            if(!b_last_snooze) snooze_btn.setEnabled(true);
            b_snoozing = false;
        }
    };

    public void onCreate(Bundle SavedInstanceState)
    {
        super.onCreate(SavedInstanceState);

        Intent intent = getIntent();
        b_weather = intent.getBooleanExtra("weather", false);
        b_calendar = intent.getBooleanExtra("calendar", false);
        current_alarm_index = intent.getIntExtra("alarm_index", 0);
        current_weekday = intent.getIntExtra("current_weekday", 0);
        alarm_end_hour = intent.getIntExtra("alarm_end_hour", 0);
        alarm_end_minute = intent.getIntExtra("alarm_end_minute", 0);
        play_ringtone = intent.getStringExtra("ringtone");
        b_puzzle_solved = intent.getBooleanExtra("b_puzzle_solved", false);
        b_last_snooze = intent.getBooleanExtra("b_last_snooze", false);

        color_set[0] = intent.getBooleanExtra("button_color0", false);
        color_set[1] = intent.getBooleanExtra("button_color1", false);
        color_set[2] = intent.getBooleanExtra("button_color2", false);
        color_set[3] = intent.getBooleanExtra("button_color3", false);
        color_set[4] = intent.getBooleanExtra("button_color4", false);
        color_set[5] = intent.getBooleanExtra("button_color5", false);
        color_set[6] = intent.getBooleanExtra("button_color6", false);
        color_set[7] = intent.getBooleanExtra("button_color7", false);
        color_set[8] = intent.getBooleanExtra("button_color8", false);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        KeyguardManager km = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km .newKeyguardLock("MyKeyguardLock"); kl.disableKeyguard();

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();

        setContentView(R.layout.popup_alarm_puzzle);

        Uri uri = Uri.parse(play_ringtone);

        message = (TextView) findViewById(R.id.indication);
        confirm_btn = (Button) findViewById(R.id.confirm_btn);
        snooze_btn = (Button) findViewById(R.id.snooze_btn);

        confirm_btn.setOnClickListener(new ClickListener());
        snooze_btn.setOnClickListener(new ClickListener());

        b[0] = (Button) findViewById(R.id.button1);
        b[1] = (Button) findViewById(R.id.button2);
        b[2] = (Button) findViewById(R.id.button3);
        b[3] = (Button) findViewById(R.id.button4);
        b[4] = (Button) findViewById(R.id.button5);
        b[5] = (Button) findViewById(R.id.button6);
        b[6] = (Button) findViewById(R.id.button7);
        b[7] = (Button) findViewById(R.id.button8);
        b[8] = (Button) findViewById(R.id.button9);

        confirm_btn.setEnabled(false);
        if(check_end_time()==-1) snooze_btn.setEnabled(false);

        for(int i = 0; i <= 8; i++)
        {
            b[i].setOnClickListener(new ClickListener());
            if(color_set[i]) b[i].setBackgroundResource(R.drawable.button_1);
            else b[i].setBackgroundResource(R.drawable.button_2);
        }

        //Get Saved Ringtone
        alarm_ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        //Overwrite mute
        alarm_ringtone.setStreamType(AudioManager.STREAM_ALARM);
        //Play Alarm
        if(b_puzzle_solved)
        {
            message.setText("You Win!");
            message.setTextColor(0xFF25FF29);
            alarm_ringtone.stop();
            confirm_btn.setEnabled(true);
            snooze_btn.setEnabled(false);
        }
        else
            alarm_ringtone.play();
        if(b_last_snooze) snooze_btn.setEnabled(false);
    }

    private class ClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.confirm_btn:
                    if(b_puzzle_solved) b_exit = true;
                    alarm_ringtone.stop();
                    alarm_ringtone = null;
                    if(b_snoozing) handler.removeCallbacks(myTask);
                    else snooze_btn.setEnabled(false);
                    updateAlarmsData();
                    if(b_weather) {
                        Intent in = new Intent(getApplicationContext(), WeatherActivity.class);
                        startActivity(in);
                    }
                    wakeLock.release();
                    PopupAlarmPuzzleActivity.this.finish();
                    break;
                case R.id.snooze_btn:
                    alarm_ringtone.stop();
                    int check_time = check_end_time();
                    if(check_time < 300000) {
                        Toast.makeText(getApplicationContext(), "Last time to snooze", Toast.LENGTH_SHORT).show();
                        b_last_snooze = true;
                    }
                    handler.postDelayed(myTask, check_time);
                    snooze_btn.setEnabled(false);
                    b_snoozing = true;
                    break;
                case R.id.button1:
                    if (!b_puzzle_solved) {
                        color_set[0] = invertBoolean(color_set[0], 0);
                        color_set[1] = invertBoolean(color_set[1], 1);
                        color_set[3] = invertBoolean(color_set[3], 3);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button2:
                    if (!b_puzzle_solved) {
                        color_set[0] = invertBoolean(color_set[0], 0);
                        color_set[1] = invertBoolean(color_set[1], 1);
                        color_set[2] = invertBoolean(color_set[2], 2);
                        color_set[4] = invertBoolean(color_set[4], 4);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button3:
                    if (!b_puzzle_solved) {
                        color_set[5] = invertBoolean(color_set[5], 5);
                        color_set[1] = invertBoolean(color_set[1], 1);
                        color_set[2] = invertBoolean(color_set[2], 2);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button4:
                    if (!b_puzzle_solved) {
                        color_set[0] = invertBoolean(color_set[0], 0);
                        color_set[3] = invertBoolean(color_set[3], 3);
                        color_set[4] = invertBoolean(color_set[4], 4);
                        color_set[6] = invertBoolean(color_set[6], 6);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button5:
                    if (!b_puzzle_solved) {
                        color_set[1] = invertBoolean(color_set[1], 1);
                        color_set[3] = invertBoolean(color_set[3], 3);
                        color_set[4] = invertBoolean(color_set[4], 4);
                        color_set[5] = invertBoolean(color_set[5], 5);
                        color_set[7] = invertBoolean(color_set[7], 7);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button6:
                    if (!b_puzzle_solved) {
                        color_set[2] = invertBoolean(color_set[2], 2);
                        color_set[4] = invertBoolean(color_set[4], 4);
                        color_set[5] = invertBoolean(color_set[5], 5);
                        color_set[8] = invertBoolean(color_set[8], 8);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button7:
                    if (!b_puzzle_solved) {
                        color_set[3] = invertBoolean(color_set[3], 3);
                        color_set[6] = invertBoolean(color_set[6], 6);
                        color_set[7] = invertBoolean(color_set[7], 7);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button8:
                    if (!b_puzzle_solved) {
                        color_set[4] = invertBoolean(color_set[4], 4);
                        color_set[6] = invertBoolean(color_set[6], 6);
                        color_set[7] = invertBoolean(color_set[7], 7);
                        color_set[8] = invertBoolean(color_set[8], 8);
                    }
                    enableConfirm(color_set);
                    break;
                case R.id.button9:
                    if (!b_puzzle_solved) {
                        color_set[5] = invertBoolean(color_set[5], 5);
                        color_set[7] = invertBoolean(color_set[7], 7);
                        color_set[8] = invertBoolean(color_set[8], 8);
                    }
                    enableConfirm(color_set);
                    break;
            }
        }
    }

    public void updateAlarmsData()
    {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

        Gson gson = new Gson();
        String json = appSharedPrefs.getString("MyObject", "");
        ArrayList<Alarm> alarms = new ArrayList<Alarm>(10);
        alarms = gson.fromJson(json, new TypeToken<ArrayList<Alarm>>() {
        }.getType());

        if (!alarms.get(current_alarm_index).repeat_weekdays) {
            alarms.get(current_alarm_index).weekdays[current_weekday] = false;
        }

        // saving the changed data to the phone
        if (alarms.size() == 0) {
            prefsEditor.putBoolean("has_data", false);
            prefsEditor.commit();
        } else {
            prefsEditor.putBoolean("has_data", true);
            Gson newGson = new Gson();
            String newJson = gson.toJson(alarms);
            prefsEditor.putString("MyObject", newJson);
            prefsEditor.commit();
        }
    }

    public void enableConfirm(boolean[] bool)
    {
        int count = 9;
        boolean is_puzzle_solved = true;
        for(int i = 0; i <= 8; i++) {
            if(!bool[i]) {
                is_puzzle_solved = false;
                count--;
            }
        }

        if(is_puzzle_solved)
        {
            b_puzzle_solved = true;
            message.setText("You Win!");
            message.setTextColor(0xFF25FF29);
            alarm_ringtone.stop();
            confirm_btn.setEnabled(true);
            snooze_btn.setEnabled(false);
            if(b_snoozing) handler.removeCallbacks(myTask);
        }
    }

    public boolean invertBoolean(boolean bool, int i)
    {
        if(bool)
        {
            b[i].setBackgroundResource(R.drawable.button_2);
            return false;
        }
        else
        {
            b[i].setBackgroundResource(R.drawable.button_1);
            return true;
        }
    }

    public int check_end_time(){
        Calendar CurrentDateTime = Calendar.getInstance();
        int current_phone_hour = CurrentDateTime.get(Calendar.HOUR);
        if(CurrentDateTime.get(Calendar.AM_PM) !=0) current_phone_hour+=12;
        int current_phone_minute =CurrentDateTime.get(Calendar.MINUTE);
        int current_phone_time = current_phone_hour * 60 + current_phone_minute;
        int alarm_end_time = alarm_end_hour * 60 + alarm_end_minute;
        int diff_time = alarm_end_time - current_phone_time;
        if(diff_time >= 5) return 300000; // 5 minute
        else if(diff_time > 0 && diff_time < 5) return diff_time * 60000;
        else return -1;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        else{
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        else{
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        } else{
            return super.onKeyLongPress(keyCode, event);
        }
    }

    @Override
    protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        if(!b_exit) {
            if(b_snoozing) handler.removeCallbacks(myTask);
            wakeLock.release();
            alarm_ringtone.stop();
            alarm_ringtone = null;
            Intent in = new Intent(this, PopupAlarmPuzzleActivity.class);
            in.putExtra("weather", b_weather);
            in.putExtra("calendar", b_calendar);
            in.putExtra("current_alarm_index", current_alarm_index);
            in.putExtra("alarm_end_hour", alarm_end_hour);
            in.putExtra("alarm_end_minute", alarm_end_minute);
            in.putExtra("ringtone", play_ringtone);
            in.putExtra("b_puzzle_solved", b_puzzle_solved);
            in.putExtra("b_last_snooze", b_last_snooze);
            in.putExtra("button_color0", color_set[0]);
            in.putExtra("button_color1", color_set[1]);
            in.putExtra("button_color2", color_set[2]);
            in.putExtra("button_color3", color_set[3]);
            in.putExtra("button_color4", color_set[4]);
            in.putExtra("button_color5", color_set[5]);
            in.putExtra("button_color6", color_set[6]);
            in.putExtra("button_color7", color_set[7]);
            in.putExtra("button_color8", color_set[8]);
            startActivity(in);
            this.finish();
        }
    }
}
