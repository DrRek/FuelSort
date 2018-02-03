package it.drrek.fuelsort.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import it.drrek.fuelsort.control.update.DataUpdaterControl;
import it.drrek.fuelsort.control.update.DataUpdaterControlListener;
import it.drrek.fuelsort.entity.station.Distributore;
import it.drrek.fuelsort.control.map.MapControl;
import it.drrek.fuelsort.control.map.MapControlListener;
import it.drrek.fuelsort.control.route.RouteControl;
import it.drrek.fuelsort.control.route.RouteControlListener;
import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.support.LoadingManager;
import it.unisa.luca.fuelsort.R;

public class MapsActivity extends AppCompatActivity {

    private LoadingManager loaderManager;
    private MapControl mapControl;
    private RouteControl routeControl;

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
        DataUpdaterControl dataUpdaterControl = new DataUpdaterControl(this);
        dataUpdaterControl.setDataUpdaterControlListener(new DataUpdaterControlListener(){
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
        dataUpdaterControl.start();

        //This is used to create the map. All the action/things relative to the map will be managed by this class.
        mapControl = new MapControl((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map), this);
        mapControl.setListener(new MapControlListener() {
            @Override
            public void startSearchingStationInScreen() {
                loaderManager.add("Cerco distributori...");
            }
            @Override
            public void lowZoomWhileSearchingStationInScreen() {
                final Toast toast = Toast.makeText(getApplicationContext(), "Zooma di più per cercare distributori manualmente.", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 450);
            }
            @Override
            public void endSearchingStationInScreen() {
                loaderManager.remove("Cerco distributori...");
            }
        });

        //This include the algoritm to find the best station based on route.
        routeControl = new RouteControl(this);
        ((EditText) findViewById(R.id.to)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                startRouteSearch(null);
                return true;
            }
        });

        loaderManager.remove("Starting app...");
    }


    @Override
    protected void onResume() {
        super.onResume();

        mapControl.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapControl.onDestroy();
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

    public void startRouteSearch(View v){
        Toast.makeText(getApplicationContext(), "Inizio la ricerca!", Toast.LENGTH_SHORT).show();
        loaderManager.add("Ricercando un percorso...");
        routeControl.setListener(new RouteControlListener(){
            @Override
            public void routeFound(Route r, Distributore d) {
                mapControl.setRoute(r, d);
                loaderManager.remove("Ricercando un percorso...");
            }
        });
        routeControl.findRoute();
    }
}