package it.drrek.fuelsort.control.update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Da riempire
 * Created by luca on 08/10/17.
 */

class DataImporter{

    static final String DISTRIBUTORI_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/anagrafica_impianti_attivi.csv";
    static final String POMPE_PATH = "http://www.sviluppoeconomico.gov.it/images/exportCSV/prezzo_alle_8.csv";

    String retrieve(String... params) {
        String link = params[0];
        try {
            URL url = new URL(link);
            InputStream is = url.openConnection().getInputStream();
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
