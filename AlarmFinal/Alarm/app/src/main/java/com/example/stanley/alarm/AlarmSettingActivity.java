package com.example.stanley.alarm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.media.Ringtone;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmSettingActivity extends AppCompatActivity {

    private Button confirmButton,cancelButton,startTimeButton,endTimeButton;
    private ToggleButton [] weekdayButtons = new ToggleButton[7]; //monButton, tueButton, wedButton, thuButton, friButton, satButton, sunButton;
    private String [] ToggleButtonName = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    private Switch weatherSwitch, calendarSwitch;
    private CheckBox repeatWeekdayButton;
    private Spinner spinnerRingtone, spinnerGameType;
    private ImageButton previewButton;

    private Intent alarmListIntent;
    private Context thisContext;
    private int current_alarm_index;
    private boolean b_add_clock = true;
    private Alarm temp_alarm = new Alarm();
    private int alarm_game_type = 0;

    //Yimin
    private Button showTimeOnStartButton, showTimeOnEndButton;
    private int requestCode = 0;
    private int start_hourTemp = 0, start_minuteTemp = 0, end_hourTemp = 0, end_minuteTemp = 0;

    // John
    // Ringtone spinner
    private Ringtone ringtone;
    private String alarm_ringtone_string;
    private int ringtone_spinning_position;
    private boolean b_playing_ringtone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_setting);

        thisContext = this;

        confirmButton = (Button) findViewById(R.id.alarm_setting_confirm_button);
        cancelButton = (Button) findViewById(R.id.alarm_setting_cancel_button);
        startTimeButton = (Button) findViewById(R.id.start_time_button);
        endTimeButton = (Button) findViewById(R.id.end_time_button);

        confirmButton.setOnClickListener(new ButtonListener());
        cancelButton.setOnClickListener(new ButtonListener());
        startTimeButton.setOnClickListener(new ButtonListener());
        endTimeButton.setOnClickListener(new ButtonListener());

        weekdayButtons[0] = (ToggleButton) findViewById(R.id.toggle_sun);
        weekdayButtons[1] = (ToggleButton) findViewById(R.id.toggle_mon);
        weekdayButtons[2] = (ToggleButton) findViewById(R.id.toggle_tue);
        weekdayButtons[3] = (ToggleButton) findViewById(R.id.toggle_wed);
        weekdayButtons[4] = (ToggleButton) findViewById(R.id.toggle_thu);
        weekdayButtons[5] = (ToggleButton) findViewById(R.id.toggle_fri);
        weekdayButtons[6] = (ToggleButton) findViewById(R.id.toggle_sat);

        for(int i=0; i<7; i++) {
            weekdayButtons[i].setOnCheckedChangeListener(new CheckListener());
            weekdayButtons[i].setTextOn(ToggleButtonName[i]);
            weekdayButtons[i].setTextOff(ToggleButtonName[i]);
        }

        repeatWeekdayButton = (CheckBox) findViewById(R.id.repeat_weekday_radio_button);
        repeatWeekdayButton.setOnCheckedChangeListener(new CheckListener());

        weatherSwitch = (Switch) findViewById(R.id.weather_switch);
        calendarSwitch = (Switch) findViewById(R.id.calendar_switch);
        weatherSwitch.setOnCheckedChangeListener(new CheckListener());
        calendarSwitch.setOnCheckedChangeListener(new CheckListener());

        Bundle bundleObject = getIntent().getExtras();
        b_add_clock = bundleObject.getBoolean("b_add_clock");
        current_alarm_index = bundleObject.getInt("current_alarm_index");

        if(!b_add_clock) {
            temp_alarm = (Alarm)bundleObject.getSerializable("edited_alarm");
            start_hourTemp = temp_alarm.start_hour;
            start_minuteTemp = temp_alarm.start_minute;
            end_hourTemp = temp_alarm.end_hour;
            end_minuteTemp = temp_alarm.end_minute;
            ringtone_spinning_position = temp_alarm.ringtone_spinning_position;
            alarm_ringtone_string = temp_alarm.ringtone;
            alarm_game_type = temp_alarm.game_type;
            for(int i=0; i<7; i++)
                if(temp_alarm.weekdays[i]) weekdayButtons[i].setChecked(true);
            if(temp_alarm.weather == true) weatherSwitch.setChecked(true);
            if(temp_alarm.calendar == true) calendarSwitch.setChecked(true);
            if(temp_alarm.repeat_weekdays == true) repeatWeekdayButton.setChecked(true);
        }
        else{
            Calendar CurrentDateTime = Calendar.getInstance();
            start_hourTemp = CurrentDateTime.get(Calendar.HOUR);
            start_minuteTemp = CurrentDateTime.get(Calendar.MINUTE);
            end_hourTemp = CurrentDateTime.get(Calendar.HOUR);
            end_minuteTemp = CurrentDateTime.get(Calendar.MINUTE);
            ringtone_spinning_position = 0;
            alarm_ringtone_string = "";
            temp_alarm.game_type = 0;
            if(CurrentDateTime.get(Calendar.AM_PM) !=0)
            {
                start_hourTemp+=12;
                end_hourTemp+=12;
            }
            temp_alarm.start_hour = start_hourTemp;
            temp_alarm.start_minute = start_minuteTemp;
            temp_alarm.end_hour = end_hourTemp;
            temp_alarm.end_minute = end_minuteTemp;

        }

        showTimeOnStartButton = (Button) findViewById(R.id.start_time_button);
        showTimeOnEndButton = (Button) findViewById(R.id.end_time_button);

        // setting display time in the button by 12 hours format
        String s_start_time = "";
        if(start_hourTemp == 0) s_start_time = String.format("%02d:%02d", 12, start_minuteTemp);
        else if(start_hourTemp >= 13) s_start_time = String.format("%02d:%02d", start_hourTemp - 12, start_minuteTemp);
        else s_start_time = String.format("%02d:%02d", start_hourTemp, start_minuteTemp);
        if(start_hourTemp >= 12) s_start_time += " PM"; else s_start_time += " AM";
        String s_end_time = "";
        if(end_hourTemp == 0) s_end_time = String.format("%02d:%02d", 12, end_minuteTemp);
        else if(end_hourTemp >= 13) s_end_time = String.format("%02d:%02d", end_hourTemp - 12, end_minuteTemp);
        else s_end_time = String.format("%02d:%02d", end_hourTemp, end_minuteTemp);
        if(end_hourTemp >= 12) s_end_time += " PM"; else s_end_time += " AM";

        showTimeOnStartButton.setText(s_start_time);
        showTimeOnEndButton.setText(s_end_time);

        List<String> gameTypeList;
        gameTypeList = new ArrayList<String>();
        gameTypeList.add("Puzzle");
        gameTypeList.add("Math Problem");
        ArrayAdapter<String> gametype_adapter = new ArrayAdapter<String>(
                getApplicationContext(), R.layout.spinner_game_type_style,gameTypeList) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);
                ((TextView) v).setTextColor(0xFF000000);
                ((TextView) v).setGravity(Gravity.RIGHT);
                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(0xFF000000);
                ((TextView) v).setGravity(Gravity.RIGHT);
                return v;
            }
        };


        gametype_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGameType = (Spinner) findViewById(R.id.game_type_spinner);
        spinnerGameType.setAdapter(gametype_adapter);

        spinnerGameType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                temp_alarm.game_type = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                temp_alarm.game_type = 0;
            }
        });

        if(b_add_clock) spinnerGameType.setSelection(0);
        else spinnerGameType.setSelection(alarm_game_type);

        // CANNOT TEST THIS RINGTONE CODE IN THE EMULATOR SINCE IT DOES NOT HAVE RINGTONE FILES
        // Alarm ringtone
        final Uri[] alarmSet = getAlarms();
        ArrayList<String> alarmList = new ArrayList<String>();
        for (int i = 0; i < alarmSet.length; i++)   {
            alarmList.add(RingtoneManager.getRingtone(getApplicationContext(),
                    alarmSet[i]).getTitle(AlarmSettingActivity.this));
        }
        ArrayAdapter<String> ringtone_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, alarmList);
        spinnerRingtone = (Spinner) findViewById(R.id.ringtone_spinner);
        ringtone_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRingtone.setAdapter(ringtone_adapter);

        // Get selected item in spinner
        spinnerRingtone.setSelection(ringtone_spinning_position);
        // Get selected ringtone
        if (alarm_ringtone_string != "") {
            Uri selectedRingtone = Uri.parse(alarm_ringtone_string);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), selectedRingtone);
        }

        spinnerRingtone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmSet[position]);
                // Overwrite mute
                ringtone.setStreamType(AudioManager.STREAM_ALARM);
                temp_alarm.ringtone_spinning_position = position;
                temp_alarm.ringtone = alarmSet[position].toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmSet[0]);
            }
        });

        // Preview ringtone
        previewButton = (ImageButton) findViewById(R.id.ringtone_tester);
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(b_playing_ringtone)
                {
                    spinnerRingtone.setEnabled(true);
                    b_playing_ringtone = false;
                    ringtone.stop();
                    previewButton.setImageResource(R.drawable.ic_play_arrow);
                }
                else
                {
                    spinnerRingtone.setEnabled(false);
                    b_playing_ringtone = true;
                    ringtone.play();
                    previewButton.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        alarmListIntent = new Intent(getApplicationContext(),AlarmListActivity.class);

    }

    // Get Ringtones
    public Uri[] getAlarms()  {
        RingtoneManager ringtoneMgr = new RingtoneManager(this);
        ringtoneMgr.setType(RingtoneManager.TYPE_ALARM);
        Cursor alarmsCursor = ringtoneMgr.getCursor();
        int alarmsCount = alarmsCursor.getCount();
        if (alarmsCount == 0 && !alarmsCursor.moveToFirst()) {
            return null;
        }
        Uri[] alarms = new Uri[alarmsCount];
        while(!alarmsCursor.isAfterLast() && alarmsCursor.moveToNext()) {
            int currentPosition = alarmsCursor.getPosition();
            alarms[currentPosition] = ringtoneMgr.getRingtoneUri(currentPosition);
        }
        return alarms;
    }

    private class CheckListener implements OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            switch(buttonView.getId())
            {
                case R.id.repeat_weekday_radio_button:
                    if(isChecked) temp_alarm.repeat_weekdays = true;
                    else temp_alarm.repeat_weekdays = false;
                    break;
                case R.id.weather_switch:
                    if(isChecked) temp_alarm.weather = true;
                    else temp_alarm.weather = false;
                    break;
                case R.id.calendar_switch:
                    if(isChecked) temp_alarm.calendar = true;
                    else temp_alarm.calendar = false;
                    break;
                case R.id.toggle_sun:
                    if(isChecked) temp_alarm.weekdays[0] = true;
                    else temp_alarm.weekdays[0] = false;
                    break;
                case R.id.toggle_mon:
                    if(isChecked) temp_alarm.weekdays[1] = true;
                    else temp_alarm.weekdays[1] = false;
                    break;
                case R.id.toggle_tue:
                    if(isChecked) temp_alarm.weekdays[2] = true;
                    else temp_alarm.weekdays[2] = false;
                    break;
                case R.id.toggle_wed:
                    if(isChecked) temp_alarm.weekdays[3] = true;
                    else temp_alarm.weekdays[3] = false;
                    break;
                case R.id.toggle_thu:
                    if(isChecked) temp_alarm.weekdays[4] = true;
                    else temp_alarm.weekdays[4] = false;
                    break;
                case R.id.toggle_fri:
                    if(isChecked) temp_alarm.weekdays[5] = true;
                    else temp_alarm.weekdays[5] = false;
                    break;
                case R.id.toggle_sat:
                    if(isChecked) temp_alarm.weekdays[6] = true;
                    else temp_alarm.weekdays[6] = false;
                    break;
            }
        }
    }

    private class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Bundle bundleObject = new Bundle();
            switch(v.getId()){
                case R.id.alarm_setting_confirm_button:
                    if(b_playing_ringtone) ringtone.stop();
                    // check the start time and end time logic
                    int start_timeTemp = temp_alarm.start_hour * 60 + temp_alarm.start_minute;
                    int end_timeTemp = temp_alarm.end_hour * 60 + temp_alarm.end_minute;
                    if(start_timeTemp > end_timeTemp) Toast.makeText(AlarmSettingActivity.this, "The start time should be earlier than end time",Toast.LENGTH_SHORT).show();
                    else {
                        if (b_add_clock) {
                            requestCode = 1; // 1 means adding a clock
                        } else // otherwise we are just editing the clock
                        {
                            requestCode = 2; // 2 means updating a clock
                        }
                        bundleObject.putInt("current_alarm_index", current_alarm_index);
                        bundleObject.putSerializable("temp_alarm", temp_alarm);
                        alarmListIntent.putExtras(bundleObject);
                        AlarmSettingActivity.this.setResult(requestCode, alarmListIntent);
                        AlarmSettingActivity.this.finish();
                    }
                    break;
                case R.id.alarm_setting_cancel_button:
                    if(b_playing_ringtone) ringtone.stop();
                    requestCode = 0;
                    AlarmSettingActivity.this.setResult(requestCode, alarmListIntent);
                    AlarmSettingActivity.this.finish();
                    break;
                case R.id.start_time_button:
                    Intent startTimeIntent = new Intent(AlarmSettingActivity.this,SetClockTime.class);
                    Bundle bundleObj = new Bundle();
                    bundleObj.putBoolean("b_modify_start_time", true);
                    bundleObject.putInt("start_hour", start_hourTemp);
                    bundleObject.putInt("start_minute", start_minuteTemp);
                    bundleObject.putInt("end_hour", end_hourTemp);
                    bundleObject.putInt("end_minute", end_minuteTemp);
                    startTimeIntent.putExtras(bundleObj);
                    startActivityForResult(startTimeIntent, 1);
                    break;
                case R.id.end_time_button:
                    Intent endTimeIntent = new Intent(AlarmSettingActivity.this,SetClockTime.class);
                    bundleObject.putBoolean("b_modify_start_time", false); // it is modifying end time if it is not start time
                    bundleObject.putInt("start_hour", start_hourTemp);
                    bundleObject.putInt("start_minute", start_minuteTemp);
                    bundleObject.putInt("end_hour", end_hourTemp);
                    bundleObject.putInt("end_minute", end_minuteTemp);
                    endTimeIntent.putExtras(bundleObject);
                    startActivityForResult(endTimeIntent, 1);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case 1:
                Bundle bundleObject = data.getExtras();
                boolean b_modify_start_time =  bundleObject.getBoolean("b_modify_start_time");

                if(b_modify_start_time) {
                    start_hourTemp =  bundleObject.getInt("start_hour");
                    start_minuteTemp = bundleObject.getInt("start_minute");
                    temp_alarm.start_hour = start_hourTemp;
                    temp_alarm.start_minute = start_minuteTemp;
                }
                else{
                    end_hourTemp =  bundleObject.getInt("end_hour");
                    end_minuteTemp = bundleObject.getInt("end_minute");
                    if(end_hourTemp == 0 && end_minuteTemp == 0)
                    {
                        // prevent from setting next day
                        end_hourTemp = 23;
                        end_minuteTemp = 59;
                    }
                    temp_alarm.end_hour = end_hourTemp;
                    temp_alarm.end_minute = end_minuteTemp;
                }

                // setting display time in the button by 12 hours format
                String s_start_time = "";
                if(start_hourTemp == 0) s_start_time = String.format("%02d:%02d", 12, start_minuteTemp);
                else if(start_hourTemp >= 13) s_start_time = String.format("%02d:%02d", start_hourTemp - 12, start_minuteTemp);
                else s_start_time = String.format("%02d:%02d", start_hourTemp, start_minuteTemp);
                if(start_hourTemp >= 12) s_start_time += " PM"; else s_start_time += " AM";
                String s_end_time = "";
                if(end_hourTemp == 0) s_end_time = String.format("%02d:%02d", 12, end_minuteTemp);
                else if(end_hourTemp >= 13) s_end_time = String.format("%02d:%02d", end_hourTemp - 12, end_minuteTemp);
                else s_end_time = String.format("%02d:%02d", end_hourTemp, end_minuteTemp);
                if(end_hourTemp >= 12) s_end_time += " PM"; else s_end_time += " AM";

                showTimeOnStartButton.setText(s_start_time);
                showTimeOnEndButton.setText(s_end_time);
                break;
            case 2:
                Toast.makeText(AlarmSettingActivity.this,"cancel setting up the time",Toast.LENGTH_SHORT).show();
            default:
                break;
        }
    }

    // phone button "back" button handler - let it goes to home screen when pressed
    @Override
    public void onBackPressed() {
        requestCode = 0;
        Intent alarmListIntent = new Intent(getApplicationContext(),AlarmListActivity.class);
        AlarmSettingActivity.this.setResult(requestCode, alarmListIntent);
        AlarmSettingActivity.this.finish();
    }


}
