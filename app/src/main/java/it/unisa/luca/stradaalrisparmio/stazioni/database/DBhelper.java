package it.unisa.luca.stradaalrisparmio.stazioni.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by luca on 08/10/17.
 */

public class DBhelper extends SQLiteOpenHelper{

    public static final String DB_NAME = "stradaalrisparmio";
    public static final String TBL_DISTRIBUTORI = "Anagrafica";
    public static final String FIELD_ID = "Id";
    public static final String FIELD_GESTORE = "Gestore";
    public static final String FIELD_BANDIERA = "Bandiera";
    public static final String FIELD_TIPO_IMPIANTO = "TipoImpianto";
    public static final String FIELD_NOME = "NOME";
    public static final String FIELD_INDIRIZZO = "Indirizzo";
    public static final String FIELD_COMUNE = "Comune";
    public static final String FIELD_PROVINCIA = "Provincia";
    public static final String FIELD_LAT = "Lat";
    public static final String FIELD_LON = "Lon";
    public static final String TBL_PREZZI = "Prezzi";
    public static final String FIELD_CARBURANTE = "Carburante";
    public static final String FIELD_PREZZO = "Prezzo";
    public static final String FIELD_IS_SELF = "IsSelf";
    public static final String FIELD_LATEST_UPDATE = "LatestUpdate";
    public static final String TBL_LATEST = "Latest";
    public static final String FIELD_DATA = "Dato";

    public static final String CMP_DISTIBUTORI = "distributori";
    public static final String CMP_POMPE = "pompe";

    public DBhelper(Context context) {
        super(context, DB_NAME, null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="CREATE TABLE "+DBhelper.TBL_DISTRIBUTORI+" ( " +
                FIELD_ID +" INTEGER PRIMARY KEY, " +
                FIELD_GESTORE+" TEXT, " +
                FIELD_BANDIERA+" TEXT, " +
                FIELD_TIPO_IMPIANTO+" TEXT, " +
                FIELD_NOME+" TEXT, " +
                FIELD_INDIRIZZO+" TEXT, " +
                FIELD_COMUNE+" TEXT, " +
                FIELD_PROVINCIA+" TEXT, " +
                FIELD_LAT+" REAL, " +
                FIELD_LON+" REAL );";
        Log.d("USATE:", q);
        db.execSQL(q);
        q="CREATE TABLE "+DBhelper.TBL_PREZZI+" ( " +
            FIELD_ID+" INTEGER," +
            FIELD_CARBURANTE+" TEXT," +
            FIELD_PREZZO+" REAL," +
            FIELD_IS_SELF+" INTEGER," +
            FIELD_LATEST_UPDATE+" TEXT," +
            "FOREIGN KEY ("+FIELD_ID+") REFERENCES "+TBL_DISTRIBUTORI+"("+FIELD_ID+") on update cascade on delete cascade," +
            "PRIMARY KEY ("+FIELD_ID+", "+FIELD_CARBURANTE+", "+FIELD_IS_SELF+") );";
        Log.d("USATE:", q);
        db.execSQL(q);

        q="CREATE TABLE "+DBhelper.TBL_LATEST+" ( " +
                FIELD_DATA+" TEXT PRIMARY KEY," +
                FIELD_LATEST_UPDATE+" TEXT );";
        Log.d("USATE:", q);
        db.execSQL(q);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {  }
}
