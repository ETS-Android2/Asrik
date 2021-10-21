package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.BloodCamp;
import com.mitrukahitesh.asrik.utility.TimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CampAdapter extends RecyclerView.Adapter<CampAdapter.CustomVH> {

    private Context context;
    private final List<BloodCamp> camps;

    public CampAdapter(Context context, List<BloodCamp> camps) {
        this.context = context;
        this.camps = camps;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_blood_camp, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(camps.get(position));
    }

    @Override
    public int getItemCount() {
        return camps.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        private final TextView address, day, month, year, time;
        private final Button remind, locate;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            address.setSelected(true);
            day = itemView.findViewById(R.id.day);
            month = itemView.findViewById(R.id.month);
            year = itemView.findViewById(R.id.year);
            time = itemView.findViewById(R.id.time);
            remind = itemView.findViewById(R.id.remind);
            locate = itemView.findViewById(R.id.locate);
            setListeners();
        }

        private void setListeners() {
        }

        private void setView(BloodCamp camp) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(camp.getYear(), camp.getMonth(), camp.getDay());
            address.setText(camp.getAddress());
            day.setText(String.format(Locale.getDefault(), "%d", camp.getDay()));
            month.setText(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
            year.setText(String.format(Locale.getDefault(), "%d", camp.getYear()));
            time.setText(String.format(
                    Locale.getDefault(),
                    "%s - %s",
                    TimeFormatter.formatTime(camp.getStartHour(), camp.getStartMin()),
                    TimeFormatter.formatTime(camp.getEndhour(), camp.getEndMin())));
        }
    }
}
