package ru.nukdotcom.mynmt.schedule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.TabHost;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;
import ru.nukdotcom.mynmt.R;

public class GetThread extends Thread {
    public Activity activity;
    public boolean isOnRefresh = true;
    public String mode = "direct";
    public TabHost tabHost;
    public RecyclerView rvNow, rvNext, rvFirst;
    public Runnable onExecuted;
    public void setOnExecuted(Runnable onExecuted){
        this.onExecuted = onExecuted;
    }
    private String parse_schedule(String endpoint, String day) {
        org.jsoup.nodes.Document doc;
        if (day.equals("first")) {
            day = "start";
        }
        try {
            doc = Jsoup.connect(endpoint + day).userAgent("Mozilla").get();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        JsonObject jsob = new JsonObject();
        jsob.addProperty("schedule_name", doc.select("div[style='font-size: 18px; padding-bootom: 5px;']").get(0).text());
        jsob.addProperty("status", "200");
        jsob.addProperty("days", day);

        org.jsoup.nodes.Element sch = doc.select("table.schedule").get(0);

        Elements auditories_work = sch.select("tr");
        JsonArray auditories = new JsonArray();
        for (int i = 1; i <= auditories_work.size() - 2; i++) {
            Element auditory_work = auditories_work.get(i);
            JsonObject auditory = new JsonObject();
            auditory.addProperty("auditory", auditory_work.select("td.first_cell").text());
            JsonArray sessions_js = new JsonArray();
            Elements sessions = auditory_work.select("td.schedule");
            for (int j = 0; j <= sessions.size() - 1; j++) {
                sessions_js.add(sessions.get(j).text());
            }
            auditory.add("sessions", sessions_js);
            auditories.add(auditory);
        }
        jsob.add("sessions", auditories);
        return jsob.toString();
    }

    private String doGet(String url)
            throws Exception {
        Log.d("Internet", "Requesting from: " + url);
        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        //add request header
        connection.setRequestMethod("GET");
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            connection.setRequestProperty("User-Agent", "MyNMT/" + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            connection.setRequestProperty("User-Agent", "MyNMT/unknown");
        }
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Content-Type", "application/json");
        //connection.setConnectTimeout(1000);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = bufferedReader.readLine()) != null) {
            response.append(inputLine);
        }
        bufferedReader.close();
        return response.toString();
    }

    private String readTxt(int id) {
        InputStream raw = activity.getResources().openRawResource(id);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }private boolean getSavedScheduleActuality() {
        if (isOnRefresh) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy",
                new Locale("ru", "RU"));
        boolean actuality = true;
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date next = calendar.getTime();
        Log.d("Schedule Actuality", sdf.format(now));
        Log.d("Schedule Actuality", sdf.format(next));
        String schedulew = readFile("now");
        if (!schedulew.equals("")) {
            try {
                JSONObject jsob = new JSONObject(schedulew);
                String date = jsob.getString("schedule_name").split(" ")[2];
                actuality = date.equals(sdf.format(now));
            } catch (JSONException e) {
                e.printStackTrace();
                actuality = false;
            }
        } else {
            actuality = false;
        }
        String schedulet = readFile("next");
        if (!schedulew.equals("")) {
            try {
                JSONObject jsob = new JSONObject(schedulet);
                String date = jsob.getString("schedule_name").split(" ")[2];
                if (!date.equals(sdf.format(next))) {
                    actuality = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                actuality = false;
            }
        } else {
            actuality = false;
        }
        Log.d("Schedule Actuality", Boolean.toString(actuality));
        return actuality;
    }

    private String readFile(String day) {
        StringBuilder file_content = new StringBuilder();
        try {
            FileInputStream fileout = activity.openFileInput("schedule_" + day + ".json");
            InputStreamReader inputStreamReader = new InputStreamReader(fileout);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            long length = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                length += line.length();
                file_content.append(line);
            }
            fileout.close();
        } catch (IOException e) {
            Log.e("Exception", "File read failed: " + e.toString());
        }
        return file_content.toString();
    }
    private void writeToFile(String data, String day) {
        try {
            if (data != null) {
                FileOutputStream fileout = activity
                        .openFileOutput("schedule_" + day + ".json", Context.MODE_PRIVATE);
                fileout.write(data.getBytes());
                Log.d("File", fileout.getFD().toString());
                fileout.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String getSchedule(String day) {
        if (getSavedScheduleActuality()) {
            return readFile(day);
        } else {
            if (mode.equals("direct")) {
                try {
                    String schedule = doGet(activity.getResources().getString(R.string.scheduleEndpoint) + day);
                    writeToFile(schedule, day);
                    return schedule;
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            } else if (mode.equals("nukdotcom")) {
                String schedule = parse_schedule(activity.getResources().getString(R.string.scheduleEndpointDirect), day);
                writeToFile(schedule, day);
                return schedule;
            }else{
                return "";
            }
        }
    }

    private tabDataModel parseJSON(String schedule, String day, String rings, String scheduleType, String group){
        tabDataModel tab = new tabDataModel();
        listItemDataModel sessions;
        if (day.equals("now")){
            tab.tabTitle = new SimpleDateFormat("EEEE", new Locale("ru", "RU")).format(new Date());
        }
        else if (day.equals("next")){
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            tab.tabTitle = new SimpleDateFormat("EEEE", new Locale("ru", "RU")).format(calendar.getTime());
        }
        else if (day.equals("first")){
            tab.tabTitle = "Понедельник";
        }
        if (!schedule.equals("")){
            JsonObject jsob;
            try{
                jsob = new JsonParser().parse(schedule).getAsJsonObject();
            }
            catch (IllegalStateException e){
                tab.items = new ArrayList<>();
                tab.items.add(new listItemDataModel("", "Пришел неверный ответ от сервера!", "Повторите попытку!"));
                return tab;
            }
            JsonArray jsar = jsob.getAsJsonArray("sessions");
            tab.items = new ArrayList<>();
            if (jsar.size() >= 0){
                int now, previous = 0;
                for (int i = 0; i <= 7; i++){
                    for (int j = 0; j <= jsar.size() - 1; j++){
                        try {
                            JsonObject auditory = jsar.get(j).getAsJsonObject();
                            String session = auditory.getAsJsonArray("sessions").get(i).getAsString();
                            String cab = auditory.get("auditory").getAsString();
                            //TODO Доделать эту лажу
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }else{

        }
    }
    public void run(){

    }
}
