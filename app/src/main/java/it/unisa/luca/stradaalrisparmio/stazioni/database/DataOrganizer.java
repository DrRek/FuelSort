package it.unisa.luca.stradaalrisparmio.stazioni.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by luca on 08/10/17.
 */

public class DataOrganizer {
    private String distributoriData, pompeData;

    public DataOrganizer(String distributoriAsString, String pompeAsString){
        this.distributoriData = distributoriAsString;
        this.pompeData = pompeAsString;

    }

    /**
     *
     * @param database
     * @return 0 means nothing to update, 1 means to update both, 2 means to update distributori, 3 means to update pompe.
     */
    public int isToUse(DBmanager database){
        String distributoriLatestVersion = distributoriData.substring(0, distributoriData.indexOf("\n"));
        String distributoriCurrentVersion = database.getDistributoriCurrentVersion();

        String pompeLatestVersion = pompeData.substring(0, pompeData.indexOf("\n"));
        String pompeCurrentVersion = database.getPompeCurrentVersion();

        if(distributoriLatestVersion.equalsIgnoreCase(distributoriCurrentVersion) &&
                pompeLatestVersion.equalsIgnoreCase(pompeCurrentVersion)){
            return 0;
        } else if(pompeLatestVersion.equalsIgnoreCase(pompeCurrentVersion)){
            return 2;
        }else if(distributoriLatestVersion.equalsIgnoreCase(distributoriCurrentVersion)){
            return 3;
        }
        return 1;
    }

    public void retrieveUpdatedDistributori(SQLiteDatabase wr){
        Log.d("Database", "Start: Insert distributori.");

        wr.delete(DBhelper.TBL_DISTRIBUTORI, DBhelper.FIELD_ID+">0", null);

        String tmpDistributoriData = distributoriData;
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
        Log.d("Database", "End: Insert distributori.");
    }

    public void retrieveUpdatedPompe(SQLiteDatabase wr){
        Log.d("Database", "Start: Insert pompe.");

        wr.delete(DBhelper.TBL_PREZZI, DBhelper.FIELD_ID+">0", null);

        String tmpPompeData = pompeData;
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
                stmt.bindDouble(3, Double.parseDouble(str[2]));
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
        Log.d("Database", "End: Insert pompe.");
    }
}
