package it.unisa.luca.stradaalrisparmio.api.strada;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
    private List<Step> regions;

    public List<Step> getRegions(){return regions;}

    /**
     * regions are used to reduce the area that need to be searched into the database on long trips. Each region is a square defined by north-east and south-ovest points wich need to be
     * searched for station.
     * @return regions finded.
     */
    public void calculateAndSetRegions(List<Step> steps){
        this.regions = new ArrayList<>();

        float latest_region_lenght = 0; //Expressed in kilometers
        float max_composed_region_size = 5000; //Region composed of lot of steps must be smaller than this value

        int regions_count = 1;
        this.regions.add(steps.get(0));
        final int step_size = steps.size();

        for(int i=1; i<step_size; i++){
            //in the start end stop variabile for now we will store point of the track, those will be changed to the bounds.
            if(steps.get(i).distance.value + this.regions.get(regions_count-1).distance.value > max_composed_region_size){
                //this means new region need to be added, easy
                this.regions.add(steps.get(i));
                regions_count++;
            } else{
                //need to be added to the current latest region
                this.regions.get(regions_count-1).end = steps.get(i).end;
            }
        }
    }
}
