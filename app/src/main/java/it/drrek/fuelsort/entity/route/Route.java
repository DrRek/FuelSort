package it.drrek.fuelsort.entity.route;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public static final int SUGGESTED_REGION_SIZE = 4000; //in metri
    public static final int BOUNDS_FOR_NORMAL_REGIONS = 1000; //in metri
    public static final int BOUNDS_FOR_TOLLS_REGIONS = 10; //in metri
    public static final int DISTANZA_MASSIMA_AGGIUNTA_AL_PERCORSO = 2000; //in metri
    public static final int TEMPO_MASSIMO_AGGIUNTO_AL_PERCORSO = 600; //in secondi
    public static final int DIMENSIONI_MASSIME_PERCORSO_IN_STAZIONE_DI_SERVIZIO = 500; //in metri

    private Distance distance;
    private Duration duration;
    private String endAddress;
    private LatLng endLocation;
    private String startAddress;
    private LatLng startLocation, northeast, southwest;
    private String parameters;
    private int numeroDiPagamenti;

    private List<LatLng> points;
    private List<Region> regions;

    public LatLng getCenter(){
        double  minLat=Math.min(southwest.latitude, northeast.latitude),
                minLng=Math.min(southwest.longitude, northeast.longitude),
                maxLat=Math.max(southwest.latitude, northeast.latitude),
                maxLng=Math.max(southwest.longitude, northeast.longitude);
        return new LatLng((maxLat-minLat)/2+minLat, (maxLng-minLng)/2+minLng);
    }

    public int getNumeroDiPagamenti(){
        if(numeroDiPagamenti < 0 && regions!= null && !regions.isEmpty()) {
            numeroDiPagamenti = 0;

            for(int i=0; i<regions.size(); i++) {
                Region tempRegion = regions.get(i);
                if (tempRegion.isToll()) {
                    if (i < 2 || (i >= 2 && ((!regions.get(i - 1).isToll() && regions.get(i - 1).getDistance() >= DIMENSIONI_MASSIME_PERCORSO_IN_STAZIONE_DI_SERVIZIO) || (!regions.get(i - 1).isToll() && !regions.get(i - 2).isToll())))) {
                        numeroDiPagamenti++;
                    }
                }
            }

        }
        return numeroDiPagamenti;
    }

    public LatLngBounds getLatLngBounds(){
        return new LatLngBounds(southwest, northeast);
    }

    public List<Region> getRegions() {
        return regions;
    }

    /**
     * Le region vengono sommate alle preesistenti se presenti.
     * Importante notare che viene fatto il controllo tra la region esistente finale e quella iniziale nuova, questo è indispensabile perché il meccanismo
     * del calcolo delle autostrade funzioni a dovere!
     * @param newRegions
     */
    public void addRegions(List<Region> newRegions) {
        if((this.regions == null || this.regions.size() <= 0) && (newRegions == null || newRegions.size() <= 0)){
            this.regions = new ArrayList<>();
        } else if(this.regions == null || this.regions.size() <=0){
            this.regions = newRegions;
        } else if(!(this.regions == null || this.regions.size() <= 0) && !(newRegions == null || newRegions.size() <= 0)){
            Region lastOne = this.regions.get(this.regions.size()-1), firstTwo = newRegions.get(0);
            if(lastOne.isToll() == firstTwo.isToll() && lastOne.getDistance() + firstTwo.getDistance() <= Route.SUGGESTED_REGION_SIZE){
                lastOne.merge(firstTwo);
                newRegions.remove(0);
                this.regions.addAll(newRegions);
            }
        }

        numeroDiPagamenti = -1;
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
