/*
    It is the adapter that is used with recycler view
    which renders a list of blood requests that a
    user has made i.e. request history
 */

package com.mitrukahitesh.asrik.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.blood.BloodRequest;
import com.mitrukahitesh.asrik.helpers.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestHistory extends RecyclerView.Adapter<RequestHistory.CustomVH> {

    private Context context;
    private List<BloodRequest> requestList = new ArrayList<>();

    public RequestHistory(Context context, List<BloodRequest> requestList) {
        this.context = context;
        this.requestList = requestList;
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
        holder.setView(requestList.get(position), position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * Extend the abstract class RecyclerView.ViewHolder
     * to create ViewHolder objects and write custom
     * implementation
     */
    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout locate;
        private final LinearLayout share;
        private final LinearLayout detailsHolder;
        private final LinearLayout emergency_ll;
        private final LinearLayout buttons;
        private final TextView title, name, units, address, severity;
        private final CircleImageView dp;
        private final Button revoke, cooldown;
        private final TextView note;
        private final ImageView verified;

        /**
         * Call super constructor, and
         * Set references and listeners to views
         */
        public CustomVH(@NonNull View itemView) {
            super(itemView);
            detailsHolder = itemView.findViewById(R.id.details_holder);
            emergency_ll = itemView.findViewById(R.id.emergency_icon_ll);
            locate = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            LinearLayout chat = itemView.findViewById(R.id.chat);
            chat.setVisibility(View.GONE);
            LinearLayout on = itemView.findViewById(R.id.online);
            on.setVisibility(View.VISIBLE);
            LinearLayout off = itemView.findViewById(R.id.offline);
            off.setVisibility(View.GONE);
            buttons = itemView.findViewById(R.id.buttons_history);
            buttons.setVisibility(View.VISIBLE);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            units = itemView.findViewById(R.id.units);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            dp = itemView.findViewById(R.id.dp);
            revoke = itemView.findViewById(R.id.revoke);
            cooldown = itemView.findViewById(R.id.cooldown);
            note = itemView.findViewById(R.id.note);
            verified = itemView.findViewById(R.id.verified);
            setListeners();
        }

        /**
         * Set listeners to views in view holder
         */
        private void setListeners() {
            detailsHolder.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(requestList.get(getAdapterPosition()).getDocumentUrl()), "application/pdf");
                context.startActivity(intent);
            });
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BloodRequest request = requestList.get(getAdapterPosition());
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
                    Uri gmmIntentUri = Uri.parse("geo:" + requestList.get(getAdapterPosition()).getLatitude() + ", " + requestList.get(getAdapterPosition()).getLongitude() + "?q=" + requestList.get(getAdapterPosition()).getAddress());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);
                }
            });
            cooldown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setTitle("Cooldown")
                            .setMessage("Are you sure you want to cooldown your request?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BloodRequest request = requestList.get(getAdapterPosition());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(Constants.EMERGENCY.toLowerCase(Locale.ROOT), false);
                                    FirebaseFirestore.getInstance()
                                            .collection(Constants.REQUESTS)
                                            .document(request.getRequestId())
                                            .update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    request.setEmergency(false);
                                                    notifyItemChanged(getAdapterPosition());
                                                }
                                            });
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create();
                    dialog.show();
                }
            });
            revoke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setCancelable(false)
                            .setTitle("Revoke")
                            .setMessage("Are you sure you want to revoke your request?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BloodRequest request = requestList.get(getAdapterPosition());
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(Constants.EMERGENCY.toLowerCase(Locale.ROOT), false);
                                    map.put(Constants.VERIFIED.toLowerCase(Locale.ROOT), false);
                                    map.put(Constants.CANCELLED.toLowerCase(Locale.ROOT), true);
                                    FirebaseFirestore.getInstance()
                                            .collection(Constants.REQUESTS)
                                            .document(request.getRequestId())
                                            .update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    request.setEmergency(false);
                                                    request.setVerified(false);
                                                    request.setCancelled(true);
                                                    notifyItemChanged(getAdapterPosition());
                                                }
                                            });
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create();
                    dialog.show();
                }
            });
        }

        /**
         * Renders request data into view holder
         */
        public void setView(BloodRequest request, int position) {
            if (request.getProfilePicUrl() == null || request.getProfilePicUrl().equals("")) {
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.ic_usercircle)).into(dp);
            } else {
                Glide.with(context).load(request.getProfilePicUrl()).into(dp);
            }
            if (request.isVerified()) {
                verified.setVisibility(View.VISIBLE);
            } else {
                verified.setVisibility(View.GONE);
            }
            if (request.isEmergency()) {
                emergency_ll.setVisibility(View.VISIBLE);
                cooldown.setEnabled(true);
                cooldown.setBackgroundColor(context.getColor(R.color.green1));
            } else {
                emergency_ll.setVisibility(View.GONE);
                cooldown.setEnabled(false);
                cooldown.setBackgroundColor(context.getColor(R.color.grey1));
            }
            if (request.isCancelled()) {
                buttons.setVisibility(View.GONE);
                note.setVisibility(View.VISIBLE);
                note.setText(context.getString(R.string.req_revoked));
            } else if (request.isRejected()) {
                buttons.setVisibility(View.GONE);
                note.setVisibility(View.VISIBLE);
                note.setText(context.getString(R.string.req_rejected));
            } else {
                buttons.setVisibility(View.VISIBLE);
                note.setVisibility(View.GONE);
            }
            title.setText(String.format("%s in %s", request.getBloodGroup(), request.getCity()));
            units.setText(String.format(Locale.getDefault(), "%d %s", request.getUnits(), context.getString(R.string.units)));
            name.setText(request.getName());
            address.setText(request.getAddress());
            severity.setText(context.getResources().getStringArray(R.array.severities)[request.getSeverityIndex()]);
        }
    }
}
