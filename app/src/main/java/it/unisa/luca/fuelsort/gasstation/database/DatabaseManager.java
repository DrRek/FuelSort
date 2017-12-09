package it.unisa.luca.fuelsort.gasstation.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.gasstation.entity.Pompa;

import static android.R.attr.id;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by luca on 08/10/17.
 * Da continuare
 */

public class DatabaseManager extends Thread{
    private DatabaseHelper dbhelper;
    private Context activityContext;

    public DatabaseManager(Context ctx) {
        dbhelper=new DatabaseHelper(ctx);
        activityContext = ctx;
    }

    private boolean setPompeForDistributore(Distributore d, SearchParams params, boolean multi_thread){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+ DatabaseHelper.TBL_PREZZI+" where "+ DatabaseHelper.FIELD_ID+"="+d.getId()+";";
        Cursor c =rd.rawQuery(sql, null);
        String carburante, latestUpdate;
        boolean isSelf;
        Float prezzo;
        ArrayList<Pompa> results = new ArrayList<>();
        boolean forNewbie = false;
        boolean rightPomp = false;
        while(c.moveToNext()){
            carburante = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_CARBURANTE));
            if(params.checkCarburante(carburante)){
                rightPomp = true;
            }
            prezzo = c.getFloat(c.getColumnIndex(DatabaseHelper.FIELD_PREZZO));
            if(c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_IS_SELF))==1)
                isSelf=true;
            else {
                forNewbie = true;
                isSelf = false;
            }
            latestUpdate = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_LATEST_UPDATE));
            results.add(
                    new Pompa(
                            id, carburante, prezzo, isSelf, latestUpdate
                    )
            );
        }
        c.close();
        if(!multi_thread)
            rd.close();
        if(!rightPomp || (!params.isSelf() && !forNewbie))
            return false;
        d.setPompe(results);
        return true;
    }

    public List<Distributore> getStationsInBound(double solat, double nelat, double solng, double nelng, SearchParams params, boolean multipleThreadWorking){

        List<Distributore> res = new ArrayList<>();
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+ DatabaseHelper.TBL_DISTRIBUTORI+" where " +
                DatabaseHelper.FIELD_LAT + " >= " + solat + " and " +
                DatabaseHelper.FIELD_LAT + " <= " + nelat + " and " +
                DatabaseHelper.FIELD_LON + " >= " + solng + " and " +
                DatabaseHelper.FIELD_LON + " <= " + nelng + ";";
        Cursor c =rd.rawQuery(sql, null);
        int id;
        String gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia;
        double lat, lon;
        Distributore temp;
        while(c.moveToNext()){
            id = c.getInt(c.getColumnIndex(DatabaseHelper.FIELD_ID));
            gestore = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_GESTORE));
            bandiera = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_BANDIERA));
            tipoImpianto = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_TIPO_IMPIANTO));
            nome = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_NOME));
            indirizzo = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_INDIRIZZO));
            comune = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_COMUNE));
            provincia = c.getString(c.getColumnIndex(DatabaseHelper.FIELD_PROVINCIA));
            lat = c.getDouble(c.getColumnIndex(DatabaseHelper.FIELD_LAT));
            lon = c.getDouble(c.getColumnIndex(DatabaseHelper.FIELD_LON));
            temp = new Distributore(
                    id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon
            );
            if(setPompeForDistributore(temp, params, multipleThreadWorking)){
                temp.setPriceByParams(params);
                res.add(temp);
            }
        }
        c.close();
        return res;
    }

    public SearchParams getSearchParams() {
        SharedPreferences pref = activityContext.getSharedPreferences("it.unisa.luca.stradaalrisparmio.pref", MODE_PRIVATE);
        String prefCarburante = pref.getString("carburante", "diesel");
        boolean prefSelf = pref.getBoolean("self", true);
        int prefKmxl = pref.getInt("kmxl", 20);
        return new DatabaseManager.SearchParams(prefCarburante, prefSelf, prefKmxl);
    }

    /**
     * carburante may be diesel benzina gpl o metano
     */
    public static class SearchParams{
        private String carburante;
        private boolean isSelf;
        private int kmxl;
        private SearchParams(String carburante, boolean isSelf, int kmxl){
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
}
