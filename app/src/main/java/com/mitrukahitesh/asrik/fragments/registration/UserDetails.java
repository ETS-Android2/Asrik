package com.mitrukahitesh.asrik.fragments.registration;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.apis.RetrofitAccessObject;
import com.mitrukahitesh.asrik.models.PinCodeDetails;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserDetails extends Fragment {

    private TextInputEditText name, email, pin;
    private TextView city, state;
    private Bundle bundle;
    private NavController controller;
    private FrameLayout frameLayout;
    private Spinner spinner;
    private String bloodGroup;
    private SharedPreferences.Editor editor;
    private final View.OnClickListener onSubmit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (name.getText() == null || name.getText().toString().equals("")) {
                name.requestFocus();
                return;
            }
            if (email.getText() == null || email.getText().toString().equals("")) {
                email.requestFocus();
                return;
            }
            if (pin.getText() == null || pin.getText().toString().length() != 6) {
                pin.requestFocus();
                return;
            }
            if (bloodGroup == null || bloodGroup.equals("")) {
                Snackbar.make(frameLayout, "Please select blood group", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (!bundle.getBoolean(Constants.ADMIN, false)) {
                saveData();
            } else {
                checkExistingAdminAndAddData();
            }
        }
    };

    private void checkExistingAdminAndAddData() {
        FirebaseFirestore.getInstance()
                .collection(Constants.ADMINS)
                .document(Objects.requireNonNull(pin.getText()).toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && (task.getResult() == null || task.getResult().exists())) {
                        if (task.getResult() != null && task.getResult().exists() && Objects.equals(task.getResult().get(Constants.ADMIN), FirebaseAuth.getInstance().getUid())) {
                            setAdmin();
                            return;
                        }
                        Snackbar.make(frameLayout, "Admin for this area already exists..", Snackbar.LENGTH_LONG).show();
                    } else {
                        setAdmin();
                    }
                });
    }

    private void setAdmin() {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.ADMIN, FirebaseAuth.getInstance().getUid());
        FirebaseFirestore.getInstance()
                .collection(Constants.ADMINS)
                .document(Objects.requireNonNull(pin.getText()).toString())
                .set(map)
                .addOnCompleteListener(task -> saveData());
    }

    private void saveData() {
        Map<String, Object> details = new HashMap<>();
        details.put(Constants.ADMIN, bundle.getBoolean(Constants.ADMIN, false));
        details.put(Constants.NUMBER, bundle.getString(Constants.NUMBER));
        details.put(Constants.NAME, Objects.requireNonNull(name.getText()).toString());
        details.put(Constants.EMAIL, Objects.requireNonNull(email.getText()).toString());
        details.put(Constants.PIN_CODE, Objects.requireNonNull(pin.getText()).toString());
        details.put(Constants.BLOOD_GROUP, bloodGroup);
        details.put(Constants.CITY, city.getText().toString());
        details.put(Constants.STATE, state.getText().toString());
        editor.putBoolean(Constants.ADMIN, bundle.getBoolean(Constants.ADMIN, false));
        editor.putString(Constants.NUMBER, bundle.getString(Constants.NUMBER));
        editor.putString(Constants.NAME, Objects.requireNonNull(name.getText()).toString());
        editor.putString(Constants.EMAIL, Objects.requireNonNull(email.getText()).toString());
        editor.putString(Constants.PIN_CODE, Objects.requireNonNull(pin.getText()).toString());
        editor.putString(Constants.BLOOD_GROUP, bloodGroup);
        editor.putString(Constants.CITY, city.getText().toString());
        editor.putString(Constants.STATE, state.getText().toString());
        editor.apply();
        FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .set(details)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        controller.navigate(R.id.action_userDetails_to_takePicture);
                    } else {
                        Snackbar.make(frameLayout, "Please try again", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public UserDetails() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (getArguments() != null) {
            bundle = getArguments();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(view);
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        pin = view.findViewById(R.id.pin);
        city = view.findViewById(R.id.city);
        state = view.findViewById(R.id.state);
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.blood_groups));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    bloodGroup = "";
                    return;
                }
                bloodGroup = getResources().getStringArray(R.array.blood_groups)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button button = view.findViewById(R.id.continue_btn);
        frameLayout = view.findViewById(R.id.root);
        pin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 6) {
                    button.setEnabled(false);
                    RetrofitAccessObject.getRetrofitAccessObject().getPinCodeDetails(editable.toString()).enqueue(new Callback<PinCodeDetails>() {
                        @Override
                        public void onResponse(@NonNull Call<PinCodeDetails> call, @NonNull Response<PinCodeDetails> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                                button.setEnabled(true);
                                city.setText(response.body().getCity());
                                state.setText(response.body().getState());
                            } else {
                                button.setEnabled(false);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<PinCodeDetails> call, Throwable t) {
                            pinCodeValidationFailed();
                            button.setEnabled(false);
                            city.setText(requireContext().getString(R.string.city));
                            state.setText(requireContext().getString(R.string.state));
                        }
                    });
                } else {
                    button.setEnabled(false);
                    city.setText(requireContext().getString(R.string.city));
                    state.setText(requireContext().getString(R.string.state));
                }
            }
        });
        button.setOnClickListener(onSubmit);
    }

    private void pinCodeValidationFailed() {
        Snackbar.make(frameLayout, "Invalid PIN Code", Snackbar.LENGTH_SHORT).show();
    }
}