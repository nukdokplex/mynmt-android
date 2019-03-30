package ru.nukdotcom.mynmt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import ru.nukdotcom.mynmt.board.BoardFragment;
import ru.nukdotcom.mynmt.news.NewsFragment;
import ru.nukdotcom.mynmt.radio.RadioFragment;
import ru.nukdotcom.mynmt.schedule.ScheduleFragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    public static Boolean isScheduleSettingsExist = false;
    public static String group, dinner, method, type, radioQuality;
    public static AppBarLayout appBarLayout;
    public static SharedPreferences preferences;
    public ScheduleFragment ScheduleF;
    public RadioFragment RadioF;
    public NewsFragment NewsF;
    public BoardFragment BoardF;


    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
    private void createNotificationChannel(String name, String description, String id) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_schedule:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContent, ScheduleF).commit();
                    return true;
                case R.id.nav_news:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContent, NewsF).commit();
                    return true;
                case R.id.nav_board:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContent, BoardF).commit();
                    return true;
                case R.id.nav_radio:
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContent, RadioF).commit();
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        //appBarLayout = (AppBarLayout)findViewById()
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        ScheduleF = new ScheduleFragment();
        NewsF = new NewsFragment();
        BoardF = new BoardFragment();
        RadioF = new RadioFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, ScheduleF).commit();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuItem preferencesMI = menu.add(0, 1, 0, getResources().getString(R.string.action_settings));
        preferencesMI.setIntent(new Intent(this, SettingsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume(){
        if (preferences.contains("pref_schedule_method") &&
        preferences.contains("pref_schedule_type") &&
        preferences.contains("pref_schedule_group") &&
        preferences.contains("pref_schedule_dinner")){
            isScheduleSettingsExist = true;
            group = preferences.getString("pref_schedule_group", "");
            type = preferences.getString("pref_schedule_type", "student");
            dinner = preferences.getString("pref_schedule_dinner", "on");
            method = preferences.getString("pref_schedule_method", "direct");
            Log.d("Preferences", group + " " + type + " " + dinner + " " + method);
        }
        else{
            isScheduleSettingsExist = false;
        }
        radioQuality = preferences.getString("pref_radio_quality", "low");
        super.onResume();
    }
}
