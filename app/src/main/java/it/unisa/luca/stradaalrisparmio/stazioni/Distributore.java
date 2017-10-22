package it.unisa.luca.stradaalrisparmio.stazioni;

import android.icu.util.DateInterval;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import it.unisa.luca.stradaalrisparmio.stazioni.database.DBmanager;

/**
 * Da riempire
 * Created by luca on 08/10/17.
 */

public class Distributore implements Comparable{
    private int id;
    private String gestore;
    private String bandiera;
    private String tipoImpianto;
    private String nome;
    private String indirizzo;
    private String comune;
    private String provincia;
    private double lat;
    private double lon;
    ArrayList<Pompa> pompe;

    public Distributore(int id, String gestore, String bandiera, String tipoImpianto, String nome, String indirizzo, String comune, String provincia, double lat, double lon){
        this.id = id;
        this.gestore = gestore;
        this.bandiera = bandiera;
        this.tipoImpianto = tipoImpianto;
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.comune = comune;
        this.provincia = provincia;
        this.lat = lat;
        this.lon = lon;
        this.pompe = null;
    }

    public int getId(){return id;}
    public String getGestore(){return gestore;}
    public String getBandiera(){return bandiera;}
    public String getTipoImpianto(){return tipoImpianto;}
    public String getNome(){return nome;}
    public String getIndirizzo(){return indirizzo;}
    public String getComune(){return comune;}
    public String getProvincia(){return provincia;}
    public double getLat(){return lat;}
    public double getLon(){return lon;}

    public void setPompe(ArrayList<Pompa> pompe){
        this.pompe = pompe;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        if(((Distributore)o).getId()>this.id){
            return -1;
        }else if(((Distributore)o).getId()<this.id){
            return 1;
        }
        return 0;
    }

    public Float getLowestPrice(DBmanager.SearchParams params){
        Float lowest = 9.999f;
        for(Pompa p : pompe){
            if(params.checkCarburante(p.getCarburante()) && (params.isSelf() || (!params.isSelf() && !p.isSelf())) && p.getPrezzo()<lowest) {
                lowest = p.getPrezzo();
            }
        }
        return lowest;
    }

    public String toString(){
        String toString = "\n"+
                "Id: "+id+"\n"+
                "Gestore: "+gestore+"\n"+
                "Bandiera: "+bandiera+"\n"+
                "TipoImpianto: "+tipoImpianto+"\n"+
                "Nome: "+nome+"\n"+
                "Indirizzo: "+indirizzo+"\n"+
                "Comune: "+comune+"\n"+
                "Provincia: "+provincia+"\n"+
                "Lat Lng: "+lat+" "+lon+"\n";
        for (Pompa p : pompe){
            toString += "Pompa:" + p.toString() +"\n";
        }
        return toString;
    }

    public LatLng getPosizione(){
        return new LatLng(lat,lon);
    }
}
