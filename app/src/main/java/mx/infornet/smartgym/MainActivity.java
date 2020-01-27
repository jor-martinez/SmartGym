package mx.infornet.smartgym;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private static final String TAG = "MainActivity";
    private Context context = this;
    private Dialog dialog_report;
    private SensorManager sensorManager;
    private Sensor sensor;
    private StringRequest request, request_pago;
    private RequestQueue queue, queue_pago;
    private String objetivo, token, token_type, fecha_termino="01/01/1999", fecha_fin="01/01/1999";
    private long dias;
    private Dialog dialog;
    private ParseJson parseJson;
    private List<Frases> frasesList;

    private int ID_PENDING = 1, ID_PENDING_AVANCE = 2;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    //cambié el valor de la variable para menor sensibilidad
    private static final int SHAKE_THRESHOLD = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        dialog_report = new Dialog(context);
        dialog = new Dialog(context);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeFragment()).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        int frasetrue = intent.getIntExtra("frase", 0);


        queue = Volley.newRequestQueue(getApplicationContext());
        //queue_obj = Volley.newRequestQueue(getApplicationContext());
        //queue_save_perder_peso = Volley.newRequestQueue(getApplicationContext());
        queue_pago = Volley.newRequestQueue(getApplicationContext());


        ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {

            String query = "SELECT * FROM usuarios";

            Cursor cursor = db.rawQuery(query, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("tokenType"));
                objetivo = cursor.getString(cursor.getColumnIndex("objetivo"));
                Log.w(TAG, objetivo );
            }

            cursor.close();

        }catch (Exception e){

            Toast toast = Toast.makeText(getApplicationContext(), "Error: "+  e.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
        db.close();

        Log.d("token", token);

        if (frasetrue==1){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShowFrase();
                    ShowPromosEventos();
                }
            }, 3000);



        }


        request_pago = new StringRequest(Request.Method.GET, Config.GET_PAGO_CURRENT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    //Log.d("RES_PAGO", jsonObject.toString());

                    fecha_fin = jsonObject.getString("fecha_fin");

                    SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    Date fecha_final = new Date();
                    try {
                        fecha_final = input.parse(fecha_fin);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    dias = getDiasRestantes(new Date(), fecha_final) + 1;


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(),
                            "Oops. Timeout error!",
                            Toast.LENGTH_LONG).show();
                }

                NetworkResponse networkResponse = error.networkResponse;

                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    try {
                        JSONObject jsonObjectError = new JSONObject(jsonError);
                        Log.e("error_pago", jsonObjectError.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", token_type + " " + token);
                return headers;
            }
        };

        queue_pago.add(request_pago);

        if (dias <= 5 && dias > 0){

            boolean alarmUP = (PendingIntent.getBroadcast(getApplicationContext(), ID_PENDING, new Intent(getApplicationContext(), BoadcastManager.class), PendingIntent.FLAG_NO_CREATE) != null);

            if (alarmUP){
                Log.d(TAG, "Dias restantes: Alarma YA activada");
            } else {
                Intent in = new Intent(getApplicationContext(), BoadcastManager.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ID_PENDING, in, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Log.d(TAG, "Dias restantes: alarma iniciada");
                am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        } else {

            Intent in = new Intent(getApplicationContext(), BoadcastManager.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ID_PENDING, in, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
            Log.d("alarma", "alarma cancelada");
        }

        if (objetivo.equals("Perder peso")){

            StringRequest request_peso = new StringRequest(Request.Method.GET, Config.GET_OBJ_PESO_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONArray jsonObject = new JSONArray(response);
                        //Log.w(TAG, "Peso: " + jsonObject.toString());

                        JSONObject registro = jsonObject.getJSONObject(0);

                        fecha_termino = registro.getString("created_at");
                        int tiempo = registro.getInt("tiempo");

                        /*Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MONTH, tiempo);*/


                        Log.w(TAG, "onResponse: "+ fecha_termino);

                        //SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                        Calendar cal = Calendar.getInstance();

                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                        try {
                            cal.setTime(sf.parse(fecha_termino));
                            cal.add(Calendar.MONTH, tiempo);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long hoy = System.currentTimeMillis();
                        long despues = cal.getTimeInMillis();

                        System.out.println("hoy: " +hoy);
                        System.out.println("despues: " +despues + " " + sf.format(cal.getTime()) );

                        Log.d("bool hoy > despues", String.valueOf(hoy >= despues));

                        if (hoy >= despues) {


                            Intent in = new Intent(getApplicationContext(), BroadcastAvancePerderPeso.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), ID_PENDING_AVANCE, in, PendingIntent.FLAG_UPDATE_CURRENT);

                            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            am.cancel(pendingIntent);
                            Log.d("alarma_avance", "alarma cancelada");

                            //en teoria deberia preguntar si quiere otro objetivo y se tendria que eliminar la bd y los datos en backend

                            //Tambien se borraría la tabla del objetivo perder peso

                            ShowNewObjetivoUno(objetivo);

                        }

                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (error instanceof TimeoutError) {
                        Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();
                    }

                    NetworkResponse networkResponse = error.networkResponse;

                    if (networkResponse != null && networkResponse.data != null) {
                        String jsonError = new String(networkResponse.data);
                        try {
                            JSONObject jsonObjectError = new JSONObject(jsonError);
                            Log.e("error_peso", jsonObjectError.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", token_type + " " + token);
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request_peso);

        } else if (objetivo.equals("Incrementar fuerza")){

            //pendiente que pongan campo tiempo en la base de datos

            StringRequest request_fuerza = new StringRequest(Request.Method.GET, Config.GET_OBJ_FUERZA_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONArray jsonObject = new JSONArray(response);
                        Log.w(TAG, "Fuerza: " + jsonObject.toString());

                        JSONObject registro = jsonObject.getJSONObject(0);

                        fecha_termino = registro.getString("created_at");
                        int tiempo = registro.getInt("tiempo");


                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                        try {
                            cal.setTime(sf.parse(fecha_termino));
                            cal.add(Calendar.MONTH, tiempo);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long hoy = System.currentTimeMillis();
                        long despues = cal.getTimeInMillis();

                        System.out.println("hoy: " +hoy);
                        System.out.println("Fecha final obj: " +despues + " " + sf.format(cal.getTime()) );

                        Log.d("bool hoy > despues", String.valueOf(hoy >= despues));

                        if (hoy >= despues) {

                            Log.d("llegó el día", "la fecha final ha pasado (fuerza)");

                            //al pasar la fecha final se borran los datos de la tabla de fuerza

                            //en teoria deberia preguntar si quiere otro objetivo y se tendria que eliminar la bd y los datos en backend

                            //Tambien se borraría la tabla del objetivo perder peso

                            ShowNewObjetivoUno(objetivo);

                        }


                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    if (error instanceof TimeoutError) {
                        Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();
                    }

                    NetworkResponse networkResponse = error.networkResponse;

                    if (networkResponse != null && networkResponse.data != null) {
                        String jsonError = new String(networkResponse.data);
                        try {
                            JSONObject jsonObjectError = new JSONObject(jsonError);
                            Log.e("error_fuerza", jsonObjectError.toString());


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", token_type + " " + token);
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request_fuerza);



        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.option_salir) {
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if(id == R.id.nav_inicio){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new HomeFragment());
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();


        } else if (id == R.id.nav_rutinas){

            Intent intentrutinas = new Intent(MainActivity.this, RutinasActivity.class);
            startActivity(intentrutinas);

        } else if (id == R.id.nav_alimentacion){

            Intent intentAlim = new Intent(MainActivity.this, AlimentacionActivity.class);
            startActivity(intentAlim);

        } else if (id == R.id.nav_progreso){

            if (objetivo.equals("Perder peso")){
                startActivity(new Intent(getApplicationContext(), ProgresoPerderPesoActivity.class));
            } else if (objetivo.equals("Incrementar fuerza")){
                startActivity(new Intent(getApplicationContext(), AvanceFuerzaActivity.class));
            }


        } else if (id == R.id.nav_perfil){

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new PerfilFragment());
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();


        } else if (id == R.id.nav_salir){

            request = new StringRequest(Request.Method.POST, Config.LOGOUT_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("res_logout", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("err_res_logout", error.toString());

                    if (error instanceof TimeoutError) {
                        Toast.makeText(getApplicationContext(),
                                "Oops. Timeout error!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders()throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", token_type+" "+token);
                    return headers;
                }
            };

            queue.add(request);

            ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
            SQLiteDatabase db = conn.getWritableDatabase();
            db.execSQL("DELETE FROM usuarios");

            startActivity(new Intent(MainActivity.this, LoginActivity.class));

            finish();

        } else if (id == R.id.nav_reportar){
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new ReportFragment());
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.addToBackStack(null);
            transaction.commit();*/

            startActivity(new Intent(MainActivity.this, ReportarActivity.class));


        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void ShowReport(){

        ImageButton cerrar_rep;
        final TextInputEditText msj_rep;
        MaterialButton enviar;

        dialog_report.setContentView(R.layout.report_layout);

        cerrar_rep = dialog_report.findViewById(R.id.btn_close_report_lay);
        msj_rep = dialog_report.findViewById(R.id.mensaje_report_lay);
        enviar = dialog_report.findViewById(R.id.btn_enviar_report_lay);

        cerrar_rep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_report.dismiss();
            }
        });

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mensje = msj_rep.getText().toString();

                if (TextUtils.isEmpty(mensje)){
                    msj_rep.setError("Introduzca el mensaje del reporte");
                    msj_rep.requestFocus();
                } else {

                    Intent itSend = new Intent(Intent.ACTION_SEND);

                    itSend.setData(Uri.parse("mailito"));
                    itSend.setType("plain/text");
                    itSend.putExtra(Intent.EXTRA_EMAIL, new String[] {"jor.martinez.salgado@gmail.com"});
                    itSend.putExtra(Intent.EXTRA_SUBJECT, "Reporte App Smart Gym miembro");
                    itSend.putExtra(Intent.EXTRA_TEXT, mensje);

                    try {
                        startActivity(Intent.createChooser(itSend, "Enviar email"));
                        Log.i("EMAIL", "Enviando email...");
                    } catch (android.content.ActivityNotFoundException e){
                        Toast.makeText(getApplicationContext(), "NO existe ningún cliente de email instalado!.", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        dialog_report.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_report.setCancelable(false);
        dialog_report.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialog_report.show();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this){

        }

        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();


            //cambié el tiempo que se verifica si se a invocado a onSensorChanged, cada 1 seg
            if ((curTime - lastUpdate) > 800) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    ShowReport();
                    //Toast.makeText(getApplicationContext(), "agitacion", Toast.LENGTH_LONG).show();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private long getDiasRestantes(Date fecha_inicial, Date fecha_final){
        long diferencia = fecha_final.getTime() - fecha_inicial.getTime();

        Log.i("MainActivity", "fechaInicial : " + fecha_inicial);
        Log.i("MainActivity", "fechaFinal : " + fecha_final);

        long segsMilli = 1000;
        long minsMilli = segsMilli * 60;
        long horasMilli = minsMilli * 60;
        long diasMilli = horasMilli * 24;

        long diasTranscurridos = diferencia / diasMilli;
        diferencia = diferencia % diasMilli;

        /*long horasTranscurridos = diferencia / horasMilli;
        diferencia = diferencia % horasMilli;

        long minutosTranscurridos = diferencia / minsMilli;
        diferencia = diferencia % minsMilli;

        long segsTranscurridos = diferencia / segsMilli;*/

        return diasTranscurridos;
    }

    public void ShowFrase(){
        TextView la_frase, el_autor;
        ImageButton cerrar_frase;

        dialog.setContentView(R.layout.popup_frase);

        cerrar_frase = dialog.findViewById(R.id.btn_close_popup);
        la_frase = dialog.findViewById(R.id.frase);
        el_autor = dialog.findViewById(R.id.autor_frase);

        cerrar_frase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        InputStream is = getResources().openRawResource(R.raw.frases_motivadoras);

        try {
            parseJson = new ParseJson();
            frasesList = parseJson.readJsonStream(is);
            System.out.println("Lectura json terminada");
        } catch (IOException e){
            e.printStackTrace();
        }

        int numRandom = (int) (Math.random() * frasesList.size() + 1);

        for (Frases frase : frasesList){
            int id_frase = frase.getId();

            if (id_frase == numRandom){
                String frase_json = frase.getFrase();
                String autor_json = frase.getAutor();

                la_frase.setText(frase_json);
                el_autor.setText(autor_json);
            }
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

    }

    public void ShowPromosEventos(){

        StringRequest request = new StringRequest(Request.Method.GET, Config.GET_PROMOS_EVENTOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray eventosOPromos = new JSONArray(response);

                    Log.d(TAG, "Response Eventos: " + eventosOPromos);

                    if (!eventosOPromos.toString().equals("[]")){

                        for (int i=0; i<eventosOPromos.length(); i++){

                            JSONObject item = eventosOPromos.getJSONObject(i);

                            final Dialog ejemplo = new Dialog(MainActivity.this);

                            ejemplo.setCancelable(false);
                            ejemplo.setContentView(R.layout.promos_eventos_layout);
                            ImageButton close = ejemplo.findViewById(R.id.btn_close_promo_evento);
                            TextView titulo = ejemplo.findViewById(R.id.tv_title_promo_evento);
                            TextView descripcion = ejemplo.findViewById(R.id.tv_descr_promo_evento);
                            ImageView imagenPromoEvento = ejemplo.findViewById(R.id.iv_promo_evento);

                            String nombreImagen = item.getString("imagen");

                            titulo.setText(item.getString("nombre"));
                            descripcion.setText(item.getString("descripcion"));

                            Picasso.get()
                                    .load(Config.IMAGEN_PROMO_EVENTO + nombreImagen)
                                    .into(imagenPromoEvento);

                            close.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ejemplo.dismiss();
                                }
                            });

                            ejemplo.show();

                        }

                    }


                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(),
                            "Oops. Timeout error!",
                            Toast.LENGTH_LONG).show();
                }

                NetworkResponse networkResponse = error.networkResponse;

                if (networkResponse != null && networkResponse.data != null) {
                    String jsonError = new String(networkResponse.data);
                    try {
                        JSONObject jsonObjectError = new JSONObject(jsonError);
                        Log.e(TAG, "Error eventos: " + jsonObjectError.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", token_type + " " + token);
                return headers;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);

    }

    public void ShowNewObjetivoUno(final String obj){

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.restart_objetivo_uno);
        final RadioButton reiniciar = dialog.findViewById(R.id.rb1_reiniciar);
        final RadioButton nuevo = dialog.findViewById(R.id.rb1_nuevo);
        final RadioButton ninguno = dialog.findViewById(R.id.rb1_ninguno);
        MaterialButton btnsig = dialog.findViewById(R.id.btn_new_objetivo1);
        btnsig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reiniciar.isChecked()){

                    if (obj.equals("Perder peso")){
                        ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "objetivo_perder_peso", null, 4);
                        SQLiteDatabase db = conn.getWritableDatabase();
                        db.execSQL("DELETE FROM objetivo_perder_peso");
                        db.close();

                        //se tendrian que pedir nuevamente los datos
                        startActivity(new Intent(MainActivity.this, PrePerderPesoActivity.class));

                    } else if (obj.equals("Incrementar fuerza")){

                        ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "objetivo_fuerza", null, 4);
                        SQLiteDatabase db = conn.getWritableDatabase();
                        db.execSQL("DELETE FROM objetivo_fuerza");
                        db.close();

                        //se tendrian que pedir nuevamente los datos
                        startActivity(new Intent(MainActivity.this, PreFuerzaActivity.class));

                    } else if (obj.equals("Aumentar masa muscular")){

                        //pendiente

                    }

                } else if (nuevo.isChecked()){

                    dialog.dismiss();
                    //se muestra el layout de los objetivos

                    Dialog dialog1 = new Dialog(MainActivity.this);
                    dialog1.setCancelable(false);
                    dialog1.setContentView(R.layout.spinner_objetivos_layout);
                    dialog1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    final RadioButton peso = dialog1.findViewById(R.id.rb2_peso);
                    final RadioButton fuerza = dialog1.findViewById(R.id.rb2_fuerza);
                    final RadioButton masa = dialog1.findViewById(R.id.rb2_masa);
                    switch (obj) {
                        case "Perder peso":
                            peso.setEnabled(false);
                            break;
                        case "Incrementar fuerza":
                            fuerza.setEnabled(false);
                            break;
                        case "Aumentar masa muscular":
                            masa.setEnabled(false);
                            break;
                    }
                    MaterialButton sig = dialog1.findViewById(R.id.btn_new_objetivo2);
                    sig.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (fuerza.isChecked()){

                                //se inicia el progreso de fuerza
                                startActivity(new Intent(MainActivity.this, PreFuerzaActivity.class));

                            } else if (masa.isChecked()){

                                //se inicia el progreso para aumentar masa muscular

                            } else if (peso.isChecked()){

                                //se inicia el progreso de perder peso
                                startActivity(new Intent(MainActivity.this, PrePerderPesoActivity.class));

                            }
                        }
                    });
                    dialog1.show();

                } else if (ninguno.isChecked()){
                    //no se hace ninguna accion
                    dialog.dismiss();

                }
            }
        });
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

    }


}
