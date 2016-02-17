package com.example.stanley.alarm;

/**
 * Created by XuanXuan on 11/30/2015.
 */

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class PopupAlarmMathProblemActivity extends AppCompatActivity {
    EditText e1;
    TextView num1;
    TextView num2;
    TextView eq;
    TextView op;
    TextView indicate;
    int n1, n2, oper, ans;
    boolean correct = false;
    private Ringtone alarm_ringtone;
    private boolean b_snoozing = false;
    private boolean b_last_snooze = false;
    private boolean b_exit = false;
    private Button snooze_btn, confirm_btn;
    private boolean b_weather, b_calendar;
    private int alarm_end_hour, alarm_end_minute;
    private String play_ringtone;
    private int current_alarm_index;
    private int current_weekday;
    private PowerManager.WakeLock wakeLock;

    private Handler handler = new Handler();

    private Runnable myTask = new Runnable() {
        public void run() {
            alarm_ringtone.play();
            if(!b_last_snooze) snooze_btn.setEnabled(true);
            b_snoozing = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        b_weather = intent.getBooleanExtra("weather", false);
        b_calendar = intent.getBooleanExtra("calendar", false);
        current_alarm_index = intent.getIntExtra("alarm_index", 0);
        current_weekday = intent.getIntExtra("current_weekday", 0);
        alarm_end_hour = intent.getIntExtra("alarm_end_hour", 0);
        alarm_end_minute = intent.getIntExtra("alarm_end_minute", 0);
        play_ringtone = intent.getStringExtra("ringtone");
        b_last_snooze = intent.getBooleanExtra("b_last_snooze", false);
        correct = intent.getBooleanExtra("correct", false);
        ans = intent.getIntExtra("answer", 0);

        n1 = intent.getIntExtra("number1", 0);
        n2 = intent.getIntExtra("number2", 0);
        oper = intent.getIntExtra("operator", 0);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        KeyguardManager km = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
        kl.disableKeyguard();

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
        wakeLock.acquire();

        setContentView(R.layout.popup_alarm_math_problem);

        Uri uri = Uri.parse(play_ringtone);

        confirm_btn = (Button) findViewById(R.id.confirmation);
        snooze_btn = (Button) findViewById(R.id.sleep);
        confirm_btn.setOnClickListener(new ClickListener());
        snooze_btn.setOnClickListener(new ClickListener());

        if (check_end_time() == -1) snooze_btn.setEnabled(false);

        e1 = (EditText) findViewById(R.id.answer);
        num1 = (TextView) findViewById(R.id.first);
        num2 = (TextView) findViewById(R.id.second);
        indicate = (TextView) findViewById(R.id.indication);
        op = (TextView) findViewById(R.id.sign);
        eq = (TextView) findViewById(R.id.equal);

        int length = e1.length() / 2;
        e1.setSelection(length);

        e1.setTextColor(0xFF000000);

        // adjusting the number
        if (oper == 0 || oper == 1){
            while (n1 % 10 == 0 || n1 < 30) {
                Random rnum = new Random();
                n1 = rnum.nextInt(100);
            }
            while (n2 % 10 == 0 || n2 < 30) {
                Random rnum = new Random();
                n1 = rnum.nextInt(100);
            }
        }
        else if(oper == 2){ // if it is multiplication
            n2  = n2 % 10;
            if (n2 < 5) n2 = 7; // adjusting the number
            // make sure n1 is not too small
            while (n1 % 10 == 0 || n1 < 30) {
                Random rnum = new Random();
                n1 = rnum.nextInt(100);
            }
        }

        num1.setText(n1 + "");
        num1.setTextColor(0xFF000000);
        num2.setText(n2 + "");
        num2.setTextColor(0xFF000000);
        eq.setTextColor(0xFF000000);

        setOperation(op, oper);

        //Get Saved Ringtone
        alarm_ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        //Overwrite mute
        alarm_ringtone.setStreamType(AudioManager.STREAM_ALARM);
        //Play Alarm

        if(correct)
        {
            alarm_ringtone.stop();
            snooze_btn.setEnabled(false);
            indicate.setText("Correct!");
            indicate.setTextColor(0xFF73FF3F);
            e1.setText("" + ans);
            e1.setTextColor(0xFF73FF3F);
            e1.setEnabled(false);
            confirm_btn.setText("Done");
        }
        else
            alarm_ringtone.play();
        if(b_last_snooze) snooze_btn.setEnabled(false);
    }

    public void setOperation(TextView a, int b) {
        switch (b) {
            case 0:
                a.setText("+");
                a.setTextColor(0xFF000000);
                break;
            case 1:
                a.setText("-");
                a.setTextColor(0xFF000000);
                break;
            case 2:
                a.setText("*");
                a.setTextColor(0xFF000000);
                break;
        }
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.confirmation:
                    if(!correct) checkAnswer(e1, n1, n2, oper);
                    else
                    {
                        alarm_ringtone = null;
                        b_exit = true;
                        updateAlarmsData();
                        if(b_weather) {
                            Intent in = new Intent(getApplicationContext(), WeatherActivity.class);
                            startActivity(in);
                        }
                        wakeLock.release();
                        PopupAlarmMathProblemActivity.this.finish();
                    }
                    break;
                case R.id.sleep:
                    alarm_ringtone.stop();
                    int check_time = check_end_time();
                    if (check_time < 300000) {
                        Toast.makeText(getApplicationContext(), "Last time to snooze", Toast.LENGTH_SHORT).show();
                        b_last_snooze = true;
                    }
                    handler.postDelayed(myTask, check_time);
                    snooze_btn.setEnabled(false);
                    b_snoozing = true;
                    break;
            }
        }
    }

    public void checkAnswer(EditText e, int a, int b, int c) {
        int answer = 0;
        int user = 0;
        switch (c) {
            case 0:
                answer = a + b;
                break;
            case 1:
                answer = a - b;
                break;
            case 2:
                answer = a * b;
                break;
        }
        if (e1.getText().toString().equals("")) {
            correct = false;
            indicate.setText("Please input an answer!");
            indicate.setTextColor(0xFF000000);
        }
        else {
            user = Integer.parseInt(e.getText().toString());

            if(user == answer) {
                ans = answer;
                correct = true;
                indicate.setText("Correct!");
                indicate.setTextColor(0xFF73FF3F);
                e1.setTextColor(0xFF73FF3F);
                e1.setEnabled(false);
                confirm_btn.setText("Done");
                alarm_ringtone.stop();
                if(b_snoozing) handler.removeCallbacks(myTask);
                else snooze_btn.setEnabled(false);
            }
            else {
                indicate.setText("Try Again!");
                indicate.setTextColor(0xFFFF2218);
                e1.setTextColor(0xFFFF2218);
                e1.setEnabled(true);
                correct = false;
            };
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
            Intent in = new Intent(this, PopupAlarmMathProblemActivity.class);
            in.putExtra("weather", b_weather);
            in.putExtra("calendar", b_calendar);
            in.putExtra("current_alarm_index", current_alarm_index);
            in.putExtra("alarm_end_hour", alarm_end_hour);
            in.putExtra("alarm_end_minute", alarm_end_minute);
            in.putExtra("ringtone", play_ringtone);
            in.putExtra("correct", correct);
            in.putExtra("b_last_snooze", b_last_snooze);
            in.putExtra("number1", n1);
            in.putExtra("number2", n2);
            in.putExtra("answer", ans);
            in.putExtra("operator", oper);
            startActivity(in);
            this.finish();
        }
    }
}
