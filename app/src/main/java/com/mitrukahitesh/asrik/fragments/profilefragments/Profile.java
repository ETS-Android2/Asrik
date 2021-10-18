package com.mitrukahitesh.asrik.fragments.profilefragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.utility.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    private TextView name, pincode, bloodGroup, city, phone, email;
    private FrameLayout root;
    private CircleImageView dp;

    public Profile() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.root);
        name = view.findViewById(R.id.name);
        pincode = view.findViewById(R.id.pincode);
        bloodGroup = view.findViewById(R.id.blood_group);
        city = view.findViewById(R.id.city);
        phone = view.findViewById(R.id.phone);
        email = view.findViewById(R.id.email);
        dp = view.findViewById(R.id.profile_image);
        view.findViewById(R.id.card1).setBackgroundResource(R.drawable.dashed_border);
        view.findViewById(R.id.card2).setBackgroundResource(R.drawable.dashed_border);
        setValues();
    }

    private void setValues() {
        SharedPreferences preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        name.setText(preferences.getString(Constants.NAME, ""));
        bloodGroup.setText(preferences.getString(Constants.BLOOD_GROUP, ""));
        city.setText(preferences.getString(Constants.CITY, ""));
        phone.setText(preferences.getString(Constants.NUMBER, ""));
        email.setText(preferences.getString(Constants.EMAIL, ""));
        pincode.setText(String.format("#%s", preferences.getString(Constants.PIN_CODE, "")));
        if (!preferences.getString(Constants.PROFILE_PIC_URL, "").equals(""))
            Glide.with(requireContext()).load(preferences.getString(Constants.PROFILE_PIC_URL, "")).into(dp);
        else
            Glide.with(requireContext()).load(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_usercircle_large)).centerCrop().into(dp);
    }
}