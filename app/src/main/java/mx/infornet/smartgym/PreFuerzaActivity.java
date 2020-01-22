package mx.infornet.smartgym;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

public class PreFuerzaActivity extends AppCompatActivity {

    private static final String TAG = "PreFuerzaActivity";
    private TextInputEditText peso, repeticiones, tiempo;
    private JsonObjectRequest request;
    private RequestQueue queue;
    private String token, token_type;
    private Spinner musculos;
    private Dialog dialog_ok_fuerza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_fuerza);

        queue = Volley.newRequestQueue(getApplicationContext());

        dialog_ok_fuerza = new Dialog(this);

        ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {

            String query = "SELECT * FROM usuarios";

            Cursor cursor = db.rawQuery(query, null);

            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("tokenType"));
            }

            cursor.close();

        }catch (Exception e){

            Toast toast = Toast.makeText(getApplicationContext(), "Error: "+  e.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
        db.close();

        Log.d(TAG, "token: "+token);

        peso = findViewById(R.id.peso_ini_levantado);
        repeticiones = findViewById(R.id.repeticiones_ini);
        musculos = findViewById(R.id.sp_musculos);
        tiempo = findViewById(R.id.tiempo_masa);

        final ProgressBar progressBar = findViewById(R.id.prog_bar_fuerza_init);
        progressBar.setVisibility(View.GONE);

        MaterialButton guardar = findViewById(R.id.btn_save_fuerza_init);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String peso_levantado = peso.getText().toString();
                String num_repeticiones = repeticiones.getText().toString();
                String time = tiempo.getText().toString();

                if (TextUtils.isEmpty(time)){
                    tiempo.setError("Introduce el tiempo de tu objetivo");
                    tiempo.requestFocus();
                } else if (TextUtils.isEmpty(peso_levantado)){
                    peso.setError("Introduce el peso levantado");
                    peso.requestFocus();
                } else if (TextUtils.isEmpty(num_repeticiones)){
                    repeticiones.setError("Introduce el número de repeticiones");
                    repeticiones.requestFocus();
                } else {

                    final double peso_decimal = Double.parseDouble(peso_levantado);
                    final int repeticiones_int = Integer.parseInt(num_repeticiones);
                    final int tiempo_int = Integer.parseInt(time);
                    final String musculo = musculos.getSelectedItem().toString();

                    progressBar.setVisibility(View.VISIBLE);

                    JSONObject params_masa = new JSONObject();

                    try {
                        params_masa.put("peso_levantado", peso_decimal);
                        params_masa.put("repeticiones", repeticiones_int);
                        params_masa.put("musculo", musculo);
                        params_masa.put("tiempo", tiempo_int);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }

                    request = new JsonObjectRequest(Request.Method.POST, Config.POST_FUERZA_URL, params_masa, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progressBar.setVisibility(View.GONE);

                            Log.w(TAG, "Response post: "+response.toString());

                            if (response.has("message")){

                                String mensj = null;
                                try {
                                    mensj = response.getString("message");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getApplicationContext(), mensj, Toast.LENGTH_LONG).show();


                                ShowOkFuerza(tiempo_int,peso_decimal,repeticiones_int);
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

                            if(networkResponse != null && networkResponse.data != null){
                                String jsonError = new String(networkResponse.data);
                                try {
                                    JSONObject jsonObjectError = new JSONObject(jsonError);
                                    Log.e(TAG, "Error post: "+jsonObjectError.toString());

                                    if (jsonObjectError.has("message")){
                                        String err = jsonObjectError.getString("message");
                                        if (err.equals("The given data was invalid.")){
                                            JSONObject errors = jsonObjectError.getJSONObject("errors");

                                            if (errors.has("peso_levantado")){

                                                JSONArray err_peso = errors.getJSONArray("peso_levantado");

                                                StringBuilder stringBuilder = new StringBuilder();

                                                for (int i=0; i<err_peso.length(); i++){
                                                    String valor = err_peso.getString(i);
                                                    stringBuilder.append(valor+"\n");
                                                }
                                                peso.setError(stringBuilder, null);
                                                peso.requestFocus();
                                            } else if (errors.has("repeticiones")){
                                                JSONArray err_repet = errors.getJSONArray("repeticiones");
                                                StringBuilder stringBuilder = new StringBuilder();

                                                for (int i=0; i<err_repet.length(); i++){
                                                    String valor = err_repet.getString(i);
                                                    stringBuilder.append(valor+"\n");
                                                }
                                                repeticiones.setError(stringBuilder, null);
                                                repeticiones.requestFocus();
                                            }
                                        }
                                    }

                                }catch (JSONException e){
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

                    queue.add(request);
                }

            }
        });

    }

    private void ShowOkFuerza(int tiempo, double peso, int repeticiones){

        ImageView close;
        TextView tv_fuerza, tv_termino, tv_inicio, tv_pedir_datos;
        MaterialButton btn;

        dialog_ok_fuerza.setContentView(R.layout.info_fuerza_layout);

        close = dialog_ok_fuerza.findViewById(R.id.btn_close_info_masa_muscular);
        tv_fuerza = dialog_ok_fuerza.findViewById(R.id.tv_fuerza_info_masa_muscular);
        tv_termino = dialog_ok_fuerza.findViewById(R.id.tv_termino_info_masa_muscular);
        tv_inicio = dialog_ok_fuerza.findViewById(R.id.tv_inicio_info_masa_muscular);
        tv_pedir_datos = dialog_ok_fuerza.findViewById(R.id.tv_pedir_datos_info_masa_muscular);
        btn = dialog_ok_fuerza.findViewById(R.id.btn_ok_info_masa);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ok_fuerza.dismiss();

                startActivity(new Intent(PreFuerzaActivity.this, MainActivity.class));
                finish();

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_ok_fuerza.dismiss();

                startActivity(new Intent(PreFuerzaActivity.this, MainActivity.class));
                finish();
            }
        });

        //se obtiene la fecha de inicio que es la actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fecha_actual = df.format(calendar.getTime());

        String ultimo = "Puedes entrar a la sección de progreso para ir registrando tus avances";

        //formula para calcular 1RM (fuerza)
        double fuerza = (0.033 * repeticiones * peso) + peso;
        String fuerza_kg = fuerza + " Kg";


        switch (tiempo){
            case 1:
                //se obtiene la fecha de un mes despues
                Calendar calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.MONTH, 1);
                SimpleDateFormat dff = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fecha_final = dff.format(calendar1.getTime());
                tv_termino.setText(fecha_final);
                break;
            case 2:
                //se obtiene la fecha de un mes despues
                calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.MONTH, 2);
                SimpleDateFormat dff2 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fecha_final2 = dff2.format(calendar1.getTime());

                tv_termino.setText(fecha_final2);

                break;
            case 3:
                //se obtiene la fecha de un mes despues
                calendar1 = Calendar.getInstance();
                calendar1.add(Calendar.MONTH, 3);
                SimpleDateFormat dff3 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String fecha_final3 = dff3.format(calendar1.getTime());
                tv_termino.setText(fecha_final3);
                break;
        }

        tv_pedir_datos.setText(ultimo);
        tv_inicio.setText(fecha_actual);
        tv_fuerza.setText(fuerza_kg);

        dialog_ok_fuerza.setCancelable(false);
        dialog_ok_fuerza.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialog_ok_fuerza.show();
    }
}
