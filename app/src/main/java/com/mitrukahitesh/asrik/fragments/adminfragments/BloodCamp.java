package com.mitrukahitesh.asrik.fragments.adminfragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.apis.RetrofitAccessObject;
import com.mitrukahitesh.asrik.models.BloodRequest;
import com.mitrukahitesh.asrik.models.FileMetaData;
import com.mitrukahitesh.asrik.models.PinCodeDetails;
import com.mitrukahitesh.asrik.utility.Constants;
import com.mitrukahitesh.asrik.utility.FileDetails;
import com.mitrukahitesh.asrik.utility.TimeFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BloodCamp extends Fragment implements DatePickerDialog.OnDateSetListener {

    private NavController controller;
    private EditText address;
    private ImageView img;
    private TextView name, cityProfile, submit, map, date, startTime, endTime;
    private ScrollView root;
    private SharedPreferences preferences;
    private CircularProgressIndicator progressIndicator;
    public static Double lat = null;
    public static Double lon = null;
    private Calendar cal = null;
    private int startHour = -1, startMin = -1, endhour = -1, endMin = -1;

    public BloodCamp() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blood_camp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).popBackStack();
            }
        });
        setReferences(view);
        setValues();
        setListeners();
    }

    private void setReferences(View view) {
        root = view.findViewById(R.id.root);
        controller = Navigation.findNavController(view);
        name = view.findViewById(R.id.name);
        cityProfile = view.findViewById(R.id.city_profile);
        address = view.findViewById(R.id.address);
        map = view.findViewById(R.id.choose_on_map);
        img = view.findViewById(R.id.dp);
        submit = view.findViewById(R.id.submit);
        date = view.findViewById(R.id.date);
        startTime = view.findViewById(R.id.start_time);
        endTime = view.findViewById(R.id.end_time);
        progressIndicator = view.findViewById(R.id.progress);
    }

    private void setValues() {
        name.setText(preferences.getString(Constants.NAME, ""));
        cityProfile.setText(String.format("%s, %s", preferences.getString(Constants.CITY, ""), preferences.getString(Constants.PIN_CODE, "")));
        Glide.with(requireContext()).load(preferences.getString(Constants.PROFILE_PIC_URL, "")).into(img);
    }

    private void setListeners() {
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(requireContext(), android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, dayOfMonth);
                        cal = calendar;
                        date.setText(String.format(Locale.getDefault(), "%d %s %d", dayOfMonth, calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), year));
                    }
                }, year, month, day);
                dialog.show();
            }
        });
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startHour = hourOfDay;
                        startMin = minute;
                        startTime.setText(TimeFormatter.formatTime(hourOfDay, minute));
                    }
                }, hour, minute, true);
                dialog.show();
            }
        });
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endhour = hourOfDay;
                        endMin = minute;
                        endTime.setText(TimeFormatter.formatTime(hourOfDay, minute));
                    }
                }, hour, minute, true);
                dialog.show();
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.BLOOD_CAMP, true);
                controller.navigate(R.id.action_bloodCamp_to_selectLocation2, bundle);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postUpdate();
            }
        });
    }

    private void postUpdate() {
        if (cal == null) {
            Snackbar.make(root, "Please select date", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (address.getText().toString().equals("")) {
            Snackbar.make(root, "Please fill address", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (lat == null || lon == null) {
            Snackbar.make(root, "Please mark location", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (startHour == -1 || startMin == -1) {
            Snackbar.make(root, "Please select start time", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (endhour == -1 || endMin == -1) {
            Snackbar.make(root, "Please select end time", Snackbar.LENGTH_SHORT).show();
            return;
        }
        toggleViews(false);
        com.mitrukahitesh.asrik.models.BloodCamp camp = new com.mitrukahitesh.asrik.models.BloodCamp();
        camp.setTime(System.currentTimeMillis());
        camp.setAddress(address.getText().toString());
        camp.setLat(lat.toString());
        camp.setLon(lon.toString());
        camp.setStartHour(startHour);
        camp.setStartMin(startMin);
        camp.setEndhour(endhour);
        camp.setEndMin(endMin);
        camp.setYear(cal.get(Calendar.YEAR));
        camp.setMonth(cal.get(Calendar.MONTH));
        camp.setDay(cal.get(Calendar.DAY_OF_MONTH));
        camp.setAdminId(FirebaseAuth.getInstance().getUid());
        camp.setAdminName(preferences.getString(Constants.NAME, ""));
        camp.setPincode(preferences.getString(Constants.PIN_CODE, ""));
        camp.setCity(preferences.getString(Constants.CITY, ""));
        camp.setState(preferences.getString(Constants.STATE, ""));
        FirebaseFirestore.getInstance()
                .collection(Constants.CAMPS)
                .document()
                .set(camp)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            notifyAdmin();
                        } else {
                            Snackbar.make(root, "Please try again", Snackbar.LENGTH_LONG).show();
                            toggleViews(true);
                        }
                    }
                });
    }

    private void notifyAdmin() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.ADDRESS, address.getText().toString());
            jsonObject.put(Constants.DATE, String.format(Locale.getDefault(), "%d %s %d", cal.get(Calendar.DAY_OF_MONTH), cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()), cal.get(Calendar.YEAR)));
            jsonObject.put(Constants.START_TIME, TimeFormatter.formatTime(startHour, startMin));
            jsonObject.put(Constants.END_TIME, TimeFormatter.formatTime(endhour, endMin));
            RetrofitAccessObject.getRetrofitAccessObject()
                    .notifyBloodCamp(preferences.getString(Constants.PIN_CODE, ""), FirebaseAuth.getInstance().getUid(), jsonObject)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            navigateToPendingRequests(true);
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.i("Asrik: BloodCamp", t.getMessage());
                            navigateToPendingRequests(false);
                        }
                    });
        } catch (JSONException exception) {
            Log.i("Asrik: BloodCamp", exception.getMessage());
            navigateToPendingRequests(false);
        }
    }

    private void navigateToPendingRequests(boolean b) {
        Snackbar.make(root, b ? "Notified donors" : "Blood camp posted", Snackbar.LENGTH_SHORT).show();
        NavOptions options = new NavOptions.Builder().setPopUpTo(R.id.pendingRequests, true).build();
        controller.navigate(R.id.action_bloodCamp_to_pendingRequests, new Bundle(), options);
    }

    private void toggleViews(boolean b) {
        submit.setEnabled(b);
        address.setEnabled(b);
        map.setEnabled(b);
        date.setEnabled(b);
        endTime.setEnabled(b);
        startTime.setEnabled(b);
        progressIndicator.setVisibility(b ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lat != null && lon != null) {
            map.setText("Edit\nLocation");
        }
        if (cal != null) {
            date.setText(String.format(Locale.getDefault(), "%d %s %d", cal.get(Calendar.DAY_OF_MONTH), cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()), cal.get(Calendar.YEAR)));
        }
        if (startHour != -1 && startMin != -1) {
            startTime.setText(String.format(Locale.getDefault(), "%d:%d", startHour, startMin));
        }
        if (endhour != -1 && endMin != -1) {
            endTime.setText(String.format(Locale.getDefault(), "%d:%d", endhour, endMin));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }
}