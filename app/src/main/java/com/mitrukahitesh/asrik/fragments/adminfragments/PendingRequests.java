package com.mitrukahitesh.asrik.fragments.adminfragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mitrukahitesh.asrik.R;

public class PendingRequests extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout root;
    private com.mitrukahitesh.asrik.adapters.PendingRequests adapter;
    private FloatingActionButton fab;
    private NavController controller;
    private TextView noReq;

    public PendingRequests() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.root);
        noReq = view.findViewById(R.id.no_requests);
        controller = Navigation.findNavController(view);
        if (recyclerView == null) {
            adapter = new com.mitrukahitesh.asrik.adapters.PendingRequests(requireContext(), root, controller, noReq);
        } else {
            adapter = (com.mitrukahitesh.asrik.adapters.PendingRequests) recyclerView.getAdapter();
        }
        recyclerView = view.findViewById(R.id.recycler);
        fab = view.findViewById(R.id.fab);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        noReq.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_pendingRequests_to_bloodCamp);
            }
        });
        root.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.setAdapter(new com.mitrukahitesh.asrik.adapters.PendingRequests(requireContext(), root, controller, noReq));
            }
        });
    }
}