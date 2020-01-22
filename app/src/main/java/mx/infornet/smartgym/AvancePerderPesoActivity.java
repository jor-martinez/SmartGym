package mx.infornet.smartgym;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AvancePerderPesoActivity extends AppCompatActivity {

    private static final String TAG = "AvancePerderPeso";
    private TextInputEditText peso;
    private MaterialButton enviar;
    private ProgressBar progressBar;
    private JsonObjectRequest request;
    private RequestQueue queue;
    private String token, token_type, tiempo, estatura, meta, peso_inicial, fecha_final;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avance_perder_peso);

        //fecha actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault());
        final String fecha_actual = df.format(calendar.getTime());

        ConexionSQLiteHelper  con1 = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
        SQLiteDatabase db1 = con1.getWritableDatabase();

        try {

            String query = "SELECT * FROM usuarios";

            Cursor cursor1 = db1.rawQuery(query, null);

            for(cursor1.moveToFirst(); !cursor1.isAfterLast(); cursor1.moveToNext()) {
                token = cursor1.getString(cursor1.getColumnIndex("token"));
                token_type = cursor1.getString(cursor1.getColumnIndex("tokenType"));
            }

            cursor1.close();

        }catch (Exception e){

            Toast toast = Toast.makeText(getApplicationContext(), "Error: "+  e.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }

        db1.close();

        StringRequest requestD = new StringRequest(Request.Method.GET, Config.GET_OBJ_PESO_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    estatura = jsonObject.getString("estatura");
                    meta = jsonObject.getString("meta");
                    tiempo = jsonObject.getString("tiempo");
                    peso_inicial = jsonObject.getString("peso_inicial");

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
                        Log.e(TAG, "Error get data: "+jsonObjectError.toString());


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

        RequestQueue queueD = Volley.newRequestQueue(this);
        queueD.add(requestD);


        queue = Volley.newRequestQueue(getApplicationContext());

        progressBar = findViewById(R.id.prog_bar_avance_perder_peso);
        peso = findViewById(R.id.peso_actual_avance_perder_peso);
        enviar = findViewById(R.id.btn_save_avance_perder_peso);

        progressBar.setVisibility(View.GONE);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String peso_actual = peso.getText().toString();

                if (TextUtils.isEmpty(peso_actual)){
                    peso.setError("Introduce tu peso actual");
                    peso.requestFocus();
                } else {

                    final double peso_decimal = Double.parseDouble(peso_actual);
                    final int tiempo_int = Integer.parseInt(tiempo);
                    final double meta_decimal = Double.parseDouble(meta);
                    final double estatura_decimal = Double.parseDouble(estatura);
                    double peso_ini_decimal = Double.parseDouble(peso_inicial);

                    JSONObject json_save = new JSONObject();

                    try {
                        json_save.put("estatura", estatura_decimal);
                        json_save.put("peso_inicial", peso_ini_decimal);
                        json_save.put("peso_actual", peso_decimal);
                        json_save.put("meta", meta_decimal);
                        json_save.put("tiempo", tiempo_int);
                        json_save.put("fecha", fecha_actual);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                    Log.w(TAG, "JSON: " +json_save );

                    progressBar.setVisibility(View.VISIBLE);

                    request = new JsonObjectRequest(Request.Method.POST, Config.POST_PERDER_PESO_URL, json_save, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            progressBar.setVisibility(View.GONE);

                            Log.w(TAG, "Response post data: "+response.toString());

                            if (response.has("message")){
                                String mensj = null;
                                try {
                                    mensj = response.getString("message");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(getApplicationContext(), mensj, Toast.LENGTH_LONG).show();


                                finish();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            progressBar.setVisibility(View.GONE);

                            if (error instanceof TimeoutError) {
                                Toast.makeText(getApplicationContext(),
                                        "Oops. Timeout error!",
                                        Toast.LENGTH_LONG).show();
                            }

                            NetworkResponse networkResponse = error.networkResponse;

                            if(networkResponse != null && networkResponse.data != null) {
                                String jsonError = new String(networkResponse.data);

                                try {
                                    JSONObject jsonObjectError = new JSONObject(jsonError);
                                    Log.e(TAG, "Error post: "+jsonObjectError.toString());

                                }catch (JSONException e){

                                }
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

                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        //se deja vacío para que al querer regresar, la activity no se cierre

    }
}
