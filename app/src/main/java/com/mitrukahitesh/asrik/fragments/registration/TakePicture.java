package com.mitrukahitesh.asrik.fragments.registration;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.activities.Main;
import com.mitrukahitesh.asrik.models.BloodRequest;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;

public class TakePicture extends Fragment {

    private CircleImageView img;
    private Uri uri = null;
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
                    Task<Uri> task = new Task<Uri>() {
                        @Override
                        public boolean isComplete() {
                            return false;
                        }

                        @Override
                        public boolean isSuccessful() {
                            return true;
                        }

                        @Override
                        public boolean isCanceled() {
                            return false;
                        }

                        @Nullable
                        @Override
                        public Uri getResult() {
                            return null;
                        }

                        @Nullable
                        @Override
                        public <X extends Throwable> Uri getResult(@NonNull Class<X> aClass) throws X {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Exception getException() {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnSuccessListener(@NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super Uri> onSuccessListener) {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Task<Uri> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                            return null;
                        }
                    };
                    finishRegistering(task);
                    return;
                }
                try {
                    upload.setVisibility(View.GONE);
                    progressIndicator.setVisibility(View.VISIBLE);
                    progressIndicator.setProgress(0);
                    Snackbar.make(frameLayout, "Uploading...", Snackbar.LENGTH_LONG).show();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.PROFILE_PIC).child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                    storageReference.putFile(uri).addOnProgressListener(requireActivity(), new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            long progress = ((snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressIndicator.setProgress((int) progress, true);
                            } else {
                                progressIndicator.setProgress((int) progress);
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressIndicator.setProgress(100);
                            Snackbar.make(frameLayout, "Picture uploaded successfully..", Snackbar.LENGTH_SHORT).show();
                            completeRegistration();
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
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.PROFILE_PIC).child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                finishRegistering(task);
            }
        });
    }

    private void finishRegistering(Task<Uri> task) {
        if (task.isSuccessful()) {
            String url;
            if (task.getResult() == null)
                url = "";
            else
                url = task.getResult().toString();
            Map<String, Object> map = new HashMap<>();
            map.put(Constants.PROFILE_PIC_URL, url);
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.PROFILE_PIC_URL, url);
            editor.apply();
            FirebaseFirestore.getInstance()
                    .collection(Constants.USERS)
                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .update(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> t) {
                            Intent intent = new Intent(requireContext(), Main.class);
                            intent.putExtra(Constants.PROFILE_PIC_URL, url);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    });
        }
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