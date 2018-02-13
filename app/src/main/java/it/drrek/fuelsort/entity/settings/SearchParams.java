package it.drrek.fuelsort.entity.settings;

/**
 * Created by Luca on 05/02/2018.
 */

public class SearchParams {
    private String carburante;
    private boolean isSelf;
    public SearchParams(String carburante, boolean isSelf){
        this.carburante = carburante;
        this.isSelf = isSelf;
    }
    public String getCarburante(){return carburante;}
    public boolean isSelf(){return isSelf;}

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
