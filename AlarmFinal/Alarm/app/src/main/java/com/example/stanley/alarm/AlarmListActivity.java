package com.example.stanley.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AlarmListActivity extends AppCompatActivity {

    private final int MAX_CLOCKS = 10;
    private int current_total_items = 0;
    private int current_alarm_index;
    private ArrayList<Alarm> alarms = new ArrayList<Alarm>(MAX_CLOCKS); // main storing data - an object: alarms
    private boolean b_show_delete_checkbox;
    private boolean [] b_delete_list = new boolean[MAX_CLOCKS];
    private boolean b_switch_changed_manually = false;

    private ListView listView;
    private Context thisContext;
    private MySimpleAdapter simpleAdapter;
    private ArrayList<HashMap<String, String>> list_for_ListView = new ArrayList<HashMap<String, String>>(); // For Alarm ListView
    private ImageButton btn_add_clock,btn_delete_clock;
    private Button btn_confirm_delete, btn_cancel_delete;
    private PendingIntent pendingIntent;
    private Bundle receiverBundle = new Bundle();

    AlarmManager alarmManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // initialize the container variables
        listView = (ListView) this.findViewById(R.id.listView);

        thisContext = this;

        // Using alarm manager to handle the time detecting
        alarmManager = (AlarmManager) AlarmListActivity.this.getSystemService(Context.ALARM_SERVICE);

        // getting the stored data from the phone
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        if(!appSharedPrefs.getBoolean("has_data", false)) // If this is the first time open the program
        {
            prefsEditor.putBoolean("has_data", true);
            prefsEditor.commit();
        }
        else // Otherwise it is no first time to run the app, then we load the data from the phone
        {
            // Get data
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("MyObject", "");
            alarms = gson.fromJson(json, new TypeToken<ArrayList<Alarm>>() {
            }.getType());

            current_total_items = alarms.size();
            for (current_alarm_index = 0; current_alarm_index < current_total_items; current_alarm_index++) {
                // add an item to listView
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("start_hour", "" + alarms.get(current_alarm_index).start_hour);
                hashMap.put("start_minute", "" + alarms.get(current_alarm_index).start_minute);
                hashMap.put("end_hour", "" + alarms.get(current_alarm_index).end_hour);
                hashMap.put("end_minute", "" + alarms.get(current_alarm_index).end_minute);
                hashMap.put("game_type", "" + alarms.get(current_alarm_index).game_type);
                hashMap.put("sun", "" + alarms.get(current_alarm_index).weekdays[0]);
                hashMap.put("mon", "" + alarms.get(current_alarm_index).weekdays[1]);
                hashMap.put("tue", "" + alarms.get(current_alarm_index).weekdays[2]);
                hashMap.put("wed", "" + alarms.get(current_alarm_index).weekdays[3]);
                hashMap.put("thu", "" + alarms.get(current_alarm_index).weekdays[4]);
                hashMap.put("fri", "" + alarms.get(current_alarm_index).weekdays[5]);
                hashMap.put("sat", "" + alarms.get(current_alarm_index).weekdays[6]);
                list_for_ListView.add(hashMap);

                // check alarms conflict
                if(alarms.get(current_alarm_index).activate) {
                    boolean b_alarm_conflict = check_alarm_conflict();
                    boolean b_current_time_conflict = check_current_time_weekday_conflict();
                    boolean b_no_weekday = check_setup_weekdays();
                    if (b_alarm_conflict || b_current_time_conflict || b_no_weekday) {
                        alarms.get(current_alarm_index).activate = false;
                    } else {
                        alarms.get(current_alarm_index).activate = true;
                    }
                }
            }
            saveDataOnPhone();
        }

        // the following 2 lines code will create an adapter to show alarms list inside ListView
        simpleAdapter = new MySimpleAdapter(this, list_for_ListView);
        listView.setAdapter(simpleAdapter);

        // The following codes will create actionbar on the screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        View viewActionBar = getLayoutInflater().inflate(R.layout.actionbar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(viewActionBar,params);

        btn_add_clock = (ImageButton)findViewById(R.id.btn_add_clock);
        btn_delete_clock = (ImageButton)findViewById(R.id.btn_delete_clock);
        btn_confirm_delete = (Button)findViewById(R.id.btn_confirm_delete);
        btn_cancel_delete = (Button)findViewById(R.id.btn_cancel_delete);

        btn_add_clock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (current_total_items >= MAX_CLOCKS) {
                    Toast.makeText(thisContext, "Cannot add more clock!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent AlarmSettingIntent = new Intent(getApplicationContext(), AlarmSettingActivity.class);
                    Bundle bundleObject = new Bundle();
                    bundleObject.putBoolean("b_add_clock", true);
                    current_alarm_index = current_total_items;
                    bundleObject.putInt("current_alarm_index", current_alarm_index);
                    AlarmSettingIntent.putExtras(bundleObject);
                    startActivityForResult(AlarmSettingIntent, 1);
                }
            }
        });
        btn_delete_clock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (current_total_items == 0) {
                    Toast.makeText(thisContext, "No Clock can be deleted", Toast.LENGTH_SHORT).show();
                } else {
                    btn_add_clock.setVisibility(Button.INVISIBLE);
                    btn_delete_clock.setVisibility(Button.INVISIBLE);
                    btn_confirm_delete.setVisibility(Button.VISIBLE);
                    btn_cancel_delete.setVisibility(Button.VISIBLE);
                    b_show_delete_checkbox = true;
                    simpleAdapter.notifyDataSetChanged();
                }
            }
        });
        btn_confirm_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_add_clock.setVisibility(Button.VISIBLE);
                btn_delete_clock.setVisibility(Button.VISIBLE);
                btn_confirm_delete.setVisibility(Button.INVISIBLE);
                btn_cancel_delete.setVisibility(Button.INVISIBLE);
                b_show_delete_checkbox = false;
                for (int i = current_total_items - 1; i >= 0; i--) {
                    if (b_delete_list[i]) {
                        alarms.remove(i); // remove alarm data structure
                        list_for_ListView.remove(i);   // remove the showing list
                        current_total_items--;
                    }
                }

                // saving the changed data to the phone
                saveDataOnPhone();
                renewAlarm();

                // reset the boolean variable
                for (int i = 0; i < MAX_CLOCKS; i++) b_delete_list[i] = false;

                // change the display of listView items
                simpleAdapter.notifyDataSetChanged();
            }
        });
        btn_cancel_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_add_clock.setVisibility(Button.VISIBLE);
                btn_delete_clock.setVisibility(Button.VISIBLE);
                btn_confirm_delete.setVisibility(Button.INVISIBLE);
                btn_cancel_delete.setVisibility(Button.INVISIBLE);

                b_show_delete_checkbox = false;
                // reset the boolean variable
                for (int i = 0; i < MAX_CLOCKS; i++) b_delete_list[i] = false;

                // change the display of listView items
                simpleAdapter.notifyDataSetChanged();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        // update the alarms information every time the alarm called
        if(current_total_items != 0) {
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext());
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

            Gson gson = new Gson();
            String json = appSharedPrefs.getString("MyObject", "");
            alarms = gson.fromJson(json, new TypeToken<ArrayList<Alarm>>(){}.getType());

            for (current_alarm_index = 0; current_alarm_index < current_total_items; current_alarm_index++) {
                list_for_ListView.get(current_alarm_index).put("start_hour", "" + alarms.get(current_alarm_index).start_hour);
                list_for_ListView.get(current_alarm_index).put("start_minute", "" + alarms.get(current_alarm_index).start_minute);
                list_for_ListView.get(current_alarm_index).put("end_hour", "" + alarms.get(current_alarm_index).end_hour);
                list_for_ListView.get(current_alarm_index).put("end_minute", "" + alarms.get(current_alarm_index).end_minute);
                list_for_ListView.get(current_alarm_index).put("game_type", "" + alarms.get(current_alarm_index).game_type);
                list_for_ListView.get(current_alarm_index).put("sun", "" + alarms.get(current_alarm_index).weekdays[0]);
                list_for_ListView.get(current_alarm_index).put("mon", "" + alarms.get(current_alarm_index).weekdays[1]);
                list_for_ListView.get(current_alarm_index).put("tue", "" + alarms.get(current_alarm_index).weekdays[2]);
                list_for_ListView.get(current_alarm_index).put("wed", "" + alarms.get(current_alarm_index).weekdays[3]);
                list_for_ListView.get(current_alarm_index).put("thu", "" + alarms.get(current_alarm_index).weekdays[4]);
                list_for_ListView.get(current_alarm_index).put("fri", "" + alarms.get(current_alarm_index).weekdays[5]);
                list_for_ListView.get(current_alarm_index).put("sat", "" + alarms.get(current_alarm_index).weekdays[6]);

                // Check whether all weekday gone only
                if(alarms.get(current_alarm_index).activate) {
                    boolean b_no_weekday = check_setup_weekdays();
                    if (b_no_weekday) alarms.get(current_alarm_index).activate = false;
                }
            }

            saveDataOnPhone();
            renewAlarm();
            simpleAdapter.notifyDataSetChanged();
        }
    }

    protected void renewAlarm(){

        boolean check_any_alarms_enable = false;
        for(int i=0; i<current_total_items; i++)
            if(alarms.get(i).activate) {
                check_any_alarms_enable = true;
                break;
            }

        if(check_any_alarms_enable) {

            int count = 0;

            Calendar ca = Calendar.getInstance();

            int current_hour = ca.get(Calendar.HOUR_OF_DAY);
            int current_min = ca.get(Calendar.MINUTE);
            int current_day = ca.get(Calendar.DAY_OF_WEEK) - 1;
            int current_time = current_hour * 60 + current_min;

            int closest_time = 60 * 24 * 7; // worst case: a week from now
            int check_day = 0;

            // find alarm in next six day
            for (int i = 0; i < current_total_items; i++) {
                int avaiable_day = 0;
                if (alarms.get(i).activate) {
                    for(int j=current_day; j<6+current_day; j++) {
                        if(j > 6) check_day = j-7;
                        else check_day = j;
                        if(alarms.get(i).weekdays[check_day]) {
                            int alarm_time = alarms.get(i).start_hour * 60 + alarms.get(i).start_minute + avaiable_day * 60 * 24;
                            int diff_time = alarm_time - current_time;
                            if(diff_time > 0) {
                                if(diff_time < closest_time)
                                {
                                    count = i;
                                    closest_time = diff_time;
                                }
                                break;
                            }
                        }
                        avaiable_day++;
                    }
                }
            }

            // find the alarm one week after
            if(closest_time == 60 * 24 * 7)
            {
                for(int i=0; i<current_total_items; i++)
                {
                    if(alarms.get(i).repeat_weekdays)
                    {
                        closest_time = alarms.get(i).start_hour * 60 + alarms.get(i).start_minute + 7 * 60 * 24 - current_time;
                        break;
                    }
                }
            }

            Alarm send_alarm = alarms.get(count);
            receiverBundle.putSerializable("alarm", send_alarm);
            Intent intent = new Intent(AlarmListActivity.this, AlarmReceiver.class);
            intent.putExtras(receiverBundle);
            intent.putExtra("count", count);

            ca.set(Calendar.HOUR_OF_DAY, send_alarm.start_hour);
            ca.set(Calendar.MINUTE, send_alarm.start_minute);
            ca.set(Calendar.SECOND, 0);

            pendingIntent = PendingIntent.getBroadcast(thisContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, ca.getTimeInMillis(), pendingIntent);
        }
        else alarmManager.cancel(pendingIntent);
    }

    private ArrayList<HashMap<String, String>> mData;
    // list of buttons and switches creater
    private class MySimpleAdapter extends BaseAdapter {

        LayoutInflater inflater = null;

        public MySimpleAdapter(Context context, ArrayList<HashMap<String, String>> data) {
            mData = data;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            current_alarm_index = position;

            if (convertView == null) convertView = inflater.inflate(R.layout.simple_adapter, null);
            final View switch_convertView = convertView;

            // show delete clock checkbox if pressed delete clock button
            if(b_show_delete_checkbox) ((CheckBox) convertView.findViewById(R.id.list_checkbox)).setVisibility(Button.VISIBLE);
            else ((CheckBox) convertView.findViewById(R.id.list_checkbox)).setVisibility(Button.INVISIBLE);
            ((CheckBox) convertView.findViewById(R.id.list_checkbox)).setChecked(false); // always uncheck at the first time

            HashMap<String, String> time = mData.get(position);
            int start_hour = Integer.parseInt(time.get("start_hour"));
            int start_minute = Integer.parseInt(time.get("start_minute"));
            int end_hour = Integer.parseInt(time.get("end_hour"));
            int end_minute = Integer.parseInt(time.get("end_minute"));
            int game_type = Integer.parseInt(time.get("game_type"));
            String sun = time.get("sun");
            String mon = time.get("mon");
            String tue = time.get("tue");
            String wed = time.get("wed");
            String thu = time.get("thu");
            String fri = time.get("fri");
            String sat = time.get("sat");

            if(alarms.get(position).activate) {
                ((Button) convertView.findViewById(R.id.list_button)).setBackgroundColor(0xFFFFFFFF);
                b_switch_changed_manually = true;
                ((Switch) convertView.findViewById(R.id.list_switch)).setChecked(true);
                b_switch_changed_manually = false;
            }
            else {
                ((Button) convertView.findViewById(R.id.list_button)).setBackgroundColor(0xFF313839);
                b_switch_changed_manually = true;
                ((Switch) convertView.findViewById(R.id.list_switch)).setChecked(false);
                b_switch_changed_manually = false;
            }

            // setting display time in the button by 12 hours format
            String s_start_time = "";
            if(start_hour == 0) s_start_time = String.format("%02d:%02d", 12, start_minute);
            else if(start_hour >= 13) s_start_time = String.format("%02d:%02d", start_hour - 12, start_minute);
            else s_start_time = String.format("%02d:%02d", start_hour, start_minute);
            if(start_hour >= 12) s_start_time += " PM"; else s_start_time += " AM";
            String s_end_time = "";
            if(end_hour == 0) s_end_time = String.format("%02d:%02d", 12, end_minute);
            else if(end_hour >= 13) s_end_time = String.format("%02d:%02d", end_hour - 12, end_minute);
            else s_end_time = String.format("%02d:%02d", end_hour, end_minute);
            if(end_hour >= 12) s_end_time += " PM"; else s_end_time += " AM";
            String s_puzzle_type = "";
            if(game_type == 0) s_puzzle_type += "Game Type: Puzzle";
            else if(game_type == 1) s_puzzle_type += "Type: Math problem";
            String s_weekdays = "";
            if(mon.equals("true")) s_weekdays += "M ";
            if(tue.equals("true")) s_weekdays += "Tu ";
            if(wed.equals("true")) s_weekdays += "W ";
            if(thu.equals("true")) s_weekdays += "Th ";
            if(fri.equals("true")) s_weekdays += "F ";
            if(sat.equals("true")) s_weekdays += "Sa ";
            if(sun.equals("true")) s_weekdays += "Su ";

            String display_in_list_button = s_start_time + " ~ " + s_end_time + "\n" + s_puzzle_type + "\n" + s_weekdays;
            ((Button) convertView.findViewById(R.id.list_button)).setText(display_in_list_button);

            ((Button) convertView.findViewById(R.id.list_button)).setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    current_alarm_index = position;
                    Intent AlarmSettingIntent = new Intent(getApplicationContext(), AlarmSettingActivity.class);
                    Bundle bundleObject = new Bundle();
                    bundleObject.putBoolean("b_add_clock", false);
                    bundleObject.putInt("current_alarm_index", current_alarm_index);
                    bundleObject.putSerializable("edited_alarm", alarms.get(current_alarm_index));
                    AlarmSettingIntent.putExtras(bundleObject);
                    startActivityForResult(AlarmSettingIntent, 1);
                }
            });

            ((Switch) convertView.findViewById(R.id.list_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!b_switch_changed_manually) {
                        current_alarm_index = position;
                        if (isChecked) // if turn on
                        {
                            //check whether the current alarm conflicts with other alarms
                            boolean b_alarm_conflict = check_alarm_conflict();
                            boolean b_current_time_conflict = check_current_time_weekday_conflict();
                            boolean b_no_weekday = check_setup_weekdays();
                            if (b_alarm_conflict || b_current_time_conflict || b_no_weekday) {
                                switch_convertView.findViewById(R.id.list_button).setBackgroundColor(0xFF313839);
                                b_switch_changed_manually = true;
                                ((Switch) switch_convertView.findViewById(R.id.list_switch)).setChecked(false);
                                b_switch_changed_manually = false;
                                if (b_alarm_conflict) Toast.makeText(thisContext, "Alarm time conflicted with another alarms!", Toast.LENGTH_SHORT).show();
                                else if(b_current_time_conflict) Toast.makeText(thisContext, "Alarm time conflicted with current time!", Toast.LENGTH_SHORT).show();
                                else if(b_no_weekday) Toast.makeText(thisContext, "Weekdays did not setup!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                alarms.get(current_alarm_index).activate = true;
                                switch_convertView.findViewById(R.id.list_button).setBackgroundColor(0xFFFFFFFF);
                            }
                        } else  // if turn off
                        {
                            alarms.get(current_alarm_index).activate = false;
                            switch_convertView.findViewById(R.id.list_button).setBackgroundColor(0xFF313839);
                        }
                        saveDataOnPhone();
                        renewAlarm();
                    }
                    b_switch_changed_manually = false;
                }
            });

            ((CheckBox) convertView.findViewById(R.id.list_checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) b_delete_list[position] = true;
                    else b_delete_list[position] = false;
                }
            });

            return convertView;
        }
    }

    // update the result for the changing data from Alarm_setting activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 1) {  // if it is 1, it means adding a new alarm
            //Get back the data from other activity
            Bundle bundleObject = data.getExtras();

            // update alarms ArrayList and current_total_items
            current_alarm_index = bundleObject.getInt("current_alarm_index");
            alarms.add((Alarm) bundleObject.getSerializable("temp_alarm"));
            current_total_items = alarms.size();

            // update the showing alarm list item
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("start_hour", ""+alarms.get(current_alarm_index).start_hour);
            hashMap.put("start_minute", "" + alarms.get(current_alarm_index).start_minute);
            hashMap.put("end_hour", ""+alarms.get(current_alarm_index).end_hour);
            hashMap.put("end_minute", "" + alarms.get(current_alarm_index).end_minute);
            hashMap.put("game_type", "" + alarms.get(current_alarm_index).game_type);
            hashMap.put("sun", "" + alarms.get(current_alarm_index).weekdays[0]);
            hashMap.put("mon", "" + alarms.get(current_alarm_index).weekdays[1]);
            hashMap.put("tue", "" + alarms.get(current_alarm_index).weekdays[2]);
            hashMap.put("wed", "" + alarms.get(current_alarm_index).weekdays[3]);
            hashMap.put("thu", "" + alarms.get(current_alarm_index).weekdays[4]);
            hashMap.put("fri", "" + alarms.get(current_alarm_index).weekdays[5]);
            hashMap.put("sat", "" + alarms.get(current_alarm_index).weekdays[6]);

            list_for_ListView.add(hashMap);

            // update listView
            simpleAdapter.notifyDataSetChanged();
        }
        else if(resultCode == 2) {  // if it is 2, it means adding a new alarm
            //Get back the data from other activity
            Bundle bundleObject = data.getExtras();

            // update alarms ArrayList and current_total_items
            current_alarm_index = bundleObject.getInt("current_alarm_index");
            Alarm tempAlarm = (Alarm) bundleObject.getSerializable("temp_alarm");

            // update the data in alarm object
            alarms.get(current_alarm_index).start_hour = tempAlarm.start_hour;
            alarms.get(current_alarm_index).start_minute = tempAlarm.start_minute;
            alarms.get(current_alarm_index).end_hour = tempAlarm.end_hour;
            alarms.get(current_alarm_index).end_minute = tempAlarm.end_minute;
            alarms.get(current_alarm_index).activate = tempAlarm.activate;
            alarms.get(current_alarm_index).game_type = tempAlarm.game_type;
            alarms.get(current_alarm_index).ringtone_spinning_position = tempAlarm.ringtone_spinning_position;
            alarms.get(current_alarm_index).ringtone = tempAlarm.ringtone;
            for(int i=0; i<7; i++)
                alarms.get(current_alarm_index).weekdays[i] = tempAlarm.weekdays[i];
            alarms.get(current_alarm_index).repeat_weekdays = tempAlarm.repeat_weekdays;
            alarms.get(current_alarm_index).weather = tempAlarm.weather;
            alarms.get(current_alarm_index).calendar = tempAlarm.calendar;

            // update listView
            list_for_ListView.get(current_alarm_index).put("start_hour", "" + alarms.get(current_alarm_index).start_hour);
            list_for_ListView.get(current_alarm_index).put("start_minute", "" + alarms.get(current_alarm_index).start_minute);
            list_for_ListView.get(current_alarm_index).put("end_hour", "" + alarms.get(current_alarm_index).end_hour);
            list_for_ListView.get(current_alarm_index).put("end_minute", "" + alarms.get(current_alarm_index).end_minute);
            list_for_ListView.get(current_alarm_index).put("game_type", "" + alarms.get(current_alarm_index).game_type);
            list_for_ListView.get(current_alarm_index).put("sun", "" + alarms.get(current_alarm_index).weekdays[0]);
            list_for_ListView.get(current_alarm_index).put("mon", "" + alarms.get(current_alarm_index).weekdays[1]);
            list_for_ListView.get(current_alarm_index).put("tue", "" + alarms.get(current_alarm_index).weekdays[2]);
            list_for_ListView.get(current_alarm_index).put("wed", "" + alarms.get(current_alarm_index).weekdays[3]);
            list_for_ListView.get(current_alarm_index).put("thu", "" + alarms.get(current_alarm_index).weekdays[4]);
            list_for_ListView.get(current_alarm_index).put("fri", "" + alarms.get(current_alarm_index).weekdays[5]);
            list_for_ListView.get(current_alarm_index).put("sat", "" + alarms.get(current_alarm_index).weekdays[6]);

            simpleAdapter.notifyDataSetChanged();
        }

        if(resultCode != 0) {
            // handle alarms conflict
            boolean b_alarm_conflict = check_alarm_conflict();
            boolean b_current_time_conflict = check_current_time_weekday_conflict();
            boolean b_no_weekday = check_setup_weekdays();
            if (b_alarm_conflict || b_current_time_conflict || b_no_weekday) {
                alarms.get(current_alarm_index).activate = false;
            } else {
                alarms.get(current_alarm_index).activate = true;
            }
        }

        // update the data on phone
        saveDataOnPhone();
    }

    public boolean check_alarm_conflict(){
        int current_button_alarm_start_time = alarms.get(current_alarm_index).start_hour * 60 + alarms.get(current_alarm_index).start_minute;
        int current_button_alarm_end_time = alarms.get(current_alarm_index).end_hour * 60 + alarms.get(current_alarm_index).end_minute;
        for (int i = 0; i < current_total_items; i++) {
            if (i != current_alarm_index && alarms.get(i).activate == true) {
                int another_alarm_start_time = alarms.get(i).start_hour * 60 + alarms.get(i).start_minute;
                int another_alarm_end_time = alarms.get(i).end_hour * 60 + alarms.get(i).end_minute;
                if ((current_button_alarm_start_time >= another_alarm_start_time && current_button_alarm_start_time <= another_alarm_end_time) ||
                        (current_button_alarm_end_time >= another_alarm_start_time && current_button_alarm_end_time <= another_alarm_end_time) ||
                        (another_alarm_start_time >= current_button_alarm_start_time && another_alarm_start_time <= current_button_alarm_end_time) ||
                        (another_alarm_end_time >= current_button_alarm_start_time && another_alarm_end_time <= current_button_alarm_end_time)) {
                    for(int j=0; j<7; j++)
                    {
                        if(alarms.get(i).weekdays[j] == true)
                            if(alarms.get(i).weekdays[j] == alarms.get(current_alarm_index).weekdays[j]) return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean check_current_time_weekday_conflict(){
        int current_phone_time = get_current_phone_time();
        int current_phone_weekday = get_current_phone_weekday();
        int current_button_alarm_start_time = alarms.get(current_alarm_index).start_hour * 60 + alarms.get(current_alarm_index).start_minute;
        boolean current_button_alarm_weekday = alarms.get(current_alarm_index).weekdays[current_phone_weekday];
        if(current_phone_time == current_button_alarm_start_time) {
            if(current_button_alarm_weekday) return true;
        }
        return false;
    }

    public int get_current_phone_time(){
        Calendar CurrentDateTime = Calendar.getInstance();
        int current_phone_hour = CurrentDateTime.get(Calendar.HOUR);
        int current_phone_minute =CurrentDateTime.get(Calendar.MINUTE);
        if(CurrentDateTime.get(Calendar.AM_PM) !=0) current_phone_hour+=12;
        int current_phone_time = current_phone_hour * 60 + current_phone_minute;
        return current_phone_time;
    }

    public int get_current_phone_weekday(){
        Calendar CurrentDateTime = Calendar.getInstance();
        int current_phone_weekday = CurrentDateTime.get(Calendar.DAY_OF_WEEK)-1;
        return current_phone_weekday;
    }

    public boolean check_setup_weekdays() {
        for (int i = 0; i < 7; i++) if(alarms.get(current_alarm_index).weekdays[i]) return false;
        return true;
    }

    public void saveDataOnPhone(){
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(thisContext);
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

        // saving the changed data to the phone
        if(current_total_items == 0)
        {
            prefsEditor.putBoolean("has_data", false);
            prefsEditor.commit();
        }
        else
        {
            prefsEditor.putBoolean("has_data", true);
            Gson gson = new Gson();
            String json = gson.toJson(alarms);
            prefsEditor.putString("MyObject", json);
            prefsEditor.commit();
        }
    }

    // phone button "back" button handler - let it goes to home screen when pressed
    @Override
    public void onBackPressed() {
        Intent backHomeScreen = new Intent(Intent.ACTION_MAIN);
        backHomeScreen.addCategory(Intent.CATEGORY_HOME);
        backHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(backHomeScreen);
    }

    // delete the time detecting for each alarms when leaving the program
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        alarmManager.cancel(pendingIntent);
        pendingIntent = null;
        alarmManager = null;
    }
}
