package it.unisa.luca.fuelsort.gasstation.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by luca on 08/10/17.
 * Da continuare
 */

public class DatabaseUpdater extends Thread{
    private DatabaseHelper dbhelper;
    private DatabaseUpdaterListener databaseUpdaterListener;
    private String distributoriNewData, pompeNewData;

    public DatabaseUpdater(Context ctx) {
        dbhelper=new DatabaseHelper(ctx);
    }

    public void setDatabaseUpdaterListener(DatabaseUpdaterListener dul){
        this.databaseUpdaterListener = dul;
    }

    @Override
    public void run() {
        Log.d("Database", "Inizia il thread.");
        updateData();
    }

    private void updateData(){
        DataImporter browser = new DataImporter();

        distributoriNewData = browser.retrieve(DataImporter.DISTRIBUTORI_PATH);
        pompeNewData = browser.retrieve(DataImporter.POMPE_PATH);

        if(isToUpdateDistributori()){
            if(databaseUpdaterListener!=null)databaseUpdaterListener.onStartStationUpdate();
            retrieveUpdatedDistributori();
            if(databaseUpdaterListener!=null)databaseUpdaterListener.onEndStationUpdate();
        }
        if(isToUpdatePompe()){
            if(databaseUpdaterListener!=null)databaseUpdaterListener.onStartPriceUpdate();
            retrieveUpdatedPompe();
            if(databaseUpdaterListener!=null)databaseUpdaterListener.onEndPriceUpdate();
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
        String query = "select * from "+ DatabaseHelper.TBL_LATEST+" where "+ DatabaseHelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DatabaseHelper.CMP_DISTIBUTORI
        };
        Cursor results = readableDatabase.rawQuery(query, parameters);
        if(results.moveToNext()){
            String response = results.getString(results.getColumnIndex(DatabaseHelper.FIELD_LATEST_UPDATE));
            results.close();
            return response;
        }
        results.close();
        return "";
    }

    private String getPompeCurrentVersion(){
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String query = "select * from "+ DatabaseHelper.TBL_LATEST+" where "+ DatabaseHelper.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DatabaseHelper.CMP_POMPE
        };
        Cursor results = rd.rawQuery(query, parameters);
        if(results.moveToNext()) {
            String response = results.getString(results.getColumnIndex(DatabaseHelper.FIELD_LATEST_UPDATE));
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

        wr.delete(DatabaseHelper.TBL_DISTRIBUTORI, DatabaseHelper.FIELD_ID+">0", null);

        String tmpDistributoriData = distributoriNewData;
        String version = tmpDistributoriData.substring(0, tmpDistributoriData.indexOf("\n"));
        wr.delete(DatabaseHelper.TBL_LATEST, DatabaseHelper.FIELD_DATA+"='"+ DatabaseHelper.CMP_DISTIBUTORI+"'", null);
        wr.execSQL("insert into "+ DatabaseHelper.TBL_LATEST+" values ('"+ DatabaseHelper.CMP_DISTIBUTORI+"','"+version+"')");
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpDistributoriData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DatabaseHelper.TBL_DISTRIBUTORI+" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        wr.beginTransaction();
        SQLiteStatement stmt = wr.compileStatement(sql);

        String line;
        try {
            while((line = br.readLine()) != null){
                Log.d("Debug update",line);
                String[] str = line.split(";");
                try {
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
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e){
                    Log.d("Update distributori exc", Arrays.toString(e.getStackTrace()));
                }
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

        wr.delete(DatabaseHelper.TBL_PREZZI, DatabaseHelper.FIELD_ID+">0", null);

        String tmpPompeData = pompeNewData;
        String version = tmpPompeData.substring(0, tmpPompeData.indexOf("\n"));
        wr.delete(DatabaseHelper.TBL_LATEST, DatabaseHelper.FIELD_DATA+"='"+ DatabaseHelper.CMP_POMPE+"'", null);
        wr.execSQL("insert into "+ DatabaseHelper.TBL_LATEST+" values ('"+ DatabaseHelper.CMP_POMPE+"','"+version+"')");
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);
        tmpPompeData = tmpPompeData.substring(tmpPompeData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpPompeData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DatabaseHelper.TBL_PREZZI+" values (?, ?, ?, ?, ?);";
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
}
