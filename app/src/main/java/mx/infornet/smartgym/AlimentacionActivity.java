package mx.infornet.smartgym;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlimentacionActivity extends AppCompatActivity {

    private TextView error;
    private ImageView btn_back;
    private ProgressBar progressBar;
    private String token, token_type;
    private RecyclerView recyclerView;
    private StringRequest request;
    private RequestQueue queue;
    private List<PlanesAlimentacion> alimentacionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alimentacion);

        error = findViewById(R.id.txt_error_alimentacion);
        error.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recycler_view_alimentacion);
        recyclerView.setAdapter(null);


        btn_back = findViewById(R.id.btn_back_alimentacion);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = findViewById(R.id.prog_bar_alimentacion);

        alimentacionList = new ArrayList<>();

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

        Log.d("token", token);

        queue = Volley.newRequestQueue(getApplicationContext());

        request = new StringRequest(Request.Method.GET, Config.PLANES_ALIM_GYM_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                Log.d("string response", response);

                progressBar.setVisibility(View.GONE);

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    Log.d("RESPONSE_PLAN", jsonObject.toString());

                    if (jsonObject.has("status")){
                        String status = jsonObject.getString("status");

                        if (status.equals("Token is Expired")){
                            Toast.makeText(getApplicationContext(), status+". Favor de iniciar sesión nuevamente", Toast.LENGTH_LONG).show();

                            ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
                            SQLiteDatabase db = conn.getWritableDatabase();
                            db.execSQL("DELETE FROM usuarios");

                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

                            finish();
                        }

                    } else if (jsonObject.toString().isEmpty()){

                        error.setVisibility(View.VISIBLE);
                        error.setText(R.string.erroralimentacion);

                    } else {
                        alimentacionList.add(new PlanesAlimentacion(
                                jsonObject.getInt("id"),
                                jsonObject.getString("nombre"),
                                jsonObject.getString("descripcion")
                        ));

                        AdapterAlimentacion adaper = new AdapterAlimentacion(getApplicationContext(), alimentacionList);
                        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                        llm.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(llm);
                        recyclerView.setAdapter(adaper);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);

                Log.d("ERROR", error.toString());

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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
