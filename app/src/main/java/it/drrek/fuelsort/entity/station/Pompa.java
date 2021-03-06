package it.drrek.fuelsort.entity.station;

import java.io.Serializable;

/**
 * A 'Disitributore' is based on one or more 'Pompa' class
 * Created by luca on 08/10/17.
 */

public class Pompa implements Serializable{

    private static final long serialVersionUID = 1L;

    private int id;
    private String carburante;
    private float prezzo;
    private boolean isSelf;
    private String latestUpdate;

    public Pompa(int id, String carburante, float prezzo, boolean isSelf, String latestUpdate){
        this.id = id;
        this.carburante = carburante;
        this.prezzo = prezzo;
        this.isSelf = isSelf;
        this.latestUpdate = latestUpdate;
    }

    public int getId(){return id;}
    public String getCarburante(){return carburante;}
    public float getPrezzo(){return prezzo;}
    public boolean isSelf(){return isSelf;}
    public String getLatestUpdate(){return latestUpdate;}

    public String toString(){
        return "\n\tCarburante: "+carburante+"\n\tPrezzo: "+prezzo+"€\n\tSelf: "+isSelf+"\n\tLatest update: "+latestUpdate;
    }
}
