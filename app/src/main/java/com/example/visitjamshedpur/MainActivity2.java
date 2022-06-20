package com.example.visitjamshedpur;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity2 extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    String ID;
    ArrayList<String> imageList = new ArrayList<>();
    String title, address;
    View leftBtn, rightBtn;
    LinearLayout imageCounterView;
    TextView counter;
    ProgressBar progressBar;
    private ImageView imageView;
    private TextView textTitle, textAddress;
    private ScrollView mScrollView;
    private GoogleMap map;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private final OnMapReadyCallback callBack = googleMap -> {
        map = googleMap;
        updateLocationUI();
        geoLocate();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(22.80476539961016, 86.2028412415867), 10));


    };
    private CardView reviewBtn;
    private Toolbar toolbar;
    private CardView navigate;
    private CardView googleMap;

    private void geoLocate() {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder
                    .getFromLocationName(title + ", Jamshedpur, Jharkhand, India", 2);
            for (Address address : addresses) {
                LatLng temp = new LatLng(address.getLatitude(), address.getLongitude());
                Objects.requireNonNull(map.addMarker(new MarkerOptions().position(temp).title(title))).showInfoWindow();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 18));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_maps);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(callBack);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        WorkaroundMapFragment mMap = ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_maps));
        mScrollView = findViewById(R.id.scrollView);
        ((WorkaroundMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.google_maps))).setListener(() -> mScrollView.requestDisallowInterceptTouchEvent(true));

        initializations();
        reviewIntent();
        toolbarOptions();
        getData();
    }

    private void toolbarOptions() {
        toolbar.setNavigationIcon(R.drawable.back_icon_white);
        toolbar.setNavigationOnClickListener(view -> super.onBackPressed());
    }

    private void reviewIntent() {
        reviewBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, Reviews.class);
            intent.putExtra("aID", ID);
            startActivity(intent);
        });
        navigate.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(title
                    + ", Jamshedpur" + ", Jharkhand"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
        googleMap.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(title
                    + ", Jamshedpur" + ", Jharkhand"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            view.getContext().startActivity(mapIntent);
        });
    }

    private void getData() {
        Intent intent = getIntent();
        ID = intent.getStringExtra("ID");
        title = intent.getStringExtra("aName");
        address = intent.getStringExtra("aAddress");
        if (address.length() < 1) textAddress.setVisibility(View.GONE);
        else textAddress.setText(address);
        textTitle.setText(title);
        FirebaseFirestore
                .getInstance()
                .collection("Attractions")
                .document(ID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        int x = 0;
                        while (snapshot.getString("image-" + x) != null) {
                            imageList.add(snapshot.getString("image-" + x));
                            x++;
                        }
                        setImage();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setImage() {
        int size = imageList.size();
        counter.setText("1/" + size);
        final int[] cnt = {0};
        if (imageList.size() > 0) {
            Glide.with(this).load(imageList.get(cnt[0]))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            imageView.setImageResource(R.drawable.image);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
        }

        rightBtn.setOnClickListener(view -> {
            if (imageList.size() > 0) {
                if (cnt[0] + 1 < size) {
                    progressBar.setVisibility(View.VISIBLE);
                    cnt[0]++;
                    counter.setText(cnt[0] + 1 + "/" + size);
                    Glide.with(this).load(imageList.get(cnt[0]))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    imageView.setImageResource(R.drawable.image);
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    counter.setText(cnt[0] + 1 + "/" + size);
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(imageView);
                }
            }
        });

        leftBtn.setOnClickListener(view -> {
            if (imageList.size() > 0) {
                if (cnt[0] - 1 >= 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    cnt[0]--;
                    counter.setText(cnt[0] + 1 + "/" + size);
                    Glide.with(this).load(imageList.get(cnt[0]))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    imageView.setImageResource(R.drawable.image);
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    counter.setText(cnt[0] + 1 + "/" + size);
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(imageView);
                }
            }
        });
    }

    private void initializations() {
        imageView = findViewById(R.id.placeImage);
        textTitle = findViewById(R.id.placeTitle);
        textAddress = findViewById(R.id.placeAddress);
        leftBtn = findViewById(R.id.leftBtn);
        rightBtn = findViewById(R.id.rightBtn);
        imageCounterView = findViewById(R.id.placeImageCounterView);
        counter = findViewById(R.id.placeImageCounter);
        progressBar = findViewById(R.id.placeProgress);
        reviewBtn = findViewById(R.id.reviews);
        toolbar = findViewById(R.id.toolbar);
        navigate = findViewById(R.id.navigate);
        googleMap = findViewById(R.id.googleMap);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), 10));
                        }
                    } else {
                        Log.d("TAG", "Current location is null. Using defaults.");
                        Log.e("TAG", "Exception: %s", task.getException());
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
}