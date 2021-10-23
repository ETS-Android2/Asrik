package com.mitrukahitesh.asrik.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.fragments.rootfragments.Admin;
import com.mitrukahitesh.asrik.fragments.rootfragments.Chat;
import com.mitrukahitesh.asrik.fragments.rootfragments.Home;
import com.mitrukahitesh.asrik.fragments.rootfragments.NearbyServices;
import com.mitrukahitesh.asrik.fragments.rootfragments.Profile;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main extends AppCompatActivity {

    private final Fragment admin = new Admin();
    private final Fragment home = new Home();
    private final Fragment chat = new Chat();
    private final Fragment nearbyServices = new NearbyServices();
    private final Fragment profile = new Profile();
    private Fragment active = home;
    private BottomNavigationView navigationView;
    private final FragmentManager fragmentManager = getSupportFragmentManager();
    public static final String ADMIN = "admin";
    public static final String HOME = "home";
    public static final String NEARBY_SERVICES = "nearby_services";
    public static final String CHAT = "chat";
    public static final String PROFILE = "profile";
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isAdmin = getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, MODE_PRIVATE).getBoolean(Constants.ADMIN, false);
        navigationView = findViewById(R.id.bottomNavigationView);
        setBottomNavListener();
        updatePicUrlOnUserRequests();
        navigationView.setBackground(null);
        navigationView.setSelectedItemId(R.id.home);
        if (isAdmin) {
            navigationView.getMenu().getItem(0).setVisible(true);
            fragmentManager.beginTransaction().add(R.id.main_container, admin, ADMIN).hide(admin).commit();
        }
        fragmentManager.beginTransaction().add(R.id.main_container, home, HOME).show(home).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, profile, PROFILE).hide(profile).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, chat, CHAT).hide(chat).commit();
        fragmentManager.beginTransaction().add(R.id.main_container, nearbyServices, NEARBY_SERVICES).hide(nearbyServices).commit();
        active = home;
    }

    private void setBottomNavListener() {
        navigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.admin) {
                fragmentManager.beginTransaction().hide(active).show(admin).commit();
                active = admin;
                return true;
            } else if (item.getItemId() == R.id.home) {
                fragmentManager.beginTransaction().hide(active).show(home).commit();
                active = home;
                return true;
            } else if (item.getItemId() == R.id.nearbyServices) {
                fragmentManager.beginTransaction().hide(active).show(nearbyServices).commit();
                active = nearbyServices;
                return true;
            } else if (item.getItemId() == R.id.chat) {
                fragmentManager.beginTransaction().hide(active).show(chat).commit();
                active = chat;
                return true;
            } else if (item.getItemId() == R.id.profile) {
                fragmentManager.beginTransaction().hide(active).show(profile).commit();
                active = profile;
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getUid() == null)
            return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.i("Asrik: FCM", s);
                        Map<String, String> map = new HashMap<>();
                        map.put(Constants.TOKEN, s);
                        FirebaseFirestore.getInstance()
                                .collection(Constants.TOKENS)
                                .document(FirebaseAuth.getInstance().getUid())
                                .set(map);
                    }
                });
    }

    private void updatePicUrlOnUserRequests() {
        String url = getIntent().getStringExtra(Constants.PROFILE_PIC_URL);
        if (url == null)
            return;
        Map<String, Object> map = new HashMap<>();
        map.put("profilePicUrl", url);
        CollectionReference reference = FirebaseFirestore.getInstance().collection(Constants.REQUESTS);
        Query query = reference.
                whereEqualTo("uid", FirebaseAuth.getInstance().getUid());
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    reference.document((String) Objects.requireNonNull(snapshot.get("requestId"))).update(map);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Map<String, Boolean> map = new HashMap<>();
        map.put(Constants.STATUS, true);
        FirebaseFirestore.getInstance().collection(Constants.ONLINE).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).set(map);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Map<String, Boolean> map = new HashMap<>();
        map.put(Constants.STATUS, false);
        FirebaseFirestore.getInstance().collection(Constants.ONLINE).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).set(map);
    }

}