package it.drrek.fuelsort.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import it.drrek.fuelsort.entity.settings.SearchParams;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.station.Pompa;

import static android.R.attr.id;

/**
 * Created by Luca on 05/02/2018.
 */

public class PompeManager {
    private DatabaseCreator dbhelper;
    private Context activityContext;

    public PompeManager(Context ctx) {
        dbhelper=new DatabaseCreator(ctx);
        activityContext = ctx;
    }

    boolean setPompeForDistributore(Distributore d, SearchParams params, boolean multi_thread){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+ DatabaseCreator.TBL_PREZZI+" where "+ DatabaseCreator.FIELD_ID+"="+d.getId()+";";
        Cursor c =rd.rawQuery(sql, null);
        String carburante, latestUpdate;
        boolean isSelf;
        Float prezzo;
        ArrayList<Pompa> results = new ArrayList<>();
        boolean forNewbie = false;
        boolean rightPomp = false;
        while(c.moveToNext()){
            carburante = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_CARBURANTE));
            if(params.checkCarburante(carburante)){
                rightPomp = true;
            }
            prezzo = c.getFloat(c.getColumnIndex(DatabaseCreator.FIELD_PREZZO));
            if(c.getInt(c.getColumnIndex(DatabaseCreator.FIELD_IS_SELF))==1)
                isSelf=true;
            else {
                forNewbie = true;
                isSelf = false;
            }
            latestUpdate = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_LATEST_UPDATE));
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

    public void retrieveUpdatedPompe(String pompeNewData){
        Log.d("Database", "Start: Insert pompe.");

        SQLiteDatabase wr = dbhelper.getWritableDatabase();

        wr.delete(DatabaseCreator.TBL_PREZZI, DatabaseCreator.FIELD_ID+">0", null);

        String tmpPompeData = pompeNewData;
        String version = tmpPompeData.substring(0, tmpPompeData.indexOf("\n"));
        wr.delete(DatabaseCreator.TBL_LATEST, DatabaseCreator.FIELD_DATA+"='"+ DatabaseCreator.CMP_POMPE+"'", null);
        wr.execSQL("insert into "+ DatabaseCreator.TBL_LATEST+" values ('"+ DatabaseCreator.CMP_POMPE+"','"+version+"')");
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpPompeData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DatabaseCreator.TBL_PREZZI+" values (?, ?, ?, ?, ?);";
        wr.beginTransaction();
        SQLiteStatement stmt = wr.compileStatement(sql);

        String line;
        try {
            while((line = br.readLine()) != null){
                String[] str = line.split(";");
                if(Float.parseFloat(str[2]) >= 0.1) { //Se il prezzo non è realistico non l'aggiungo
                    stmt.bindLong(1, Integer.parseInt(str[0]));
                    stmt.bindString(2, str[1]);
                    stmt.bindDouble(3, Float.parseFloat(str[2]));
                    stmt.bindLong(4, Integer.parseInt(str[3]));
                    stmt.bindString(5, str[4]);
                    stmt.executeInsert();
                }
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

    public String getPompeCurrentVersion(){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String query = "select * from "+ DatabaseCreator.TBL_LATEST+" where "+ DatabaseCreator.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DatabaseCreator.CMP_POMPE
        };
        Cursor results = rd.rawQuery(query, parameters);
        if(results.moveToNext()) {
            String response = results.getString(results.getColumnIndex(DatabaseCreator.FIELD_LATEST_UPDATE));
            results.close();
            return response;
        }
        results.close();
        rd.close();
        return "";
    }
}
