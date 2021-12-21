/*
    It is the adapter that is used with recycler view
    which renders a list of pending requests
 */

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.mitrukahitesh.asrik.models.blood.BloodRequest;
import com.mitrukahitesh.asrik.helpers.Constants;

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

public class PendingRequests extends RecyclerView.Adapter<PendingRequests.CustomVH> {

    private Context context;
    private View root, noReq;
    private NavController controller;
    private final List<BloodRequest> requests = new ArrayList<>();
    /**
     * Stores time of the last blood request
     * Helps in achieving lazy loading
     */
    private Long last = 0l;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Stores at which number of blood request, more requests are fetched
     * Helps prevent redundant data fetch at same level
     * Say, user has viewed request no. 7, then this set will store 7 in it
     * So, if user again views the 7th request, app does not make request to more data
     */
    private final Set<Integer> updatedAt = new HashSet<>();
    /**
     * Contains UID of blood seeker whose online status listener is set
     * Prevents setting multiple listeners
     */
    private final Set<String> gotOnlineStatus = new HashSet<>();
    /**
     * Contains updated emergency status of requests
     */
    private final Map<String, Boolean> checkedAsEmergency = new HashMap<>();

    public PendingRequests(Context context, View root, NavController controller, View noReq) {
        this.context = context;
        this.root = root;
        this.controller = controller;
        this.noReq = noReq;
        fetchData();
    }

    /**
     * Fetch pending blood requests
     */
    private void fetchData() {
        CollectionReference reference = db.collection(Constants.REQUESTS);
        Query query = reference.
                whereEqualTo(Constants.PINCODE.toLowerCase(Locale.ROOT), context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.PIN_CODE, "")).
                whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), false).
                whereEqualTo(Constants.REJECTED.toLowerCase(Locale.ROOT), false).
                orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.ASCENDING).
                whereGreaterThan(Constants.TIME.toLowerCase(Locale.ROOT), last).
                limit(15);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ((SwipeRefreshLayout) root).setRefreshing(false);
                if (task.isSuccessful()) {
                    if (task.getResult() == null)
                        return;
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        requests.add(snapshot.toObject(BloodRequest.class));
                        notifyItemInserted(requests.size() - 1);
                    }
                    if (requests.size() > 0) {
                        noReq.setVisibility(View.GONE);
                        last = requests.get(requests.size() - 1).getTime();
                    } else {
                        noReq.setVisibility(View.VISIBLE);
                    }
                    Log.i("Asrik: Pending requests", "fetched " + task.getResult().size() + " " + last);
                } else {
                    Log.i("Asrik: Pending requests", "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
     */
    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_request, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the RecyclerView.ViewHolder.itemView
     * to reflect the item at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(requests.get(position), position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * Extend the abstract class RecyclerView.ViewHolder
     * to create ViewHolder objects and write custom
     * implementation
     */
    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout locate, share, chat, on, off, detailsHolder, emergency_ll, buttons;
        private final TextView title, name, units, address, severity;
        private final CircleImageView dp;
        private final ImageView verified;
        private final Button approve, reject;
        private final CheckBox emergency;

        /**
         * Call super constructor, and
         * Set references and listeners to views
         */
        public CustomVH(@NonNull View itemView) {
            super(itemView);
            detailsHolder = itemView.findViewById(R.id.details_holder);
            locate = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            chat = itemView.findViewById(R.id.chat);
            on = itemView.findViewById(R.id.online);
            off = itemView.findViewById(R.id.offline);
            emergency = itemView.findViewById(R.id.emergency);
            emergency_ll = itemView.findViewById(R.id.emergency_ll);
            buttons = itemView.findViewById(R.id.buttons);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            units = itemView.findViewById(R.id.units);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            dp = itemView.findViewById(R.id.dp);
            approve = itemView.findViewById(R.id.approve);
            reject = itemView.findViewById(R.id.reject);
            verified = itemView.findViewById(R.id.verified);
            setListeners();
        }

        /**
         * Set listeners to views in view holder
         */
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
                    controller.navigate(R.id.action_pendingRequests_to_chat4, bundle);
                }
            });
            emergency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkedAsEmergency.put(requests.get(getAdapterPosition()).getRequestId(), isChecked);
                }
            });
            approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.VERIFIED.toLowerCase(Locale.ROOT), true);
                    map.put(Constants.EMERGENCY.toLowerCase(Locale.ROOT), checkedAsEmergency.getOrDefault(requests.get(getAdapterPosition()).getRequestId(), false));
                    FirebaseFirestore.getInstance()
                            .collection(Constants.REQUESTS)
                            .document(requests.get(getAdapterPosition()).getRequestId())
                            .update(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        requests.get(getAdapterPosition()).setVerified(true);
                                        requests.get(getAdapterPosition()).setEmergency(checkedAsEmergency.getOrDefault(requests.get(getAdapterPosition()).getRequestId(), false));
                                        notifyVerification(requests.get(getAdapterPosition()));
                                        notifyItemChanged(getAdapterPosition());
                                    } else {
                                        if (root != null)
                                            Snackbar.make(root, "Approval failed", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.REJECTED.toLowerCase(Locale.ROOT), true);
                    FirebaseFirestore.getInstance()
                            .collection(Constants.REQUESTS)
                            .document(requests.get(getAdapterPosition()).getRequestId())
                            .update(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        BloodRequest request = requests.get(getAdapterPosition());
                                        notifyRejection(request);
                                        requests.remove(getAdapterPosition());
                                        notifyItemRemoved(getAdapterPosition());
                                    } else {
                                        if (root != null)
                                            Snackbar.make(root, "Approval failed", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            });
        }

        /**
         * Renders pending request data into view holder
         */
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
            if (request.isVerified()) {
                verified.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.GONE);
                emergency_ll.setVisibility(View.GONE);
            } else {
                verified.setVisibility(View.GONE);
                buttons.setVisibility(View.VISIBLE);
                emergency_ll.setVisibility(View.VISIBLE);
            }
            emergency.setChecked(checkedAsEmergency.getOrDefault(request.getRequestId(), false));
            title.setText(String.format("%s in %s", request.getBloodGroup(), request.getCity()));
            units.setText(String.format(Locale.getDefault(), "%d %s", request.getUnits(), context.getString(R.string.units)));
            name.setText(request.getName());
            address.setText(request.getAddress());
            severity.setText(context.getResources().getStringArray(R.array.severities)[request.getSeverityIndex()]);
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

    /**
     * Send request to server to notify the users about request rejection
     */
    private void notifyRejection(BloodRequest request) {
        JSONObject object = new JSONObject();
        try {
            SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
            object.put(Constants.ADMIN, preferences.getString(Constants.NAME, ""));
            object.put(Constants.UNITS, request.getUnits());
            object.put(Constants.BLOOD_GROUP, request.getBloodGroup());
            object.put(Constants.SEVERITY, request.getSeverity().toUpperCase(Locale.ROOT));
            object.put(Constants.PROFILE_PIC_URL, preferences.getString(Constants.PROFILE_PIC_URL, ""));
            RetrofitAccessObject.getRetrofitAccessObject()
                    .notifyRequestRejection(request.getUid(), object)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });
        } catch (JSONException exception) {
            Log.i("Asrik: Reject Request", exception.getMessage());
        }
    }

    /**
     * Send request to server to notify the users about request verification
     */
    private void notifyVerification(BloodRequest request) {
        JSONObject object = new JSONObject();
        try {
            SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE);
            object.put(Constants.REQUEST_ID, request.getRequestId());
            object.put(Constants.ADMIN, preferences.getString(Constants.NAME, ""));
            object.put(Constants.UNITS, request.getUnits());
            object.put(Constants.NAME, request.getName());
            object.put(Constants.EMERGENCY, request.isEmergency());
            object.put(Constants.BLOOD_GROUP, request.getBloodGroup());
            object.put(Constants.SEVERITY, request.getSeverity().toUpperCase(Locale.ROOT));
            object.put(Constants.PROFILE_PIC_URL, preferences.getString(Constants.PROFILE_PIC_URL, ""));
            RetrofitAccessObject.getRetrofitAccessObject()
                    .notifyUsersForRequestVerified(request.getPincode(), request.getUid(), object)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                        }
                    });
        } catch (JSONException exception) {
            Log.i("Asrik: Reject Request", exception.getMessage());
        }
    }
}
