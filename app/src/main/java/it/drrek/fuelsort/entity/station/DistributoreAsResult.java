package it.drrek.fuelsort.entity.station;

import it.drrek.fuelsort.view.DistributoreAsResultFragmentListener;

/**
 * Created by Luca on 17/02/2018.
 */

public class DistributoreAsResult extends Distributore {

    private double kmPerProssimoDistributore;
    private double litriPerProssimoDistributore;
    private int costoBenzinaNecessaria;

    private DistributoreAsResult prev, next;

    public DistributoreAsResult(Distributore d){
        super(d.getId(), d.getGestore(), d.getBandiera(), d.getTipoImpianto(), d.getNome(), d.getIndirizzo(), d.getComune(), d.getProvincia(), d.getLat(), d.getLon());
        super.setPompe(d.getPompe());
        super.setPrice(d.getBestPriceUsingSearchParams());

        kmPerProssimoDistributore = 0;
        litriPerProssimoDistributore = 0;
        costoBenzinaNecessaria = 0;
        prev=null;
        next=null;
    }


    public double getKmPerProssimoDistributore() {
        return kmPerProssimoDistributore;
    }

    public void setKmPerProssimoDistributore(double kmPerProssimoDistributore) {
        this.kmPerProssimoDistributore = kmPerProssimoDistributore;
    }

    public double getLitriPerProssimoDistributore() {
        return litriPerProssimoDistributore;
    }

    public void setLitriPerProssimoDistributore(double litriPerProssimoDistributore) {
        this.litriPerProssimoDistributore = litriPerProssimoDistributore;
    }

    public int getCostoBenzinaNecessaria() {
        return costoBenzinaNecessaria;
    }

    public void setCostoBenzinaNecessaria(int costoBenzinaNecessaria) {
        this.costoBenzinaNecessaria = costoBenzinaNecessaria;
    }

    public DistributoreAsResult getPrev() {
        return prev;
    }

    public void setPrev(DistributoreAsResult prev) {
        prev.setNext(this);
        this.prev = prev;
    }

    public DistributoreAsResult getNext() {
        return next;
    }

    public void setNext(DistributoreAsResult next) {
        this.next = next;
    }
}
