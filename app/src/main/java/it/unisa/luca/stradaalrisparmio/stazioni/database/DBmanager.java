package it.unisa.luca.stradaalrisparmio.stazioni.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import it.unisa.luca.stradaalrisparmio.stazioni.Distributore;
import it.unisa.luca.stradaalrisparmio.support.Loading;

/**
 * Created by luca on 08/10/17.
 */

public class DBmanager extends Thread{
    private DBhelper dbhelper;
    private SQLiteDatabase wr;

    public DBmanager(Context ctx){
        dbhelper=new DBhelper(ctx);
    }

    @Override
    public void run() {
        Log.d("Database", "Inizia il thread.");
        updateData();
    }

    public void updateData(){
        DataImporter browser = new DataImporter();

        DataOrganizer updatedData = new DataOrganizer(browser.retrieve(DataImporter.DISTRIBUTORI_PATH), browser.retrieve(DataImporter.POMPE_PATH));
        int whatToUpdate = updatedData.isToUse(this);
        if(whatToUpdate != 0){
            Log.d("Database", "E' necessario aggiornare. cod:"+ whatToUpdate);
            wr = dbhelper.getWritableDatabase();
            if(whatToUpdate == 1 || whatToUpdate == 2){
                updatedData.retrieveUpdatedDistributori(wr);
            }
            if(whatToUpdate == 1 || whatToUpdate == 3){
                updatedData.retrieveUpdatedPompe(wr);
            }
            wr.close();
        } else{
            Log.d("Database", "Non è necessario aggiornare.");
        }
    }

    public String getDistributoriCurrentVersion(){
        SQLiteDatabase readableDatabase = dbhelper.getReadableDatabase();
        String query = "select * from "+DBhelper.TBL_LATEST+" where "+DBhelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DBhelper.CMP_DISTIBUTORI
        };
        Cursor results = readableDatabase.rawQuery(query, parameters);
        while(results.moveToNext()){
            return results.getString(results.getColumnIndex(DBhelper.FIELD_LATEST_UPDATE));
        }
        return "";
    }

    public String getPompeCurrentVersion(){
        SQLiteDatabase readableDatabase = dbhelper.getReadableDatabase();
        String query = "select * from "+DBhelper.TBL_LATEST+" where "+DBhelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DBhelper.CMP_POMPE
        };
        Cursor results = readableDatabase.rawQuery(query, parameters);
        while(results.moveToNext()){
            return results.getString(results.getColumnIndex(DBhelper.FIELD_LATEST_UPDATE));
        }
        readableDatabase.close();
        return "";
    }

    public ArrayList<Distributore> getDistributoriInRange(double minLat, double maxLat, double minLng, double maxLng){
        ArrayList<Distributore> risultati = new ArrayList<Distributore>();
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+DBhelper.TBL_DISTRIBUTORI+" where " +
                DBhelper.FIELD_LAT + " >= " + minLat + " and " +
                DBhelper.FIELD_LAT + " <= " + maxLat + " and " +
                DBhelper.FIELD_LON + " >= " + minLng + " and " +
                DBhelper.FIELD_LON + " <= " + maxLng + ";";

        Cursor c =rd.rawQuery(sql, null);
        int id;
        String gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia;
        double lat, lon;
        while(c.moveToNext()){
            id = c.getInt(c.getColumnIndex(DBhelper.FIELD_ID));
            gestore = c.getString(c.getColumnIndex(DBhelper.FIELD_GESTORE));
            bandiera = c.getString(c.getColumnIndex(DBhelper.FIELD_BANDIERA));
            tipoImpianto = c.getString(c.getColumnIndex(DBhelper.FIELD_TIPO_IMPIANTO));
            nome = c.getString(c.getColumnIndex(DBhelper.FIELD_NOME));
            indirizzo = c.getString(c.getColumnIndex(DBhelper.FIELD_INDIRIZZO));
            comune = c.getString(c.getColumnIndex(DBhelper.FIELD_COMUNE));
            provincia = c.getString(c.getColumnIndex(DBhelper.FIELD_PROVINCIA));
            lat = c.getDouble(c.getColumnIndex(DBhelper.FIELD_LAT));
            lon = c.getDouble(c.getColumnIndex(DBhelper.FIELD_LON));
            risultati.add(
                    new Distributore(
                            id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon
                    )
            );
        }
        rd.close();
        return risultati;
    }
}
