/*
    Fragment under Home tab
    Provides interface to raise a new blood request
 */

package com.mitrukahitesh.asrik.fragments.homefragments;

import static android.app.Activity.RESULT_OK;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.mitrukahitesh.asrik.models.blood.BloodRequest;
import com.mitrukahitesh.asrik.models.file.FileMetaData;
import com.mitrukahitesh.asrik.models.pin.PinCodeDetails;
import com.mitrukahitesh.asrik.helpers.Constants;
import com.mitrukahitesh.asrik.helpers.FileDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RaiseRequest extends Fragment {

    private NavController controller;
    private EditText pin, units, address;
    private Spinner blood_spinner, severity;
    private Uri uri;
    private ImageView img;
    private TextView name, cityProfile, submit, city, state, map, selectFile;
    public static final int MEDIA_SELECT = 1;
    private LinearProgressIndicator progressIndicator;
    private ScrollView root;
    private SharedPreferences preferences;
    private String bloodGroup = "", severityString = "";
    public static Double lat = null;
    public static Double lon = null;
    private int severityIndex;

    public RaiseRequest() {
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach and before onCreateView
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This will be called between onCreate and onViewCreated
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_raise_request, container, false);
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
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).popBackStack();
            }
        });
        setReferences(view);
        setListeners();
    }

    /**
     * Set reference to views
     */
    private void setReferences(View view) {
        root = view.findViewById(R.id.root);
        controller = Navigation.findNavController(view);
        progressIndicator = view.findViewById(R.id.progress);
        blood_spinner = view.findViewById(R.id.blood_group_spinner);
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.blood_groups));
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blood_spinner.setAdapter(arrayAdapter1);
        severity = view.findViewById(R.id.severity_spinner);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.severities));
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        severity.setAdapter(arrayAdapter2);
        progressIndicator.setVisibility(View.GONE);
        name = view.findViewById(R.id.name);
        cityProfile = view.findViewById(R.id.city_profile);
        pin = view.findViewById(R.id.pin);
        units = view.findViewById(R.id.units);
        address = view.findViewById(R.id.address);
        city = view.findViewById(R.id.city);
        state = view.findViewById(R.id.state);
        map = view.findViewById(R.id.choose_on_map);
        selectFile = view.findViewById(R.id.select_file);
        if (uri != null) {
            FileMetaData metaData = FileDetails.getFileMetaData(requireContext(), uri);
            selectFile.setText(metaData != null ? metaData.displayName : "file.pdf");
        }
        img = view.findViewById(R.id.dp);
        submit = view.findViewById(R.id.submit);
        name.setText(preferences.getString(Constants.NAME, ""));
        cityProfile.setText(preferences.getString(Constants.CITY, ""));
        Glide.with(requireContext()).load(preferences.getString(Constants.PROFILE_PIC_URL, "")).into(img);
    }

    /**
     * Set required listeners to views
     */
    private void setListeners() {
        blood_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        severity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    severityString = "";
                    return;
                }
                severityIndex = position;
                severityString = getResources().getStringArray(R.array.severities)[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        pin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                /*
                    Get Postal Code details
                 */
                if (editable.toString().length() == 6) {
                    submit.setEnabled(false);
                    RetrofitAccessObject.getRetrofitAccessObject().getPinCodeDetails(editable.toString()).enqueue(new Callback<PinCodeDetails>() {
                        @Override
                        public void onResponse(@NonNull Call<PinCodeDetails> call, @NonNull Response<PinCodeDetails> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                                submit.setEnabled(true);
                                city.setText(response.body().getCity());
                                state.setText(response.body().getState());
                            } else {
                                submit.setEnabled(false);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<PinCodeDetails> call, Throwable t) {
                            pinCodeValidationFailed();
                            submit.setEnabled(false);
                            city.setText(requireContext().getString(R.string.city));
                            state.setText(requireContext().getString(R.string.state));
                        }
                    });
                } else {
                    submit.setEnabled(false);
                    city.setText(requireContext().getString(R.string.city));
                    state.setText(requireContext().getString(R.string.state));
                }
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.navigate(R.id.action_raiseRequest_to_selectLocation, new Bundle());
            }
        });
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), MEDIA_SELECT);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    Validate input and then post request
                 */
                if (uri == null ||
                        bloodGroup.length() == 0 ||
                        severityString.length() == 0 ||
                        units.getText().toString().length() == 0 ||
                        address.getText().toString().length() == 0) {
                    Snackbar.make(root, "Please fill all details", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (lat == null || lon == null) {
                    Snackbar.make(root, "Please mark location on map to make navigation easy", Snackbar.LENGTH_LONG).show();
                    return;
                }
                try {
                    toggleViews(false);
                    progressIndicator.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressIndicator.setProgress(0, true);
                    } else {
                        progressIndicator.setProgress(0);
                    }
                    DocumentReference reference = FirebaseFirestore.getInstance().collection(Constants.REQUESTS).document();
                    BloodRequest request = new BloodRequest();
                    request.setUid(FirebaseAuth.getInstance().getUid());
                    request.setRequestId(reference.getId());
                    request.setBloodGroup(bloodGroup);
                    request.setSeverity(severityString);
                    request.setSeverityIndex(severityIndex);
                    request.setAddress(address.getText().toString());
                    request.setUnits(Integer.parseInt(units.getText().toString()));
                    request.setPincode(pin.getText().toString());
                    request.setCity(city.getText().toString());
                    request.setState(state.getText().toString());
                    request.setEmergency(false);
                    request.setVerified(false);
                    request.setLatitude(lat.toString());
                    request.setLongitude(lon.toString());
                    request.setTime(System.currentTimeMillis());
                    request.setName(preferences.getString(Constants.NAME, "User"));
                    request.setNameLowerCase(preferences.getString(Constants.NAME_LOWER_CASE_CAMEL, "User").toLowerCase(Locale.ROOT));
                    request.setNumber(preferences.getString(Constants.NUMBER, ""));
                    request.setProfilePicUrl(preferences.getString(Constants.PROFILE_PIC_URL, ""));
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.REQUESTS).child(reference.getId());
                    storageReference.putFile(uri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            long progress = ((snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount());
                            progressIndicator.setProgress((int) progress, true);
                            if (progress == 100) {
                                progressIndicator.setProgress(100);
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressIndicator.setProgress(100);
                            Snackbar.make(root, "File uploaded successfully..", Snackbar.LENGTH_SHORT).show();
                            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        request.setDocumentUrl(task.getResult().toString());
                                        reference.set(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                notifyAdmin();
                                            }
                                        });
                                    } else {
                                        Log.i("Asrik", task.getException() != null ? task.getException().getMessage() : "Error");
                                    }
                                }
                            });
                        }
                    });
                } catch (
                        Exception e) {
                    Log.i("Asrik", e.getMessage());
                }
            }
        });
    }

    /**
     * Request server to notify admin the blood request posted
     */
    private void notifyAdmin() {
        JSONObject object = new JSONObject();
        try {
            object.put(Constants.NAME, preferences.getString(Constants.NAME, ""));
            object.put(Constants.PROFILE_PIC_URL, preferences.getString(Constants.PROFILE_PIC_URL, ""));
            object.put(Constants.UNITS, units.getText().toString());
            object.put(Constants.BLOOD_GROUP, bloodGroup);
            object.put(Constants.SEVERITY, severityString);
        } catch (JSONException exception) {
            Log.i("Asrik: RaiseRequest", exception.getMessage());
        }
        RetrofitAccessObject.getRetrofitAccessObject()
                .notifyAdmin(pin.getText().toString(), object)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        Snackbar.make(root, "Request sent to admin for verification..", Snackbar.LENGTH_LONG).show();
                        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.feed, true).build();
                        controller.navigate(R.id.action_raiseRequest_to_feed, null, navOptions);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Snackbar.make(root, "Request sent to admin for verification..", Snackbar.LENGTH_LONG).show();
                        NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.feed, true).build();
                        controller.navigate(R.id.action_raiseRequest_to_feed, null, navOptions);
                    }
                });
    }

    /**
     * Message to display when wrong postal code is entered
     * Or there is network error
     * Or server is not responding
     */
    private void pinCodeValidationFailed() {
        Snackbar.make(root, "Invalid PIN Code", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Disable view while posting blood request
     */
    private void toggleViews(boolean b) {
        submit.setEnabled(b);
        pin.setEnabled(b);
        blood_spinner.setEnabled(b);
        severity.setEnabled(b);
        units.setEnabled(b);
        address.setEnabled(b);
        map.setEnabled(b);
        selectFile.setEnabled(b);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Check if location is selected
     * If yes, set text to "Edit Location", else "Select Location"
     */
    @Override
    public void onResume() {
        super.onResume();
        if (lat != null && lon != null) {
            map.setText(requireContext().getString(R.string.edit_location));
        }
    }

    /**
     * Called when user selects a file to send
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_SELECT) {
                if (data == null || data.getData() == null)
                    return;
                uri = data.getData();
                Log.i("Asrik", data.getData().toString());
                FileMetaData metaData = FileDetails.getFileMetaData(requireContext(), uri);
                selectFile.setText(metaData != null ? metaData.displayName : "file.pdf");
            }
        }
    }
}