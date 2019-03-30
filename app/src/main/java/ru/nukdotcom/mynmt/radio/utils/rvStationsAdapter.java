package ru.nukdotcom.mynmt.radio.utils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import ru.nukdotcom.mynmt.R;
import ru.nukdotcom.mynmt.radio.RadioFragment;

public class rvStationsAdapter extends RecyclerView.Adapter<rvStationsAdapter.ViewHolder> {

    private List<rvStationPair> models = new ArrayList<>();
    private View view;
    public void setItems(Collection<rvStationPair> pairs) {
        models.addAll(pairs);
        notifyDataSetChanged();
    }


    public void clearItems(){
        models.clear();
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    @NonNull
    @Override
    public rvStationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view_station, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(models.get(position));

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        CardView card;
        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.titleTextView);
            icon = (ImageView)itemView.findViewById(R.id.icon);
            card = (CardView)itemView.findViewById(R.id.radioStationCardView);
        }

        public void bind(final rvStationPair pair){
            title.setText(pair.title);
            Picasso.get().load(pair.icon)
                    .into(icon);
            card.setOnClickListener(v -> {
                RadioFragment.station_title = pair.title;
                RadioFragment.stream_128 = pair.stream_128;
                RadioFragment.stream_320 = pair.stream_320;
                EventBus.getDefault().post("station_changed");
                Log.d("RadioAdapter", pair.title);
            });
        }
    }
}

