package com.example.stanley.alarm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class SetClockTime extends AppCompatActivity {

    private Button confirmButton,cancelButton;
    private Intent state = new Intent();
    private int old_start_hour, old_start_minute, old_end_hour, old_end_minute;
    private int new_start_hour, new_start_minute, new_end_hour, new_end_minute;
    private boolean b_modify_start_time = true;
    private int resultCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_clock_time);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels * 0.8;
        double height = dm.heightPixels * 0.6;

        getWindow().setLayout((int) width, (int) height);

        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.dimAmount = (float) 0.05;
        getWindow().setAttributes(wlp);

        confirmButton = (Button) findViewById(R.id.set_clock_confirm_button);
        cancelButton = (Button) findViewById(R.id.set_clock_cancel_button);

        confirmButton.setOnClickListener(new ButtonListener());
        cancelButton.setOnClickListener(new ButtonListener());

        Bundle bundleObject = getIntent().getExtras();
        b_modify_start_time = bundleObject.getBoolean("b_modify_start_time");
        old_start_hour = bundleObject.getInt("start_hour");
        old_start_minute = bundleObject.getInt("start_minute");
        old_end_hour = bundleObject.getInt("end_hour");
        old_end_minute = bundleObject.getInt("end_minute");

        //initialize new_hour and new_minute
        new_start_hour = old_start_hour;
        new_start_minute = old_start_minute;
        new_end_hour = old_end_hour;
        new_end_minute = old_end_minute;

        Calendar CurrentDateTime = Calendar.getInstance();
        if(b_modify_start_time) {
            new_start_hour = CurrentDateTime.get(Calendar.HOUR);
            new_start_minute = CurrentDateTime.get(Calendar.MINUTE);
            if(CurrentDateTime.get(Calendar.AM_PM) !=0) new_start_hour+=12;
        }
        else{
            new_end_hour = CurrentDateTime.get(Calendar.HOUR);
            new_end_minute = CurrentDateTime.get(Calendar.MINUTE);
            if(CurrentDateTime.get(Calendar.AM_PM) !=0) new_end_hour+=12;
        }

        TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if(b_modify_start_time) {
                    new_start_hour = hourOfDay;
                    new_start_minute = minute;
                }
                else
                {
                    new_end_hour = hourOfDay;
                    new_end_minute = minute;
                }
            }
        });
    }

    private class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent AlarmSettingActivity = new Intent();
            switch(v.getId()){
                case R.id.set_clock_confirm_button:
                    resultCode = 1;
                    Bundle bundleObject = new Bundle();
                    bundleObject.putBoolean("b_modify_start_time", b_modify_start_time);
                    if(b_modify_start_time)
                    {
                        bundleObject.putInt("start_hour", new_start_hour);
                        bundleObject.putInt("start_minute", new_start_minute);
                    }
                    else{
                        bundleObject.putInt("end_hour", new_end_hour);
                        bundleObject.putInt("end_minute", new_end_minute);
                    }
                    AlarmSettingActivity.putExtras(bundleObject);
                    SetClockTime.this.setResult(resultCode, AlarmSettingActivity);
                    SetClockTime.this.finish();
                    break;
                case R.id.set_clock_cancel_button:
                    resultCode = 2;
                    SetClockTime.this.setResult(resultCode, AlarmSettingActivity);
                    SetClockTime.this.finish();
                    break;
            }
        }
    }

    // phone button "back" button handler - It will go back to alarm setting page and cancel all changes
    @Override
    public void onBackPressed() {
        Intent AlarmSettingActivity = new Intent();
        resultCode = 2;
        SetClockTime.this.setResult(resultCode, AlarmSettingActivity);
        SetClockTime.this.finish();
    }
}
