package com.vaatu.tripmate.ui.home.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.vaatu.tripmate.R;
import com.vaatu.tripmate.data.remote.network.FirebaseDB;
import com.vaatu.tripmate.utils.TripModel;

import java.util.ArrayList;
import java.util.List;

public class HomeAdaptor extends RecyclerView.Adapter<HomeAdaptor.ViewHolder> {

    List<TripModel> list = new ArrayList<>();
    List<TripModel> canceledlist = new ArrayList<>();
    Context cntxt;
    FirebaseDB mFirebaseDB;


    public HomeAdaptor(List<TripModel> list, Context cntxt) {
        this.list = list;
        this.cntxt = cntxt;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflate = LayoutInflater.from(parent.getContext());
        View v = inflate.inflate(R.layout.custom_card_row, parent, false);
        mFirebaseDB = FirebaseDB.getInstance();
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.start.setText(list.get(position).startloc);
        holder.name.setText(list.get(position).tripname);
        holder.date.setText(list.get(position).date);

        holder.time.setText(list.get(position).time);
        holder.end.setText(list.get(position).endloc);
        holder.startnowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.get(position).setStatus("Done!");
                mFirebaseDB.addTripToHistory(list.get(position));
                mFirebaseDB.removeFromUpcoming(list.get(position));
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + list.get(position).getEndloc());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                cntxt.startActivity(mapIntent);
            }
        });
        holder.popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(cntxt, holder.popup);
                popupMenu.inflate(R.menu.menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //handle item selection from the card pop menu
                        if (item.getItemId() == R.id.editnote) {
                            //Toast.makeText(cntxt, "View note", Toast.LENGTH_LONG).show();


                            PopupMenu pop = new PopupMenu(cntxt, v);
                            pop.inflate(R.menu.notes_menu);
                            for (String n : list.get(position).getNotes()) {

                                pop.getMenu().add(n);
                            }
                            pop.show();

                        }
                        if (item.getItemId() == R.id.cancel) {
                            // Toast.makeText(cntxt, "Cancel Trip : " + position, Toast.LENGTH_LONG).show();
                            Toast.makeText(cntxt, "Canceled Trip!", Toast.LENGTH_LONG).show();
                            list.get(position).setStatus("Canceled!");
                            mFirebaseDB.addTripToHistory(list.get(position));
                            mFirebaseDB.removeFromUpcoming(list.get(position));
                            notifyDataSetChanged();

                        }
                        return false;
                    }
                });

                popupMenu.show();

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardview;
        TextView start, end, date, time, name;
        Button popup, startnowBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            startnowBtn = itemView.findViewById(R.id.startnow);
            start = itemView.findViewById(R.id.start_loc_id);
            end = itemView.findViewById(R.id.end_loc_id);
            time = itemView.findViewById(R.id.Time_id);
            name = itemView.findViewById(R.id.trip_name_id);
            date = itemView.findViewById(R.id.Date_id);
            popup = itemView.findViewById(R.id.pop_menu_id);
        }
    }
}
