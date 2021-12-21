/*
    Fragment under History tab
    Shows the list of requests the user has made till date
 */

package com.mitrukahitesh.asrik.fragments.historyfragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.RequestHistory;
import com.mitrukahitesh.asrik.models.blood.BloodRequest;
import com.mitrukahitesh.asrik.helpers.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class History extends Fragment {

    private TextView noReq;
    private RecyclerView recyclerView;
    private final List<BloodRequest> requestList = new ArrayList<>();
    private RequestHistory adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public History() {
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach and before onCreateView
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between onCreate and onViewCreated
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Called immediately after onCreateView has returned,
     * but before any saved state has been restored in to the view.
     * Set references to views
     * Set listeners to views
     * Set initial values of views
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (recyclerView == null) {
            adapter = new RequestHistory(requireContext(), requestList);
            fetchData();
        } else {
            adapter = (RequestHistory) recyclerView.getAdapter();
        }
        swipeRefreshLayout = view.findViewById(R.id.root);
        noReq = view.findViewById(R.id.no_requests);
        recyclerView = view.findViewById(R.id.recycler);
        if (requestList.size() != 0) {
            recyclerView.setVisibility(View.VISIBLE);
            noReq.setVisibility(View.GONE);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
            }
        });
    }

    /**
     * Fetch all the requests user has made previously
     */
    private void fetchData() {
        FirebaseFirestore.getInstance()
                .collection(Constants.REQUESTS)
                .whereEqualTo(Constants.UID.toLowerCase(Locale.ROOT), FirebaseAuth.getInstance().getUid())
                .orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            requestList.clear();
                            for (QueryDocumentSnapshot querySnapshot : task.getResult()) {
                                BloodRequest request = querySnapshot.toObject(BloodRequest.class);
                                requestList.add(request);
                            }
                            swipeRefreshLayout.setRefreshing(false);
                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                            noReq.setVisibility(View.GONE);
                        }
                        Log.i("Asrik: History Requests", "fetched " + requestList.size());
                    }
                });
    }
}