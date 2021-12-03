package com.mitrukahitesh.asrik.fragments.common;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.fragments.adminfragments.BloodCamp;
import com.mitrukahitesh.asrik.fragments.homefragments.RaiseRequest;
import com.mitrukahitesh.asrik.helpers.Constants;

public class SelectLocation extends Fragment {

    private GoogleMap googleMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker marker;
    private final LatLng delhi = new LatLng(28.7041, 77.1025);
    private Button confirmLocation;
    private LatLng current = null;
    private boolean camp;
    private final GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            requestToTurnGpsOn();
            return false;
        }
    };

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            SelectLocation.this.googleMap = googleMap;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(delhi, 5);
            googleMap.animateCamera(cameraUpdate);
            checkLocationPermission();
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    if (marker != null)
                        marker.remove();
                    marker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                    current = latLng;
                    confirmLocation.setVisibility(View.VISIBLE);
                }
            });
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(@NonNull Marker marker) {

                }

                @Override
                public void onMarkerDrag(@NonNull Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(@NonNull Marker marker) {
                    current = marker.getPosition();
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).popBackStack();
            }
        });
        confirmLocation = view.findViewById(R.id.confirm);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        confirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putDouble(Constants.LATITUDE, current.latitude);
                bundle.putDouble(Constants.LONGITUDE, current.longitude);
                if (camp) {
                    BloodCamp.lat = current.latitude;
                    BloodCamp.lon = current.longitude;
                } else {
                    RaiseRequest.lat = current.latitude;
                    RaiseRequest.lon = current.longitude;
                }
                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestToTurnGpsOn();
            getCurrentLocation();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                googleMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

        }, null);
    }

    private void requestToTurnGpsOn() {
        if (((LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER))
            return;
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Location Service")
                .setMessage("Please turn on GPS for better experience.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent gpsOptionsIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            camp = getArguments().getBoolean(Constants.BLOOD_CAMP, false);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PERMISSION_GRANTED) {
            requestToTurnGpsOn();
            getCurrentLocation();
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        }
    }

}