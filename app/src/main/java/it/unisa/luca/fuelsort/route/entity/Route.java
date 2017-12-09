package it.unisa.luca.fuelsort.route.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

public class Route {
    public static final int SUGGESTED_REGION_SIZE = 4000; //in m
    public static final int BOUNDS_FOR_REGION = 500; //in m

    private Distance distance;
    private Duration duration;
    private String endAddress;
    private LatLng endLocation;
    private String startAddress;
    private LatLng startLocation, northeast, southwest;
    private String parameters;

    private List<LatLng> points;
    private List<Step> regions;

    public LatLng getCenter(){
        double  minLat=Math.min(southwest.latitude, northeast.latitude),
                minLng=Math.min(southwest.longitude, northeast.longitude),
                maxLat=Math.max(southwest.latitude, northeast.latitude),
                maxLng=Math.max(southwest.longitude, northeast.longitude);
        return new LatLng((maxLat-minLat)/2+minLat, (maxLng-minLng)/2+minLng);
    }

    public LatLngBounds getLatLngBounds(){
        return new LatLngBounds(southwest, northeast);
    }

    public List<Step> getRegions() {
        return regions;
    }

    public void setRegions(List<Step> regions) {
        this.regions = regions;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public void setNortheast(LatLng northeast) {
        this.northeast = northeast;
    }

    public void setSouthwest(LatLng southwest) {
        this.southwest = southwest;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }
}
