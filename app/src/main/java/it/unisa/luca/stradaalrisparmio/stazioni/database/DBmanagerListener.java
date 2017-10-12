package it.unisa.luca.stradaalrisparmio.stazioni.database;

import java.util.List;

import it.unisa.luca.stradaalrisparmio.api.strada.Route;

public interface DBmanagerListener {
    void onUpdateDistributori();
    void onUpdatePompe();
    void onEndUpdateDistributori();
    void onEndUpdatePompe();
    void onDistributoriInRangeStart();
    void onDistributoriInRangeFound();
}
