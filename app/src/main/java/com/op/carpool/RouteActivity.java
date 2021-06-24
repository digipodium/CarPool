package com.op.carpool;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


public class RouteActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    public static final String DISTANCE = "distance";
    public static final String TIME = "time";
    public static final String DEPARTURE = "departure";
    public static final String END = "end";
    public static final String DEPARTURECITY = "departurecity";
    public static final String ENDCITY = "endcity";
    public static final String ORIGIN = "origin";
    public static final String DESTINATION = "destination";
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    ArrayList<RideDetailPart> fullDetails = new ArrayList<>();

    int INTENT_ID = 8976;
    MarkerOptions place1, place2, wayPoint1, wayPoint2;

    Animation ttbAnim, bttAnim;
    private MapboxMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private String strdeparture, strWaypoint1, strWaypoint2, strfinish, strdepartureCity, strFinishCity;

    private SearchView departureEditori, stageEditori, stageEditori2, finishEditori;
    private Button nextBtn;

    private ImageButton stageRemoveBtn, stageRemoveBtn2;
    private int locking = 0;
    private LinearLayout linearContainer;
    private ConstraintLayout routeDetails;
    private Button drawerButton;
    private ImageButton locationButton;
    private boolean drawer_expand = true;
    private MapView mapView;
    private DirectionsRoute currentRoute;
    private TextView distance;
    private TextView duration;
    private Point origin;
    private Point destination;
    private Style style;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null) {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_route);

        mapView = findViewById(R.id.map2);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setEnabled(false);

        findViewById(R.id.searchButton).setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        findViewById(R.id.stageBtn).setOnClickListener(this);
        distance = findViewById(R.id.infoTxt);
        duration = findViewById(R.id.infoTxt2);
        locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(this);

        stageRemoveBtn = findViewById(R.id.stageRemoveBtn);
        stageRemoveBtn2 = findViewById(R.id.stageRemoveBtn2);

        stageRemoveBtn.setOnClickListener(this);
        stageRemoveBtn2.setOnClickListener(this);

        departureEditori = findViewById(R.id.lahtoEdit);
        stageEditori = findViewById(R.id.stageEdit);
        stageEditori2 = findViewById(R.id.stageEdit2);

        ttbAnim = AnimationUtils.loadAnimation(this, R.anim.toptobottomanimation);
        bttAnim = AnimationUtils.loadAnimation(this, R.anim.bottomtotopanimation);

        linearContainer = findViewById(R.id.linearLayout);
        drawerButton = findViewById(R.id.drawer_bottom);

        routeDetails = findViewById(R.id.routeDetails);

        bttAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 0));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void expandableDrawer(View view) {
        animationHandler();
    }

    private void animationHandler() {
        if (!drawer_expand) {
            linearContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            doAnimation(ttbAnim);
            drawer_expand = true;
        } else {
            doAnimation(bttAnim);
            drawer_expand = false;
        }
    }

    private void doAnimation(Animation anim) {
        linearContainer.startAnimation(anim);
        drawerButton.startAnimation(anim);
        locationButton.startAnimation(anim);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.searchButton) {
            wayPoint1 = null;
            wayPoint2 = null;
            hideKeyboard(RouteActivity.this);
            finishEditori = findViewById(R.id.destinationEdit);
            strdeparture = departureEditori.getQuery().toString();
            strfinish = finishEditori.getQuery().toString();
            strWaypoint1 = stageEditori.getQuery().toString();
            strWaypoint2 = stageEditori2.getQuery().toString();
            Constant.waypointAddressesList.set(0, strWaypoint1);
            Constant.waypointAddressesList.set(1, strWaypoint2);

            if (strdeparture.trim().equals("") || strfinish.trim().equals("")) {
                Toast.makeText(getApplicationContext(), "Choose start and destination points", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    geoLocate(strdeparture, strfinish, strWaypoint1, strWaypoint2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else if (v.getId() == R.id.locationButton) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                final Geocoder geocoder = new Geocoder(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {

                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                        String address = addresses.get(0).getAddressLine(0);
                                        departureEditori.setQuery(address, false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Location failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        } else if (v.getId() == R.id.nextBtn) {
            Intent details = new Intent(this, RideDetailsActivity.class);
            details.putExtra(DISTANCE, Double.parseDouble(distance.getText().toString().split(" ")[0]));
            details.putExtra(TIME, Double.parseDouble(duration.getText().toString()));
            details.putExtra(DEPARTURE, strdeparture);
            details.putExtra(END, strfinish);
            details.putExtra(DEPARTURECITY, strdepartureCity);
            details.putExtra(ENDCITY, strFinishCity);
            details.putExtra(ORIGIN, origin);
            details.putExtra(DESTINATION, destination);

            startActivityForResult(details, INTENT_ID);
        } else if (v.getId() == R.id.stageBtn & locking == 0) {
            stageEditori.setVisibility(View.VISIBLE);
            stageRemoveBtn.setVisibility(View.VISIBLE);
            locking = 1;

        } else if (v.getId() == R.id.stageBtn & locking == 1) {
            stageEditori2.setVisibility(View.VISIBLE);
            stageRemoveBtn2.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.stageRemoveBtn) {
            stageEditori.setVisibility(View.GONE);
            stageRemoveBtn.setVisibility(View.GONE);
            strWaypoint1 = "";
            stageEditori.setQuery(strWaypoint1, true);
            locking = 0;
        } else if (v.getId() == R.id.stageRemoveBtn2) {
            stageEditori2.setVisibility(View.GONE);
            stageRemoveBtn2.setVisibility(View.GONE);
            strWaypoint2 = "";
            stageEditori2.setQuery(strWaypoint2, true);
        }

    }

    public void geoLocate(String start, String stop, String waypoint1, String waypoint2) throws IOException {
        mMap.clear();

        Geocoder gc = new Geocoder(this);
        double startLat = 0;
        double startLon = 0;
        double stopLat = 0;
        double stopLon = 0;
        double way1Lat = 0;
        double way1Lon = 0;
        double way2Lat = 0;
        double way2Lon = 0;

        try {
            List<Address> listStart = gc.getFromLocationName(start, 1);
            Address add = listStart.get(0);
            startLat = add.getLatitude();
            startLon = add.getLongitude();
            strdepartureCity = add.getLocality();
            Log.d("TESTIII", "startcity " + strdepartureCity);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Start position wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> listStop = gc.getFromLocationName(stop, 1);
            Address add2 = listStop.get(0);
            stopLat = add2.getLatitude();
            stopLon = add2.getLongitude();
            strFinishCity = add2.getLocality();
            Log.d("TESTIII", "endtcity " + strFinishCity);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Destination wrong", Toast.LENGTH_SHORT).show();
            return;
        }


        if (strWaypoint1 != null && !strWaypoint1.isEmpty()) {
            try {
                List<Address> listWay1 = gc.getFromLocationName(waypoint1, 1);
                Address add3 = listWay1.get(0);
                way1Lat = add3.getLatitude();
                way1Lon = add3.getLongitude();
                wayPoint1 = new MarkerOptions().position(new LatLng(way1Lat, way1Lon)).title("WayPoint1");
                mMap.addMarker(wayPoint1);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Waypoint wrong", Toast.LENGTH_SHORT).show();
                return;
            }

        }
        if (strWaypoint2 != null && !strWaypoint2.isEmpty()) {
            try {
                List<Address> listWay2 = gc.getFromLocationName(waypoint2, 1);
                Address add4 = listWay2.get(0);
                way2Lat = add4.getLatitude();
                way2Lon = add4.getLongitude();
                wayPoint2 = new MarkerOptions().position(new LatLng(way2Lat, way2Lon)).title("WayPoint2");
                mMap.addMarker(wayPoint2);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Waypoint wrong", Toast.LENGTH_SHORT).show();
            }

        }
        /*if(latitude != null && !latitude.isEmpty()){
            Double gpsLat = Double.valueOf(latitude);
            Double gpsLon = Double.valueOf(longitude);
            place1 = new MarkerOptions().position(new LatLng(gpsLat, gpsLon)).title("Location 1");
            latitude = null;
            longitude = null;
        }*/
        else {
            place1 = new MarkerOptions().position(new LatLng(startLat, startLon)).title("Location 1");
        }

        place2 = new MarkerOptions().position(new LatLng(stopLat, stopLon)).title("Location 2");


        animationHandler();

        routeDetails.setVisibility(View.VISIBLE);

        mMap.addMarker(place1);
        mMap.addMarker(place2);
        List<Point> waypoint = new ArrayList();
        if (way1Lat != 0.0 && way1Lon != 0.0) {
            waypoint.add(Point.fromLngLat(way1Lat, way1Lon));
        }
        if (way2Lat != 0.0 && way2Lon != 0.0) {
            waypoint.add(Point.fromLngLat(way2Lat, way2Lon));
        }
        origin = Point.fromLngLat(place1.getPosition().getLatitude(), place1.getPosition().getLongitude());
        destination = Point.fromLngLat(place2.getPosition().getLatitude(), place2.getPosition().getLongitude());
        initSource(style);
        initLayers(style);
        getRoute(mMap, origin, destination, waypoint);
        nextBtn.setEnabled(true);
//        MapboxDirections.Builder builder = MapboxDirections.builder()
//                .origin(origin)
//                .destination(destination)
//                .overview(DirectionsCriteria.OVERVIEW_FULL)
//                .profile(DirectionsCriteria.PROFILE_DRIVING)
//                .alternatives(true)
//                .steps(true)
//                .accessToken(getString(R.string.mapbox_access_token));
//        if (waypoint.size() > 0) {
//            builder.waypoints(waypoint);
//        }
//        MapboxDirections client = builder.build();
//        client.enqueueCall(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                if (response.body() == null) {
//                    Toast.makeText(RouteActivity.this, "No routes found, make sure you set the right user and access token.", Toast.LENGTH_LONG).show();
//                    return;
//                } else if (response.body().routes().size() < 1) {
//                    Toast.makeText(RouteActivity.this, "No routes found", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                currentRoute = response.body().routes().get(0);
//                distance.setText(currentRoute.distance() + " km ");
//                duration.setText(String.valueOf(currentRoute.duration()));
//
//                nextBtn.setEnabled(true);
//                mMap.getStyle(style -> {
//                    GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);
//                    if (source != null) {
//                        Toast.makeText(RouteActivity.this, "setting route", Toast.LENGTH_SHORT).show();
//                        Feature directionsRouteFeature = Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
//                        source.setGeoJson(directionsRouteFeature);
//                        style.addSource(source);
//                    } else {
//                        Toast.makeText(RouteActivity.this, "source is null", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//                Toast.makeText(RouteActivity.this, "error:" + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), 10));
    }


    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

        // Add the red marker icon image to the map
        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.mapbox_marker_icon_default)));

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, -9f})));
    }

    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination, List<Point> waypoint) {
        MapboxDirections.Builder builder = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token));

        if (waypoint.size() > 0) {
            builder.waypoints(waypoint);
        }
        MapboxDirections client = builder.build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Toast.makeText(RouteActivity.this, "Response code: " + response.code(), Toast.LENGTH_SHORT).show();
                if (response.body() == null) {
                    Toast.makeText(RouteActivity.this, "No routes found, make sure you set the right user and access token.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (response.body().routes().size() < 1) {
                    Toast.makeText(RouteActivity.this, "no routes found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the directions route
                currentRoute = response.body().routes().get(0);
                distance.setText(String.valueOf(currentRoute.distance()));
                duration.setText(String.valueOf(currentRoute.duration()));
                // Make a toast which displays the route's distance
                Toast.makeText(RouteActivity.this, currentRoute.distance() + "km", Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                            // Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                            // Create a LineString with the directions route's geometry and
                            // reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                            }
                        }
                    });
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(), 10));
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Toast.makeText(RouteActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INTENT_ID && resultCode == Activity.RESULT_OK) {
            RideDetailPart newPart = (RideDetailPart) data.getSerializableExtra("DETAILS");
            fullDetails.add(newPart);

            String date = newPart.date;
            String time = newPart.time;
            int passenger = newPart.passenger;
            float price = newPart.price;
            int range = newPart.range;

            /*TextView teksti = (TextView) findViewById(R.id.testailua);
            teksti.setText();*/
            Toast.makeText(this, date + " " + time + " " + passenger + " " + price + " " + range, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onMapReady(@NonNull @NotNull MapboxMap mapboxMap) {

        mMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            this.style = style;
            LatLng loc = new LatLng(26.8467, 80.9462);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 18));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
