package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.blood.BloodRequest;
import com.mitrukahitesh.asrik.helpers.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedRequests extends RecyclerView.Adapter<FeedRequests.CustomVH> {

    private Context context;
    private NavController controller;
    private final List<BloodRequest> requests = new ArrayList<>();
    private Long last;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection(Constants.REQUESTS);
    private final Map<String, Query> queryMap = new HashMap<>();
    private String queryName = "", searchName = "";
    private final Set<Integer> updatedAt = new HashSet<>();
    private final Set<String> gotOnlineStatus = new HashSet<>();

    public FeedRequests(Context context, NavController controller, String queryName, String searchName) {
        this.context = context;
        this.controller = controller;
        this.queryName = queryName;
        this.searchName = searchName;
        if (!queryName.equals(Constants.SEVERITY_HIGH_LOW) && !queryName.equals(Constants.SEVERITY_LOW_HIGH)) {
            if (queryName.equals(Constants.OLDEST_FIRST))
                last = 0L;
            else
                last = System.currentTimeMillis();
        } else {
            if (queryName.equals(Constants.SEVERITY_HIGH_LOW)) {
                last = 2L;
            } else {
                last = 3L;
            }
        }
        setMap(searchName);
        fetchData();
    }

    private void setMap(String name) {
        if (name.equals("")) {
            queryMap.put(
                    Constants.RELEVANCE,
                    reference.
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
            );
            queryMap.put(
                    Constants.NEWEST_FIRST,
                    reference.
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
            );
            queryMap.put(
                    Constants.OLDEST_FIRST,
                    reference.
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.ASCENDING)
            );
            queryMap.put(
                    Constants.SEVERITY_HIGH_LOW,
                    reference.
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.SEVERITY_INDEX, Query.Direction.ASCENDING)
            );
            queryMap.put(
                    Constants.SEVERITY_LOW_HIGH,
                    reference.
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.SEVERITY_INDEX, Query.Direction.DESCENDING)
            );
        } else {
            queryMap.put(
                    Constants.RELEVANCE,
                    reference.
                            whereEqualTo(Constants.NAME_LOWER_CASE_CAMEL, name).
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
            );
            queryMap.put(
                    Constants.NEWEST_FIRST,
                    reference.
                            whereEqualTo(Constants.NAME_LOWER_CASE_CAMEL, name).
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING)
            );
            queryMap.put(
                    Constants.OLDEST_FIRST,
                    reference.
                            whereEqualTo(Constants.NAME_LOWER_CASE_CAMEL, name).
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.ASCENDING)
            );
            queryMap.put(
                    Constants.SEVERITY_HIGH_LOW,
                    reference.
                            whereEqualTo(Constants.NAME_LOWER_CASE_CAMEL, name).
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.SEVERITY_INDEX, Query.Direction.ASCENDING)
            );
            queryMap.put(
                    Constants.SEVERITY_LOW_HIGH,
                    reference.
                            whereEqualTo(Constants.NAME_LOWER_CASE_CAMEL, name).
                            whereEqualTo(Constants.CANCELLED.toLowerCase(Locale.ROOT), false).
                            whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                            orderBy(Constants.SEVERITY_INDEX, Query.Direction.DESCENDING)
            );
        }
    }

    private void fetchData() {
        Query query = queryMap.get(queryName);
        if (query == null)
            return;
        if (queryName.equals(Constants.OLDEST_FIRST)) {
            query = query.
                    whereGreaterThan(Constants.TIME.toLowerCase(Locale.ROOT), last).
                    limit(10);
        } else if (!queryName.equals(Constants.SEVERITY_HIGH_LOW) && !queryName.equals(Constants.SEVERITY_LOW_HIGH)) {
            query = query.
                    whereLessThan(Constants.TIME.toLowerCase(Locale.ROOT), last).
                    limit(10);
        } else {
            if (queryName.equals(Constants.SEVERITY_HIGH_LOW)) {
                if (last > 5)
                    return;
                query = query.
                        startAt(last - 1).
                        endBefore(last);
            } else {
                if (last < 0)
                    return;
                query = query
                        .startAt(last + 1)
                        .endBefore(last);
            }
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                String pincode = context.getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, Context.MODE_PRIVATE).getString(Constants.PIN_CODE, "");
                if (task.isSuccessful()) {
                    if (task.getResult() == null)
                        return;
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        BloodRequest request = snapshot.toObject(BloodRequest.class);
                        if (request.isEmergency() && request.getPincode().equals(pincode) && queryName.equals(Constants.RELEVANCE) && searchName.equals(""))
                            continue;
                        requests.add(request);
                        notifyItemInserted(requests.size() - 1);
                    }
                    if (requests.size() > 0 && !queryName.equals(Constants.SEVERITY_HIGH_LOW) && !queryName.equals(Constants.SEVERITY_LOW_HIGH))
                        last = requests.get(requests.size() - 1).getTime();
                    else {
                        if (queryName.equals(Constants.SEVERITY_HIGH_LOW)) {
                            last++;
                        } else {
                            last--;
                        }
                        if (task.getResult().size() == 0)
                            fetchData();
                    }
                    Log.i("Asrik", "fetched " + task.getResult().size() + " " + last);
                } else {
                    Log.i("Asrik", "get failed with ", task.getException());
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

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            detailsHolder = itemView.findViewById(R.id.details_holder);
            emergency_ll = itemView.findViewById(R.id.emergency_icon_ll);
            locate = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            chat = itemView.findViewById(R.id.chat);
            on = itemView.findViewById(R.id.online);
            off = itemView.findViewById(R.id.offline);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            units = itemView.findViewById(R.id.units);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            dp = itemView.findViewById(R.id.dp);
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
            if (!queryName.equals(Constants.SEVERITY_HIGH_LOW) && !queryName.equals(Constants.SEVERITY_LOW_HIGH) && (position + 1) % 7 == 0 && !updatedAt.contains(position)) {
                fetchData();
                updatedAt.add(position);
            } else if (position + 1 == requests.size() && !updatedAt.contains(position)) {
                fetchData();
                updatedAt.add(position);
            }
            if (request.getProfilePicUrl() == null || request.getProfilePicUrl().equals("")) {
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.ic_usercircle)).into(dp);
            } else {
                Glide.with(context).load(request.getProfilePicUrl()).into(dp);
            }
            if (request.isEmergency()) {
                emergency_ll.setVisibility(View.VISIBLE);
            } else {
                emergency_ll.setVisibility(View.GONE);
            }
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
}
