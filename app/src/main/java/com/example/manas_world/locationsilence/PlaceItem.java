package com.example.manas_world.locationsilence;

import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Manas_world on 24-06-2017.
 */
public class PlaceItem {

    private String placeName;
    private String placeAddress;
    private Double placeLatitude;
    private Double placeLongitude;

    public PlaceItem(){
    }

    public PlaceItem(String placeName, String placeAddress, Double placeLatitude, Double placeLongitude) {
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.placeLatitude = placeLatitude;
        this.placeLongitude = placeLongitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public Double getPlaceLatitude() {
        return placeLatitude;
    }

    public void setPlaceLatitude(Double placeLatitude) {
        this.placeLatitude = placeLatitude;
    }

    public Double getPlaceLongitude() {
        return placeLongitude;
    }

    public void setPlaceLongitude(Double placeLongitude) {
        this.placeLongitude = placeLongitude;
    }
}
