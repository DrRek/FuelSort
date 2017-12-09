package it.unisa.luca.fuelsort.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import it.unisa.luca.fuelsort.gasstation.database.DatabaseUpdater;
import it.unisa.luca.fuelsort.gasstation.database.DatabaseUpdaterListener;
import it.unisa.luca.fuelsort.gasstation.entity.Distributore;
import it.unisa.luca.fuelsort.map.MapManager;
import it.unisa.luca.fuelsort.route.RouteManager;
import it.unisa.luca.fuelsort.route.RouteManagerListener;
import it.unisa.luca.fuelsort.route.entity.Route;
import it.unisa.luca.fuelsort.support.LoadingManager;
import it.unisa.luca.stradaalrisparmio.R;

public class MapsActivity extends AppCompatActivity {

    private LoadingManager loaderManager;
    private MapManager mapManager;
    private RouteManager routeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //The loader manager created will manage the "update view" to show the user background relevant event
        loaderManager = LoadingManager.getLoader(this);
        loaderManager.add("Starting app...");

        //This toolbar is the app bar used to display the option button.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //This will be executed to retrieve updated data relative to gas station and their price.
        DatabaseUpdater databaseUpdater = new DatabaseUpdater(this);
        databaseUpdater.setDatabaseUpdaterListener(new DatabaseUpdaterListener(){
            @Override
            public void onStartPriceUpdate() {
                loaderManager.add("Updating price data...");
            }
            @Override
            public void onStartStationUpdate() {
                loaderManager.add("Updating station data...");
            }
            @Override
            public void onEndPriceUpdate() {
                loaderManager.remove("Updating price data...");
            }
            @Override
            public void onEndStationUpdate() {
                loaderManager.remove("Updating station data...");
            }
        });
        databaseUpdater.start();

        //This is used to create the map. All the action/things relative to the map will be managed by this class.
        mapManager = new MapManager((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map), this);

        //This include the algoritm to find the best station based on route.
        routeManager = new RouteManager(this);
        ((EditText) findViewById(R.id.to)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Toast.makeText(getApplicationContext(), "Inizio la ricerca!", Toast.LENGTH_SHORT).show();
                loaderManager.add("Ricercando un percorso...");
                routeManager.setListener(new RouteManagerListener(){
                    @Override
                    public void routeFound(Route r, Distributore d) {
                        mapManager.setRoute(r, d);
                        loaderManager.remove("Ricercando un percorso...");
                    }
                });
                routeManager.findRoute();
                return true;
            }
        });

        loaderManager.remove("Starting app...");
    }


    @Override
    protected void onResume() {
        super.onResume();

        mapManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapManager.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
    }
}