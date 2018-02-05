package it.drrek.fuelsort.entity.settings;

/**
 * Created by Luca on 05/02/2018.
 */

public class SearchParams {
    private String carburante;
    private boolean isSelf;
    private int kmxl;
    public SearchParams(String carburante, boolean isSelf, int kmxl){
        this.carburante = carburante;
        this.isSelf = isSelf;
        this.kmxl = kmxl;
    }
    public String getCarburante(){return carburante;}
    public boolean isSelf(){return isSelf;}
    public int getKmxl(){return kmxl;}

    public boolean checkCarburante(String toCheck){
        toCheck = toCheck.toLowerCase();
        if(toCheck.contains(carburante.toLowerCase())) return true;
        else if(carburante.equalsIgnoreCase("diesel")){
            if(toCheck.contains("diesel") || toCheck.contains("gasolio")) return true;
        } else if(carburante.equalsIgnoreCase("benzina")){
            if(toCheck.contains("benzina") || toCheck.contains("super")) return true;
        }
        return false;
    }
}
