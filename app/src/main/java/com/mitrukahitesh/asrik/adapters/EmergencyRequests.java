package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.apis.RetrofitAccessObject;
import com.mitrukahitesh.asrik.models.BloodRequest;
import com.mitrukahitesh.asrik.utility.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmergencyRequests extends RecyclerView.Adapter<EmergencyRequests.CustomVH> {

    private Context context;
    private final List<BloodRequest> requests = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Set<Integer> updatedAt = new HashSet<>();
    private final Set<String> gotOnlineStatus = new HashSet<>();
    private NavController controller;

    public EmergencyRequests(Context context, NavController controller) {
        this.context = context;
        this.controller = controller;
        fetchData();
    }

    private void fetchData() {
        CollectionReference reference = db.collection(Constants.REQUESTS);
        Query query = reference.
                whereEqualTo(Constants.PINCODE.toLowerCase(Locale.ROOT), context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.PIN_CODE, "")).
                whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                whereEqualTo(Constants.EMERGENCY.toLowerCase(Locale.ROOT), true).
                orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null)
                        return;
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        requests.add(snapshot.toObject(BloodRequest.class));
                        notifyItemInserted(requests.size() - 1);
                    }
                    Log.i("Asrik: Emergency requests", "fetched " + task.getResult().size());
                } else {
                    Log.i("Asrik: Emergency requests", "get failed with ", task.getException());
                }
            }
        });
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(requests.get(position), position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout locate, share, chat, on, off, detailsHolder, emergency_ll;
        private final TextView title, name, units, address, severity;
        private final CircleImageView dp;
        private final ImageView verified;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            detailsHolder = itemView.findViewById(R.id.details_holder);
            locate = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            chat = itemView.findViewById(R.id.chat);
            on = itemView.findViewById(R.id.online);
            off = itemView.findViewById(R.id.offline);
            emergency_ll = itemView.findViewById(R.id.emergency_icon_ll);
            emergency_ll.setVisibility(View.VISIBLE);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            units = itemView.findViewById(R.id.units);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            setListeners();
        }

        private void setListeners() {
            detailsHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(requests.get(getAdapterPosition()).getDocumentUrl()), "application/pdf");
                    context.startActivity(intent);
                }
            });
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BloodRequest request = requests.get(getAdapterPosition());
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String text = String.format(Locale.getDefault(), "Blood Requirement\n\nName: %s\nBlood Group: %s\nQuantity: %d unit(s)\nAddress: %s\nCity: %s\nState: %s\nPIN Code: %s\n\nContact: %s\n\nRegards,\n%s\nAsrik",
                            request.getName(),
                            request.getBloodGroup(),
                            request.getUnits(),
                            request.getAddress(),
                            request.getCity(),
                            request.getState(),
                            request.getPincode(),
                            request.getNumber(),
                            context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.NAME, "The Real Avenger"));
                    intent.putExtra(Intent.EXTRA_TEXT, text);
                    context.startActivity(intent);
                }
            });
            locate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("geo:" + requests.get(getAdapterPosition()).getLatitude() + ", " + requests.get(getAdapterPosition()).getLongitude() + "?q=" + requests.get(getAdapterPosition()).getAddress());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                }
            });
            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BloodRequest request = requests.get(getAdapterPosition());
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.PROFILE_PIC_URL, request.getProfilePicUrl());
                    bundle.putString(Constants.NAME, request.getName());
                    bundle.putString(Constants.UID, request.getUid());
                    bundle.putString(Constants.NUMBER, request.getNumber());
                    controller.navigate(R.id.action_feed_to_chat3, bundle);
                }
            });
        }

        public void setView(BloodRequest request, int position) {
            if ((position + 1) % 15 == 0 && !updatedAt.contains(position)) {
                fetchData();
                updatedAt.add(position);
            }
            if (request.getProfilePicUrl() == null || request.getProfilePicUrl().equals("")) {
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.ic_usercircle)).into(dp);
            } else {
                Glide.with(context).load(request.getProfilePicUrl()).into(dp);
            }
            title.setText(String.format("%s in %s", request.getBloodGroup(), request.getCity()));
            units.setText(String.format(Locale.getDefault(), "%d units", request.getUnits()));
            name.setText(request.getName());
            address.setText(request.getAddress());
            severity.setText(request.getSeverity());
            if (request.isUserOnline()) {
                on.setVisibility(View.VISIBLE);
                off.setVisibility(View.GONE);
            } else {
                on.setVisibility(View.GONE);
                off.setVisibility(View.VISIBLE);
            }
            if (gotOnlineStatus.contains(request.getRequestId()))
                return;
            FirebaseFirestore.getInstance()
                    .collection(Constants.ONLINE)
                    .document(request.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().contains(Constants.STATUS)) {
                                request.setUserOnline((Boolean) task.getResult().get(Constants.STATUS));
                                notifyItemChanged(position);
                            }
                        }
                    });
            gotOnlineStatus.add(request.getRequestId());
        }
    }
}
