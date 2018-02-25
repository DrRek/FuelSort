package it.drrek.fuelsort.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.List;

import it.drrek.fuelsort.R;
import it.drrek.fuelsort.control.map.MapControl;
import it.drrek.fuelsort.control.map.MapControlListener;
import it.drrek.fuelsort.control.route.RouteControl;
import it.drrek.fuelsort.control.route.RouteControlListener;
import it.drrek.fuelsort.control.update.DataUpdaterControl;
import it.drrek.fuelsort.control.update.DataUpdaterControlListener;
import it.drrek.fuelsort.entity.exception.NoDataForPathException;
import it.drrek.fuelsort.entity.exception.NoPathFoundException;
import it.drrek.fuelsort.entity.exception.NoStationForPathException;
import it.drrek.fuelsort.entity.exception.UnableToUpdateException;
import it.drrek.fuelsort.entity.route.Route;
import it.drrek.fuelsort.entity.station.DistributoreAsResult;
import it.drrek.fuelsort.support.LoadingManager;
import it.drrek.fuelsort.support.SingleShotLocationProvider;

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
        dataUpdaterControl.setDataUpdaterControlListener(new DataUpdaterControlListener() {
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
                loaderManager.remove("Starting app...");
            }

            @Override
            public void exceptionUpdatingData(Exception e) {
                loaderManager.remove("Updating price data...");
                loaderManager.remove("Updating station data...");
                loaderManager.remove("Starting app...");
                HandleExceptionAsListener(e);
            }

            @Override
            public void onEndStationUpdate() {
                loaderManager.remove("Updating station data...");
                loaderManager.remove("Starting app...");
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

        View.OnFocusChangeListener changeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                onChangeEditTextFocus(view, focus);
            }
        };
        findViewById(R.id.to).setOnFocusChangeListener(changeListener);
        findViewById(R.id.from).setOnFocusChangeListener(changeListener);
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

    public void startRouteSearch(View v) {

        //Solo per chiudere la tastiera
        View et = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (et != null) {
            //se è un edit text lo riporta alle dimensioni normali
            et.clearFocus();
            if (imm != null) {
                imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
            }
        }

        routeControl.setListener(new RouteControlListener() {
            ProgressDialog dialog;

            @Override
            public void startRouteSearch() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog = ProgressDialog.show(MapsActivity.this, "Ricerca stazioni", "Inizio la ricerca di un percorso ottimale.", true);
                        dialog.show();
                    }
                });
            }

            @Override
            public void sendMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setMessage(message);
                    }
                });
            }

            @Override
            public void routeFound(Route r, List<DistributoreAsResult> distributori) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                mapControl.setRoute(r, distributori);
            }

            @Override
            public void exceptionSearchingForRoute(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
                HandleExceptionAsListener(e);
            }
        });
        routeControl.findRoute();
    }

    public void HandleExceptionAsListener(Exception e) {
        if (e instanceof NoDataForPathException) {
            Log.e("MapsActivity", "Nessun percorso ritrovato.");
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            Dialog d = builder.create();
            d.show();
        } else if (e instanceof NoStationForPathException) {
            Log.e("MapsActivity", e.getMessage());
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
            builder.setMessage("Nessun distributore ritrovato. Se il problema persiste prova a forzare un aggiornamento dei dati nelle impostazioni.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            Dialog d = builder.create();
            d.show();
        } else if (e instanceof UnableToUpdateException) {
            Log.e("MapsActivity", "Impossibile aggiornare.");
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog d = builder.create();
                    d.show();
                }
            });
        } else if (e instanceof NoPathFoundException) {
            Log.e("MapsActivity", "Impossibile trovare un perrcorso.");
            final AlertDialog.Builder builder = new AlertDialog.Builder((Context) this);
            builder.setMessage(e.getMessage())
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog d = builder.create();
                    d.show();
                }
            });
        } else {
            e.printStackTrace();
        }
    }

    private void onChangeEditTextFocus(View view, boolean focus) {
        if (focus) {
            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            ((EditText) view).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }
    }

    public void setMapOnGpsPosition(View v) {
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(Location location) {
                        mapControl.setMyPosition(location.getLatitude(), location.getLongitude());
                    }
                });

    }
}