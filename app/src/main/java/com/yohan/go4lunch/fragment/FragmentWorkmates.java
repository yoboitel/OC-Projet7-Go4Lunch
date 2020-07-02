package com.yohan.go4lunch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.WorkmatesAdapter;
import com.yohan.go4lunch.model.User;

import java.util.ArrayList;
import java.util.List;

public class FragmentWorkmates extends Fragment {

    private RecyclerView rcWorkmates;
    private WorkmatesAdapter workmatesAdapter;
    private ArrayList<User> workmatesList;

    public FragmentWorkmates() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workmates, container, false);

        //Init RecyclerView And Adapter
        workmatesList = new ArrayList<>();
        rcWorkmates = v.findViewById(R.id.rcWorkmates);
        rcWorkmates.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rcWorkmates.setHasFixedSize(true);

        loadWorkmatesFromFirestore();

        return v;
    }

    private void loadWorkmatesFromFirestore() {

        if(workmatesList.size()>0)
            workmatesList.clear();

        Query workmatesQuery = FirebaseFirestore.getInstance().collection("Users");
        workmatesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot querySnapshot: task.getResult()){

                    //For each users document found in firestore, create a user object with its info and add it in the list.
                    User user= new User(
                            querySnapshot.getString("uid"),
                            querySnapshot.getString("firstnameAndName"),
                            querySnapshot.getString("photoUrl"),
                            querySnapshot.getString("choosedRestaurantId"),
                            querySnapshot.getBoolean("notificationActive"));

                    workmatesList.add(user);
                }

                workmatesAdapter = new WorkmatesAdapter(requireContext(), workmatesList, new WorkmatesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Toast.makeText(requireContext(), "Open Detailed Activity for restaurant choosed by " + workmatesList.get(position).getFirstnameAndName(), Toast.LENGTH_SHORT).show();
                    }
                });
                rcWorkmates.setAdapter(workmatesAdapter);
                workmatesAdapter.notifyDataSetChanged();
            }
        });
    }
}