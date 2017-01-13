package com.example.david.mapas;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

// Posicionamiento
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Geocoder;

import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Posicionamiento
    private LocationManager locManager;
    private LocationListener locListener;

    // Control GPS
    Geocoder geocoder;
    // Posicion en el mapa
    private Location posicionactual = null;
    // zoom : va desde 2 ( nivel mas alto) hasta 21 ( a nivel de calle )
    private int zoom = 2;
    // tipo de mapa
    private int tipomapa = 0;

    private ArrayList<LatLng> marcadores;

    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        obtenerPosicion();
        marcadores = new ArrayList<LatLng>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // CONFIGURACION DE GOOGLE MAPS
        // Tipo de Mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Aparece el botón para situarnos en el mapa mediante un circulo azul y hacer zoom sobre nuestra posición
        mMap.setMyLocationEnabled(true);
        // Controles de zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Aparece la brujula cuando giramos el mapa
        mMap.getUiSettings().setCompassEnabled(true);
        // mMap.getUiSettings().set.... para otras configuraciones
        // Listener de los eventos que detectan pulsaciones sobre la pantalla
        mapFragment.getMap().setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng posicion) {

                if(marcadores.size() == 3){
                    mMap.clear();
                    marcadores.clear();
                }else{
                    marcadores.add(posicion);
                    mMap.addMarker(new MarkerOptions().position(posicion).title("Marcador " + marcadores.size()));
                    if(marcadores.size() == 3){
                        PolylineOptions polylineOptions = new PolylineOptions();
                        for(int i=0; i<3; i++){
                            polylineOptions.add(marcadores.get(i));
                        }
                        mMap.addPolyline(polylineOptions);
                    }
                }
            }

        });
        mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng posicion) {
                mMap.addMarker(new MarkerOptions().position(posicion).title("Marcador creado con onMapLongClick").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void obtenerPosicion() {
        locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Nos registramos para recibir actualizaciones de la posición
        locListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                posicionactual = location;
                Toast.makeText(getApplicationContext(), "Nueva Posicion :" +
                                posicionactual.getLatitude()+" , "+posicionactual.getLongitude(), Toast.LENGTH_LONG).show();
            }
            public void onProviderDisabled(String provider){
            }
            public void onProviderEnabled(String provider){
            }
            public void onStatusChanged(String provider, int status, Bundle extras){
            }
        };
        // Comprobamos si el GPS esta activo
        Boolean isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            // Obtenemos nueva posicion por GPS
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
            if (posicionactual == null) {
                // Obtenemos la última posición conocida por GPS
                posicionactual = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Toast.makeText(getApplicationContext(), "Ultima Posicion por GPS",Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(getApplicationContext(), "Posicion por GPS",Toast.LENGTH_LONG).show();
            }
        }
        else {
            // Obtenemos nueva posicion por RED
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locListener);
            if (posicionactual == null) {
                // Obtenemos la última posición conocida por RED
                posicionactual = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Toast.makeText(getApplicationContext(), "Ultima Posicion por NETWORK_PROVIDER ",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Posicion por NETWORK_PROVIDER ",Toast.LENGTH_LONG).show();
            }
        }
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locListener);
    }

    private void addMarcador(LatLng position, String titulo, String info) {
        // Comprueb si hemos obtenido el mapa correctamente
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(position) // posicion clase LatLon
                    .title(titulo) // titulo
                    .snippet(info) // subtitulo
            );
        }
    }
    public void bt_localizame(View v) {
        // Comprobamos si hemos obtenido el MAPA correctamente
        if (mMap != null) {
            // Comprobamos si hemos obtenido NUESTRA POSICION ACTUAL ( ULTIMA OBTENIDA )correctamente
            if (posicionactual != null) {
                addMarcador(new LatLng(posicionactual.getLatitude(),
                        posicionactual.getLongitude()),"Titulo : Aqui estamos", "Snippe : Anexo al titulo");
            }
        }
    }
    public void bt_zoom(View v) {
        // Comprobamos si hemos obtenido el MAPA correctamente
        if (mMap != null) {
            // Obtenemos la posicion de la camara ( donde estamos enfocando actualmente )
            CameraPosition cp = mMap.getCameraPosition();
            // Obtenemos su posicion en Latitud , Longitud
            LatLng posicion = cp.target;
            // Obtenemos el zoom
            zoom = (int) cp.zoom;
            // Aumentamos el Zoom
            if (zoom < 21) {zoom++;};
            // Nos situamos en una posicion y le asignamos un zoom
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, zoom));
        }
    }

    public void bt_tipomapa(View v) {
        // Comprobamos si hemos obtenido el MAPA correctamente
        if (mMap != null) {
            tipomapa = (tipomapa + 1) % 4;
            switch(tipomapa) {
                case 0:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 2:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 3:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }
        }
    }
    // Movimiento de camara a una posicion realizando el movimiento
    public void bt_animateCamera(View v) {
        // Comprobamos si hemos obtenido el MAPA correctamente
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.173127,-3.6065), 15));
            addMarcador(new LatLng(37.173127, -3.6065), "titulo", "snippe");
        }
    }
}
