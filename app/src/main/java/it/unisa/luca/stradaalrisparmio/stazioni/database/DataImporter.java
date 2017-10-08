package it.unisa.luca.stradaalrisparmio.stazioni.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by luca on 08/10/17.
 */

public class DataImporter{

    public static final String DISTRIBUTORI_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv";
    public static final String POMPE_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/prezzo_alle_8.csv";

    public String retrieve(String... params) {
        String link = params[0];
        try {
            URL url = new URL(link);
            InputStream is = url.openConnection().getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
