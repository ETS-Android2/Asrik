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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mitrukahitesh.asrik.R;

public class PendingRequests extends Fragment {

    private RecyclerView recyclerView;
    private ConstraintLayout root;
    private com.mitrukahitesh.asrik.adapters.PendingRequests adapter;
    private FloatingActionButton fab;
    private NavController controller;

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
        if (recyclerView == null) {
            adapter = new com.mitrukahitesh.asrik.adapters.PendingRequests(requireContext(), root);
        } else {
            adapter = (com.mitrukahitesh.asrik.adapters.PendingRequests) recyclerView.getAdapter();
        }
        controller = Navigation.findNavController(view);
        recyclerView = view.findViewById(R.id.recycler);
        fab = view.findViewById(R.id.fab);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_pendingRequests_to_bloodCamp);
            }
        });
    }
}