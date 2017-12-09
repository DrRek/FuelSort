package it.unisa.luca.fuelsort.gasstation.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Da riempire
 * Created by luca on 08/10/17.
 */

class DatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "stradaalrisparmio";
    static final String TBL_DISTRIBUTORI = "Anagrafica";
    static final String FIELD_ID = "Id";
    static final String FIELD_GESTORE = "Gestore";
    static final String FIELD_BANDIERA = "Bandiera";
    static final String FIELD_TIPO_IMPIANTO = "TipoImpianto";
    static final String FIELD_NOME = "NOME";
    static final String FIELD_INDIRIZZO = "Indirizzo";
    static final String FIELD_COMUNE = "Comune";
    static final String FIELD_PROVINCIA = "Provincia";
    static final String FIELD_LAT = "Lat";
    static final String FIELD_LON = "Lon";
    static final String TBL_PREZZI = "Prezzi";
    static final String FIELD_CARBURANTE = "Carburante";
    static final String FIELD_PREZZO = "Prezzo";
    static final String FIELD_IS_SELF = "IsSelf";
    static final String FIELD_LATEST_UPDATE = "LatestUpdate";
    static final String TBL_LATEST = "Latest";
    static final String FIELD_DATA = "Dato";

    static final String CMP_DISTIBUTORI = "distributori";
    static final String CMP_POMPE = "pompe";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="CREATE TABLE "+ DatabaseHelper.TBL_DISTRIBUTORI+" ( " +
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
        q="CREATE TABLE "+ DatabaseHelper.TBL_PREZZI+" ( " +
            FIELD_ID+" INTEGER," +
            FIELD_CARBURANTE+" TEXT," +
            FIELD_PREZZO+" REAL," +
            FIELD_IS_SELF+" INTEGER," +
            FIELD_LATEST_UPDATE+" TEXT," +
            "FOREIGN KEY ("+FIELD_ID+") REFERENCES "+TBL_DISTRIBUTORI+"("+FIELD_ID+") on update cascade on delete cascade," +
            "PRIMARY KEY ("+FIELD_ID+", "+FIELD_CARBURANTE+", "+FIELD_IS_SELF+") );";
        Log.d("USATE:", q);
        db.execSQL(q);

        q="CREATE TABLE "+ DatabaseHelper.TBL_LATEST+" ( " +
                FIELD_DATA+" TEXT PRIMARY KEY," +
                FIELD_LATEST_UPDATE+" TEXT );";
        Log.d("USATE:", q);
        db.execSQL(q);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {  }
}