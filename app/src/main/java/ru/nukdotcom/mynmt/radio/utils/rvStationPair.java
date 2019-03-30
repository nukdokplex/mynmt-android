package ru.nukdotcom.mynmt.radio.utils;

public class rvStationPair {
    public String title;
    public String stream_128;
    public String stream_320;
    public String icon;
    public rvStationPair(String title, String stream_128, String stream_320, String icon){
        this.title = title;
        this.stream_128 = stream_128;
        this.stream_320 = stream_320;
        this.icon = icon;
    }
}