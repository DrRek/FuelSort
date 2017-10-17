package it.unisa.luca.stradaalrisparmio.stazioni.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import it.unisa.luca.stradaalrisparmio.api.strada.Route;
import it.unisa.luca.stradaalrisparmio.stazioni.Distributore;
import it.unisa.luca.stradaalrisparmio.stazioni.Pompa;
import it.unisa.luca.stradaalrisparmio.support.Loading;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.R.attr.id;

/**
 * Created by luca on 08/10/17.
 * Da continuare
 */

public class DBmanager extends Thread{
    private DBhelper dbhelper;
    private Activity activity;
    private String distributoriNewData, pompeNewData;

    public DBmanager(Context ctx) {
        activity = (Activity) ctx;
        dbhelper=new DBhelper(ctx);
    }

    @Override
    public void run() {
        Log.d("Database", "Inizia il thread.");
        updateData();
    }

    private void updateData(){
        final Loading loaderView = Loading.getLoader(activity);
        DataImporter browser = new DataImporter();

        distributoriNewData = browser.retrieve(DataImporter.DISTRIBUTORI_PATH);
        pompeNewData = browser.retrieve(DataImporter.POMPE_PATH);

        if(isToUpdateDistributori()){
            loaderView.add("Updating station data...");
            retrieveUpdatedDistributori();
            loaderView.remove("Updating station data...");
        }
        if(isToUpdatePompe()){
            loaderView.add("Updating price data...");
            retrieveUpdatedPompe();
            loaderView.remove("Updating price data...");
        }
        Log.d("Database", "End managing updates");
    }

    private boolean isToUpdateDistributori(){
        String distributoriLatestVersion = distributoriNewData.substring(0, distributoriNewData.indexOf("\n"));
        return !getDistributoriCurrentVersion().equalsIgnoreCase(distributoriLatestVersion);
    }

    private boolean isToUpdatePompe(){
        String pompeLatestVersion = pompeNewData.substring(0, pompeNewData.indexOf("\n"));
        return !getPompeCurrentVersion().equalsIgnoreCase(pompeLatestVersion);
    }

    private String getDistributoriCurrentVersion(){
        SQLiteDatabase readableDatabase = dbhelper.getReadableDatabase();
        String query = "select * from "+DBhelper.TBL_LATEST+" where "+DBhelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DBhelper.CMP_DISTIBUTORI
        };
        Cursor results = readableDatabase.rawQuery(query, parameters);
        if(results.moveToNext()){
            String response = results.getString(results.getColumnIndex(DBhelper.FIELD_LATEST_UPDATE));
            results.close();
            return response;
        }
        results.close();
        return "";
    }

    private String getPompeCurrentVersion(){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String query = "select * from "+DBhelper.TBL_LATEST+" where "+DBhelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DBhelper.CMP_POMPE
        };
        Cursor results = rd.rawQuery(query, parameters);
        if(results.moveToNext()) {
            String response = results.getString(results.getColumnIndex(DBhelper.FIELD_LATEST_UPDATE));
            results.close();
            return response;
        }
        results.close();
        rd.close();
        return "";
    }

    private void retrieveUpdatedDistributori(){
        Log.d("Database", "Start: Insert distributori.");

        SQLiteDatabase wr = dbhelper.getWritableDatabase();

        wr.delete(DBhelper.TBL_DISTRIBUTORI, DBhelper.FIELD_ID+">0", null);

        String tmpDistributoriData = distributoriNewData;
        String version = tmpDistributoriData.substring(0, tmpDistributoriData.indexOf("\n"));
        wr.delete(DBhelper.TBL_LATEST, DBhelper.FIELD_DATA+"='"+DBhelper.CMP_DISTIBUTORI+"'", null);
        wr.execSQL("insert into "+DBhelper.TBL_LATEST+" values ('"+DBhelper.CMP_DISTIBUTORI+"','"+version+"')");
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpDistributoriData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DBhelper.TBL_DISTRIBUTORI+" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        wr.beginTransaction();
        SQLiteStatement stmt = wr.compileStatement(sql);

        String line;
        try {
            while((line = br.readLine()) != null){
                String[] str = line.split(";");
                stmt.bindLong(1, Integer.parseInt(str[0]));
                stmt.bindString(2, str[1]);
                stmt.bindString(3, str[2]);
                stmt.bindString(4, str[3]);
                stmt.bindString(5, str[4]);
                stmt.bindString(6, str[5]);
                stmt.bindString(7, str[6]);
                stmt.bindString(8, str[7]);
                stmt.bindDouble(9, Double.parseDouble(str[8]));
                stmt.bindDouble(10, Double.parseDouble(str[9]));
                stmt.executeInsert();
                stmt.clearBindings();
            }
            wr.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            wr.endTransaction();
        }
        wr.close();
        Log.d("Database", "End: Insert distributori.");
    }

    private void retrieveUpdatedPompe(){
        Log.d("Database", "Start: Insert pompe.");

        SQLiteDatabase wr = dbhelper.getWritableDatabase();

        wr.delete(DBhelper.TBL_PREZZI, DBhelper.FIELD_ID+">0", null);

        String tmpPompeData = pompeNewData;
        String version = tmpPompeData.substring(0, tmpPompeData.indexOf("\n"));
        wr.delete(DBhelper.TBL_LATEST, DBhelper.FIELD_DATA+"='"+DBhelper.CMP_POMPE+"'", null);
        wr.execSQL("insert into "+DBhelper.TBL_LATEST+" values ('"+DBhelper.CMP_POMPE+"','"+version+"')");
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpPompeData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DBhelper.TBL_PREZZI+" values (?, ?, ?, ?, ?);";
        wr.beginTransaction();
        SQLiteStatement stmt = wr.compileStatement(sql);

        String line;
        try {
            while((line = br.readLine()) != null){
                String[] str = line.split(";");
                stmt.bindLong(1, Integer.parseInt(str[0]));
                stmt.bindString(2, str[1]);
                stmt.bindDouble(3, Float.parseFloat(str[2]));
                stmt.bindLong(4, Integer.parseInt(str[3]));
                stmt.bindString(5, str[4]);
                stmt.executeInsert();
                stmt.clearBindings();
            }
            wr.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            wr.endTransaction();
        }
        wr.close();
        Log.d("Database", "End: Insert pompe.");
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
        Distributore temp;
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
            temp = new Distributore(id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon);
            setPompeForDistributore(temp);
            risultati.add(temp);
        }
        c.close();
        rd.close();
        return risultati;
    }

    public void setPompeForDistributore(Distributore d){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+DBhelper.TBL_PREZZI+" where "+DBhelper.FIELD_ID+"="+d.getId()+";";
        Cursor c =rd.rawQuery(sql, null);
        String carburante, latestUpdate;
        boolean isSelf;
        Float prezzo;
        ArrayList<Pompa> results = new ArrayList<>();
        while(c.moveToNext()){
            carburante = c.getString(c.getColumnIndex(DBhelper.FIELD_CARBURANTE));
            prezzo = c.getFloat(c.getColumnIndex(DBhelper.FIELD_PREZZO));
            if(c.getInt(c.getColumnIndex(DBhelper.FIELD_IS_SELF))==1)
                isSelf=true;
            else
                isSelf=false;
            latestUpdate = c.getString(c.getColumnIndex(DBhelper.FIELD_LATEST_UPDATE));
            results.add(
                    new Pompa(
                            id, carburante, prezzo, isSelf, latestUpdate
                    )
            );
        }
        c.close();
        rd.close();
        d.setPompe(results);
    }

    public ArrayList<Distributore> getZoneStation(Route r){
        ArrayList<Distributore> results = new ArrayList<Distributore>();
        Double minLat, maxLat, minLng, maxLng;
        minLat = r.startLocation.latitude;
        maxLat = minLat;
        minLng = r.startLocation.longitude;
        maxLng = minLng;
        for(LatLng p : r.points){
            if(p.latitude < minLat) minLat = p.latitude;
            if(p.latitude > maxLat) maxLat = p.latitude;
            if(p.longitude < minLng) minLng = p.longitude;
            if(p.longitude > maxLng) maxLng = p.longitude;
        }
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
            results.add(
                    new Distributore(
                            id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon
                    )
            );
        }
        c.close();
        rd.close();
        return results;
    }
}
