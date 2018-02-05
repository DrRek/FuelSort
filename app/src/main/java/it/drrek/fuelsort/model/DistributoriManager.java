package it.drrek.fuelsort.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.drrek.fuelsort.entity.settings.SearchParams;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.entity.station.Pompa;

import static android.R.attr.id;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Luca on 05/02/2018.
 */

public class DistributoriManager {

    private DatabaseCreator dbhelper;
    private Context activityContext;

    public DistributoriManager(Context ctx) {
        dbhelper=new DatabaseCreator(ctx);
        activityContext = ctx;
    }

    public List<Distributore> getStationsInBound(double solat, double nelat, double solng, double nelng, SearchParams params, boolean multipleThreadWorking){

        PompeManager pMan = new PompeManager(activityContext);

        List<Distributore> res = new ArrayList<>();
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+ DatabaseCreator.TBL_DISTRIBUTORI+" where " +
                DatabaseCreator.FIELD_LAT + " >= " + solat + " and " +
                DatabaseCreator.FIELD_LAT + " <= " + nelat + " and " +
                DatabaseCreator.FIELD_LON + " >= " + solng + " and " +
                DatabaseCreator.FIELD_LON + " <= " + nelng + ";";
        Cursor c =rd.rawQuery(sql, null);
        int id;
        String gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia;
        double lat, lon;
        Distributore temp;
        while(c.moveToNext()){
            id = c.getInt(c.getColumnIndex(DatabaseCreator.FIELD_ID));
            gestore = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_GESTORE));
            bandiera = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_BANDIERA));
            tipoImpianto = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_TIPO_IMPIANTO));
            nome = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_NOME));
            indirizzo = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_INDIRIZZO));
            comune = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_COMUNE));
            provincia = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_PROVINCIA));
            lat = c.getDouble(c.getColumnIndex(DatabaseCreator.FIELD_LAT));
            lon = c.getDouble(c.getColumnIndex(DatabaseCreator.FIELD_LON));
            temp = new Distributore(
                    id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon
            );
            if(pMan.setPompeForDistributore(temp, params, multipleThreadWorking)){
                temp.setPriceByParams(params);
                res.add(temp);
            }
        }
        c.close();
        return res;
    }

    public List<Distributore> getStationsInBound(double solat, double nelat, double solng, double nelng, SearchParams params, boolean multipleThreadWorking, boolean justToll){

        PompeManager pMan = new PompeManager(activityContext);

        List<Distributore> res = new ArrayList<>();
        SQLiteDatabase rd = dbhelper.getReadableDatabase();
        String sql = "SELECT * FROM "+ DatabaseCreator.TBL_DISTRIBUTORI+" where " +
                DatabaseCreator.FIELD_LAT + " >= " + solat + " and " +
                DatabaseCreator.FIELD_LAT + " <= " + nelat + " and " +
                DatabaseCreator.FIELD_LON + " >= " + solng + " and " +
                DatabaseCreator.FIELD_LON + " <= " + nelng + " and ";

        if(justToll){
            sql += DatabaseCreator.FIELD_TIPO_IMPIANTO + " like '" + DatabaseCreator.TIPO_AUTOSTRADA + "';";
        }else{
            sql += DatabaseCreator.FIELD_TIPO_IMPIANTO + " not like '" + DatabaseCreator.TIPO_AUTOSTRADA + "';";
        }
        Cursor c =rd.rawQuery(sql, null);
        int id;
        String gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia;
        double lat, lon;
        Distributore temp;
        while(c.moveToNext()){
            id = c.getInt(c.getColumnIndex(DatabaseCreator.FIELD_ID));
            gestore = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_GESTORE));
            bandiera = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_BANDIERA));
            tipoImpianto = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_TIPO_IMPIANTO));
            nome = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_NOME));
            indirizzo = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_INDIRIZZO));
            comune = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_COMUNE));
            provincia = c.getString(c.getColumnIndex(DatabaseCreator.FIELD_PROVINCIA));
            lat = c.getDouble(c.getColumnIndex(DatabaseCreator.FIELD_LAT));
            lon = c.getDouble(c.getColumnIndex(DatabaseCreator.FIELD_LON));
            temp = new Distributore(
                    id, gestore, bandiera, tipoImpianto, nome, indirizzo, comune, provincia, lat, lon
            );
            if(pMan.setPompeForDistributore(temp, params, multipleThreadWorking)){
                temp.setPriceByParams(params);
                res.add(temp);
            }
        }
        c.close();
        return res;
    }

    public void retrieveUpdatedDistributori(String distributoriNewData){
        Log.d("Database", "Start: Insert distributori.");

        SQLiteDatabase wr = dbhelper.getWritableDatabase();

        wr.delete(DatabaseCreator.TBL_DISTRIBUTORI, DatabaseCreator.FIELD_ID+">0", null);

        String tmpDistributoriData = distributoriNewData;
        String version = tmpDistributoriData.substring(0, tmpDistributoriData.indexOf("\n"));
        wr.delete(DatabaseCreator.TBL_LATEST, DatabaseCreator.FIELD_DATA+"='"+ DatabaseCreator.CMP_DISTIBUTORI+"'", null);
        wr.execSQL("insert into "+ DatabaseCreator.TBL_LATEST+" values ('"+ DatabaseCreator.CMP_DISTIBUTORI+"','"+version+"')");
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);
        tmpDistributoriData = tmpDistributoriData.substring(tmpDistributoriData.indexOf("\n")+1);

        InputStream is = new ByteArrayInputStream(tmpDistributoriData.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String sql = "insert into "+ DatabaseCreator.TBL_DISTRIBUTORI+" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        wr.beginTransaction();
        SQLiteStatement stmt = wr.compileStatement(sql);

        String line;
        try {
            while((line = br.readLine()) != null){
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

    public String getDistributoriCurrentVersion(){
        SQLiteDatabase readableDatabase = dbhelper.getReadableDatabase();
        String query = "select * from "+ DatabaseCreator.TBL_LATEST+" where "+ DatabaseCreator.FIELD_DATA+" = ?;";
        String[] parameters = new String[] {
                DatabaseCreator.CMP_DISTIBUTORI
        };
        Cursor results = readableDatabase.rawQuery(query, parameters);
        if(results.moveToNext()){
            String response = results.getString(results.getColumnIndex(DatabaseCreator.FIELD_LATEST_UPDATE));
            results.close();
            return response;
        }
        results.close();
        return "";
    }

}
