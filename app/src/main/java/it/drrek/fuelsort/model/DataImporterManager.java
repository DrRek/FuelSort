package it.drrek.fuelsort.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Da riempire
 * Created by luca on 08/10/17.
 */

public class DataImporterManager {

    public static final String DISTRIBUTORI_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv";
    public static final String POMPE_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/prezzo_alle_8.csv";

    public String retrieve(String... params) throws IOException{
        String link = params[0];
        URL url = new URL(link);
        InputStream is = url.openConnection().getInputStream();
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append("\n");
        }
        return buffer.toString();
    }
}
