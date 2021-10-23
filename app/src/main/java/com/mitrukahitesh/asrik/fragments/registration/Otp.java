package com.mitrukahitesh.asrik.fragments.registration;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.concurrent.TimeUnit;

public class Otp extends Fragment {

    private NavController controller;
    private LinearLayout ll;
    private TextView sec, resend;
    private String number;
    private Button submit;
    private TextInputEditText code;
    private String verId;
    private boolean verified = false;
    private boolean admin;

    public Otp() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            admin = getArguments().getBoolean(Constants.ADMIN, false);
            number = getArguments().getString(Constants.NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = Navigation.findNavController(view);
        ll = view.findViewById(R.id.ll);
        sec = view.findViewById(R.id.resend_val);
        resend = view.findViewById(R.id.resend_code);
        submit = view.findViewById(R.id.submit);
        code = view.findViewById(R.id.otp);
        submit.setOnClickListener(v -> {
            if (code.getText() == null)
                return;
            if (code.getText().toString().length() != 6) {
                return;
            }
            submit.setEnabled(false);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verId, code.getText().toString());
            signInWithPhoneAuthCredential(credential);
        });
        resend.setOnClickListener(v -> requestCode());
        requestCode();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Log.i("Asrik", "Complete");
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.i("Asrik", "Failed");
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.i("Asrik", "Sent");
            submit.setEnabled(true);
            verId = s;
            ll.setVisibility(View.VISIBLE);
            new Thread(() -> {
                int count = 60;
                while (count > 0 && !verified) {
                    int finalCount = count;
                    requireActivity().runOnUiThread(() -> sec.setText(String.format(" 00:%s", finalCount >= 10 ? finalCount + "" : "0" + finalCount)));
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    --count;
                }
                if (!verified) {
                    requireActivity().runOnUiThread(() -> {
                        ll.setVisibility(View.GONE);
                        resend.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        }
    };

    private void requestCode() {
        ll.setVisibility(View.GONE);
        resend.setVisibility(View.GONE);
        submit.setEnabled(false);
        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder()
                .setPhoneNumber(number)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        verified = true;
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.NUMBER, number);
                        bundle.putBoolean(Constants.ADMIN, admin);
                        controller.navigate(R.id.action_otp_to_userDetails, bundle);
                    } else {
                        submit.setEnabled(true);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.i("Asrik", "Incorrect OTP");
                        }
                    }
                });
    }
}