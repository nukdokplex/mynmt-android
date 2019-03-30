package ru.nukdotcom.mynmt.radio;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nukdotcom.mynmt.MainActivity;
import ru.nukdotcom.mynmt.R;
import ru.nukdotcom.mynmt.radio.player.PlaybackStatus;
import ru.nukdotcom.mynmt.radio.player.RadioManager;
import ru.nukdotcom.mynmt.radio.utils.rvStationPair;
import ru.nukdotcom.mynmt.radio.utils.rvStationsAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RadioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RadioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View view;
    RadioManager radioManager;
    private RecyclerView rvStations;
    public static String station_title = "Radio Record",
            code = "rr",
            stream_320 = "http://air.radiorecord.ru:805/rr_320",
            stream_128 = "http://air.radiorecord.ru:805/rr_128";
    public Drawable play, pause;
    public static Boolean isHQ;
    TextView titleTextView, subtitleTextView;

    public RadioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RadioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RadioFragment newInstance(String param1, String param2) {
        RadioFragment fragment = new RadioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_radio, container, false);
        isHQ = MainActivity.radioQuality != null && MainActivity.radioQuality.equals("hq");
        titleTextView = (TextView)view.findViewById(R.id.titleTextView);
        subtitleTextView = (TextView)view.findViewById(R.id.subtitleTextView);
        subtitleTextView.setText(station_title);
        ((MainActivity) getActivity()).setActionBarTitle(view.getResources().getString(R.string.nav_radio));
        radioManager = RadioManager.with(getActivity());
        rvStationsAdapter adapter = new rvStationsAdapter();
        play = getResources().getDrawable(R.drawable.ic_play_black);
        pause = getResources().getDrawable(R.drawable.ic_pause_black);
        ((ImageButton)view.findViewById(R.id.playTrigger)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHQ){radioManager.playOrPause(stream_320);}
                else{radioManager.playOrPause(stream_128);}
            }
        });
        rvStations = (RecyclerView)view.findViewById(R.id.rvStations);
        rvStations.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvStations.setAdapter(adapter);
        ThreadLoadStations thread = new ThreadLoadStations(getActivity());
        thread.adapter = adapter;
        thread.start();
        EventBus.getDefault().register(this);
        return view;
    }

    @Subscribe
    public void onEvent(String status){
        //Toast.makeText(view.getContext(), status, Toast.LENGTH_LONG).show();
        Log.d("Status/EventBus", status);
        switch (status){
            case PlaybackStatus.LOADING:
                //Toast.makeText(view.getContext(), "Загрузка", Toast.LENGTH_LONG).show();
                break;
            case PlaybackStatus.PLAYING:
                ((ImageButton)view.findViewById(R.id.playTrigger)).setBackground(pause);
                break;
            case PlaybackStatus.PAUSED:
                ((ImageButton)view.findViewById(R.id.playTrigger)).setBackground(play);
                break;
            case "station_changed":
                Log.d("Status/EventBus", "station changed ("+station_title+")");
                if (radioManager.isPlaying()){
                    if (isHQ) {
                        radioManager.playOrPause(stream_320);
                        radioManager.playOrPause(stream_320);
                    }
                    else{
                        radioManager.playOrPause(stream_128);
                        radioManager.playOrPause(stream_128);
                    }
                }
                else {
                    if (isHQ){radioManager.playOrPause(stream_320);}
                    else{radioManager.playOrPause(stream_128);}
                }
                subtitleTextView.setText(station_title);

                break;

        }
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onResume(){
        super.onResume();
        radioManager.bind();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public class ThreadLoadStations extends Thread {
        public Activity activity;
        public rvStationsAdapter adapter;
        ThreadLoadStations(Activity activity){
            this.activity = activity;
        }



        private String readTxt(int id)
        {
            InputStream raw = activity.getResources().openRawResource(id);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i;
            try
            {
                i = raw.read();
                while (i != -1)
                {
                    byteArrayOutputStream.write(i);
                    i = raw.read();
                }
                raw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


            return byteArrayOutputStream.toString();

        }
        @Override
        public void run() {
            String stations = readTxt(R.raw.stations);
            try{
                JSONObject jsob = new JSONObject(stations);
                final ArrayList<rvStationPair> pairs = new ArrayList<rvStationPair>();
                JSONArray jsar = jsob.getJSONArray("stations");
                for (int i = 0; i <= jsar.length()-1; i++){
                    JSONObject station = jsar.getJSONObject(i);
                    pairs.add(new rvStationPair(station.getString("station_name"),
                            station.getString("stream_128"),
                            station.getString("stream_320"),
                            station.getString("icon")));
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setItems(pairs);
                        adapter.notifyDataSetChanged();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
