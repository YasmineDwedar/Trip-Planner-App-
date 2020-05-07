package com.vaatu.tripmate.ui.home.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vaatu.tripmate.R;
import com.vaatu.tripmate.ui.home.UpcomingTripsActivity;
import com.vaatu.tripmate.utils.TripModel;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mTripsRef;
    private List<TripModel> tripDetails = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mTripsRef = FirebaseDatabase.getInstance().getReference().child("trip-mate").child(currentUser.getUid()).child("upcomingtrips");

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView rev = root.findViewById(R.id.recycler);
        RecAdaptor adpater = new RecAdaptor(tripDetails, getActivity());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rev.setLayoutManager(linearLayoutManager);
        //rev.setLayoutManager(new LinearLayoutManager(getContext()));
        rev.setAdapter(adpater);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.i("DataSnapshot Loop", "##");
                tripDetails.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    tripDetails.add(ds.getValue(TripModel.class));
                }
                adpater.notifyDataSetChanged();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Database", "loadPost:onCancelled", databaseError.toException());
                // ...
            }

        };
        mTripsRef.addValueEventListener(postListener);


        return root;
    }

}
