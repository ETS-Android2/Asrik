package com.mitrukahitesh.asrik.fragments.registration;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.activities.Main;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class TakePicture extends Fragment {

    private CircleImageView img;
    private Uri uri;
    public static final int MEDIA_SELECT = 1;
    private FrameLayout frameLayout;
    private LinearProgressIndicator progressIndicator;

    public TakePicture() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_take_picture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img = view.findViewById(R.id.img);
        Button upload = view.findViewById(R.id.upload);
        frameLayout = view.findViewById(R.id.root);
        progressIndicator = view.findViewById(R.id.progress);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), MEDIA_SELECT);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri == null) {
                    completeRegistration();
                }
                try {
                    upload.setVisibility(View.GONE);
                    progressIndicator.setVisibility(View.VISIBLE);
                    progressIndicator.setProgress(0);
                    Snackbar.make(frameLayout, "Uploading...", Snackbar.LENGTH_LONG).show();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_pic").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                    storageReference.putFile(uri).addOnProgressListener(requireActivity(), new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            long progress = ((snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressIndicator.setProgress((int) progress, true);
                            } else {
                                progressIndicator.setProgress((int) progress);
                            }
                            if (progress == 100) {
                                try {
                                    completeRegistration();
                                } catch (Exception e) {
                                    Snackbar.make(frameLayout, "Please try again", Snackbar.LENGTH_SHORT).show();
                                    upload.setVisibility(View.VISIBLE);
                                    progressIndicator.setVisibility(View.GONE);
                                    Log.i("Asrik", e.getMessage());
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Snackbar.make(frameLayout, "Please try again", Snackbar.LENGTH_SHORT).show();
                    upload.setVisibility(View.VISIBLE);
                    progressIndicator.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        });
    }

    private void completeRegistration() {
        Intent intent = new Intent(requireContext(), Main.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_SELECT) {
                if (data == null || data.getData() == null)
                    return;
                uri = data.getData();
                Log.i("Asrik", data.getData().toString());
                img.setImageURI(uri);
                Glide.with(TakePicture.this).load(uri).fitCenter().into(img);
            }
        }
    }
}