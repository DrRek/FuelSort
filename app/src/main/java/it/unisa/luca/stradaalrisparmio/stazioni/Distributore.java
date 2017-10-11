package it.unisa.luca.stradaalrisparmio.stazioni;

import android.icu.util.DateInterval;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

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

    @Override
    public int compareTo(@NonNull Object o) {
        if(((Distributore)o).getId()>this.id){
            return -1;
        }else if(((Distributore)o).getId()<this.id){
            return 1;
        }
        return 0;
    }

    public LatLng getPosizione(){
        return new LatLng(lat,lon);
    }
}
