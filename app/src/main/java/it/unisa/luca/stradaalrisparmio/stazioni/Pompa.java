package it.unisa.luca.stradaalrisparmio.stazioni;

/**
 * Created by luca on 08/10/17.
 */

public class Pompa {
    private int id;
    private String carburante;
    private double prezzo;
    private boolean isSelf;
    private String latestUpdate;

    Pompa(int id, String carburante, double prezzo, boolean isSelf, String latestUpdate){
        this.carburante = carburante;
        this.prezzo = prezzo;
        this.isSelf = isSelf;
        this.latestUpdate = latestUpdate;
    }

    public int getId(){return id;}
    public String getCarburante(){return carburante;}
    public double getPrezzo(){return prezzo;}
    public boolean isSelf(){return isSelf;}
    public String getLatestUpdate(){return latestUpdate;}
}
