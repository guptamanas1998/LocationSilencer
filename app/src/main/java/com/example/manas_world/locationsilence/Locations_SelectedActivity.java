package com.example.manas_world.locationsilence;

import android.app.PendingIntent;
import android.content.ClipData;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Locations_SelectedActivity extends AppCompatActivity
        implements OnConnectionFailedListener {

    private RecyclerView mPlacesList;

    private DatabaseReference mDatabase;

    private ValueEventListener valueEventListener, LatitudeValueEventListener, LongitudeValueEventListener;

    private FirebaseAuth mAuth;

    private FirebaseUser mUser;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private FloatingActionButton mFloatingActionButton;

    private static final int PLACE_PICKER_REQUEST = 1;

    private static final String TAG = "Selected Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations__selected);

        mPlacesList = (RecyclerView) findViewById(R.id.placesList);
        mPlacesList.setHasFixedSize(true);
        mPlacesList.setLayoutManager(new LinearLayoutManager(this));

        /** Creating a reference to the UserId Nodes **/
        //------------------------------------------------------------------------//
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference(mUser.getUid());
        //------------------------------------------------------------------------//

        /** Enabling offline capabilities **/
        //---------------------------------------------------------------------------------------//
        mDatabase.keepSynced(true);
        //---------------------------------------------------------------------------------------//


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Conneced to the GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended Connection to GoogleApiClient");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "Failed to connect to googleApiClient - " + connectionResult.getErrorMessage());
                    }
                })
                .build();

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingactionbutton);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Locations_SelectedActivity.this, LoginActivity.class));
                }

            }
        };

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(Locations_SelectedActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }
//--------------------------------------------------------------------------------------------------------------------------------------//

    /** Inflating the Options Menu( Logout Icon ) **/

//---------------------------------------------------------------------------------------------------------------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "OncCreateOptionsMenu works");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return super.onCreateOptionsMenu(menu);
    }
//-----------------------------------------------------------------------------------------------------------------------------------------//

    /** Adding Logout Icon Button's functionality **/

//------------------------------------------------------------------------------------------------------------------------------------------//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout_icon:
                mAuth.signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
//-----------------------------------------------------------------------------------------------------------------------------------------//

    /** Retreiving data from the FireBbase Database in a RecyclerView
     * Deleting a Geofence.
     * Adding the Delete Button Functionality to the Card
     * Stopping the GPS service if the Firebase Database is  empty **/
//-----------------------------------------------------------------------------------------------------------------------------------------//
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<PlaceItem, PlaceItemHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PlaceItem, PlaceItemHolder>(
                PlaceItem.class,
                R.layout.place_item,
                PlaceItemHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final PlaceItemHolder viewHolder, final PlaceItem model, int position) {
                final String placeId = getRef(position).getKey();

                viewHolder.setPlaceName(model.getPlaceName());
                viewHolder.setPlaceAddress(model.getPlaceAddress());

                Geofence geofence = new Geofence.Builder()
                        .setRequestId(placeId)
                        .setCircularRegion(model.getPlaceLatitude(), model.getPlaceLongitude(), 20)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setNotificationResponsiveness(1000)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();

                GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).addGeofence(geofence).build();

                Intent intent = new Intent(Locations_SelectedActivity.this, GeofenceService.class);
                PendingIntent pendingIntent = PendingIntent.getService(Locations_SelectedActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if(!mGoogleApiClient.isConnected()){
                    Log.d(TAG, "GoogleApiClient not connected");
                }
                else{
                    //noinspection MissingPermission
                    LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofencingRequest, pendingIntent).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()){
                                Log.d(TAG, "Succesfully added geofence");
                            }
                            else{
                                Log.d(TAG, "Failed to add geofemce" + status.getStatus());
                            }
                        }
                    });
                }


                final String placeLatLngString = model.getPlaceLatitude() + "," + model.getPlaceLongitude();
                final ArrayList<String> deleteGeofenceList = new ArrayList<String>();
                deleteGeofenceList.add(placeLatLngString);


                viewHolder.mView.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, deleteGeofenceList);
                        deleteGeofenceList.clear();

                        mDatabase.child(placeId).removeValue();
                        valueEventListener = mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.hasChildren()){
                                    Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                                    stopService(i);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "Database Error" + databaseError.getMessage());
                            }
                        });
                        mDatabase.removeEventListener(valueEventListener);
                    }
                });
            }
        };
        mPlacesList.setAdapter(firebaseRecyclerAdapter);

    }
 //---------------------------------------------------------------------------------------------------------------------------------------//

    /** Checking if the user has selected a location or not
     * If the location is selected then we add it to the Firebase Database **/
//--------------------------------------------------------------------------------------------------//

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String placeName = place.getName().toString();
                String placeAddress = place.getAddress().toString();
                Double placeLatitude = place.getLatLng().latitude;
                Double placeLongitude = place.getLatLng().longitude;
                String placeLatLng = place.getLatLng().toString();
                addNewPlacetoFirebase(placeName, placeAddress, placeLatitude, placeLongitude);


                Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
            }
        }
    }
//--------------------------------------------------------------------------------------------------//

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(Locations_SelectedActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
    }


    /** Adding a place to Firebase.
      * Method is called in onActivityResult **/
//--------------------------------------------------------------------------------------------------//
    public void addNewPlacetoFirebase(String placeName, String placeAddress, Double placeLatitude, Double placeLongitude){
        DatabaseReference newPlace = mDatabase.push();
        newPlace.child("placeName").setValue(placeName);
        newPlace.child("placeAddress").setValue(placeAddress);
        newPlace.child("placeLatitude").setValue(placeLatitude);
        newPlace.child(("placeLongitude")).setValue(placeLongitude);
    }
//--------------------------------------------------------------------------------------------------//

    /** Creating a ViewHolder for the RecyclerView **/

    //-----------------------------------------------------------------------------------------------//
    public static class PlaceItemHolder extends RecyclerView.ViewHolder{

        View mView;

        public PlaceItemHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setPlaceName(String placeName){
            TextView placeNmaeTextView = (TextView) mView.findViewById(R.id.placeName);
            placeNmaeTextView.setText(placeName);
        }

        public void setPlaceAddress(String placeAddress){
            TextView placeAddressTextView = (TextView) mView.findViewById(R.id.placeAdress);
            placeAddressTextView.setText(placeAddress);
        }
    }
    //-----------------------------------------------------------------------------------------------//
}







