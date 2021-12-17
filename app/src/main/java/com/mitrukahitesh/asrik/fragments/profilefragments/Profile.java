package com.mitrukahitesh.asrik.fragments.profilefragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.activities.LocaleSelection;
import com.mitrukahitesh.asrik.activities.Main;
import com.mitrukahitesh.asrik.helpers.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    private TextView name, pincode, bloodGroup, city, phone, email;
    private FrameLayout root;
    private CircleImageView dp;
    private SwitchCompat notifyBloodCamps, coolDown;
    private RadioGroup languageGroup;
    private LinearLayout logout;

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
        languageGroup = view.findViewById(R.id.language_radio_group);
        notifyBloodCamps = view.findViewById(R.id.camp_notification);
        coolDown = view.findViewById(R.id.cooldown);
        logout = view.findViewById(R.id.logout_ll);
        notifyBloodCamps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(Constants.ENABLE_NOTIFICATION_BLOOD_CAMP, isChecked).apply();
            }
        });
        coolDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
                preferences.edit().putBoolean(Constants.COOL_DOWN, isChecked).apply();
            }
        });
        view.findViewById(R.id.card1).setBackgroundResource(R.drawable.dashed_border);
        view.findViewById(R.id.card2).setBackgroundResource(R.drawable.dashed_border);
        setValues(view);
    }

    private void setValues(View view) {
        SharedPreferences preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        name.setText(preferences.getString(Constants.NAME, ""));
        bloodGroup.setText(preferences.getString(Constants.BLOOD_GROUP, ""));
        city.setText(preferences.getString(Constants.CITY, ""));
        phone.setText(preferences.getString(Constants.NUMBER, ""));
        email.setText(preferences.getString(Constants.EMAIL, ""));
        pincode.setText(String.format("#%s", preferences.getString(Constants.PIN_CODE, "")));
        notifyBloodCamps.setChecked(preferences.getBoolean(Constants.ENABLE_NOTIFICATION_BLOOD_CAMP, true));
        coolDown.setChecked(preferences.getBoolean(Constants.COOL_DOWN, false));
        if (!preferences.getString(Constants.PROFILE_PIC_URL, "").equals(""))
            Glide.with(requireContext()).load(preferences.getString(Constants.PROFILE_PIC_URL, "")).into(dp);
        else
            Glide.with(requireContext()).load(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_usercircle_large)).centerCrop().into(dp);
        if (preferences.getString(Constants.LOCALE, "en").equals("en")) {
            languageGroup.check(view.findViewById(R.id.en).getId());
        } else {
            languageGroup.check(view.findViewById(R.id.hi).getId());
        }
        languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String locale = "en";
                if (checkedId == view.findViewById(R.id.hi).getId()) {
                    locale = "hi";
                }
                preferences.edit().putString(Constants.LOCALE, locale).apply();
                Main.userDefaultLanguage(requireActivity(), locale);
                Snackbar.make(root, requireContext().getString(R.string.restart), Snackbar.LENGTH_LONG).show();
            }
        });
        if (preferences.getBoolean(Constants.ADMIN, false)) {
            logout.setVisibility(View.VISIBLE);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                            .setTitle(requireContext().getString(R.string.are_you_sure))
                            .setMessage(requireContext().getString(R.string.logout_message))
                            .setNegativeButton(requireContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton(requireContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeAsAdminAndLogout();
                                }
                            })
                            .create();
                    dialog.show();
                }
            });
        }
    }

    private void removeAsAdminAndLogout() {
        FirebaseFirestore.getInstance()
                .collection(Constants.ADMINS)
                .document(requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.PIN_CODE, ""))
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).edit().clear().apply();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(requireContext(), LocaleSelection.class);
                            requireContext().startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), requireContext().getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}