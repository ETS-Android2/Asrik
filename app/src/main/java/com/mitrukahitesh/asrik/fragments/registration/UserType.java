package com.mitrukahitesh.asrik.fragments.registration;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.helpers.Constants;

public class UserType extends Fragment {

    public UserType() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button admin = view.findViewById(R.id.admin);
        Button normal = view.findViewById(R.id.normal);
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.ADMIN, true);
                Navigation.findNavController(view).navigate(R.id.action_userType_to_phoneNumber, bundle);
            }
        });
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.ADMIN, false);
                Navigation.findNavController(view).navigate(R.id.action_userType_to_phoneNumber, bundle);
            }
        });
    }
}