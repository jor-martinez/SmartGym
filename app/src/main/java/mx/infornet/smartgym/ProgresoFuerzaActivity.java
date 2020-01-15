package mx.infornet.smartgym;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProgresoFuerzaActivity extends AppCompatActivity {

    private static final String TAG = "ProgresoFuerzaActivity";
    private String token_type, token;
    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso_fuerza);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_prog_fuerza);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Progreso");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        StringRequest request = new StringRequest(Request.Method.GET, Config.GET_OBJ_FUERZA_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.w(TAG, "onResponse get data: "+ response );

                try {
                    jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 1){

                        LineChart lineChart = findViewById(R.id.linechart_fuerza);
                        lineChart.getDescription().setText("Objetivo incrementar fuerza");
                        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

                        XAxis xAxis = lineChart.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);

                        int pechoCount = 0;
                        int espaldaCount = 0;
                        int piernaCount = 0;
                        int bicepsCount = 0;
                        int tricepsCount = 0;
                        int abdomenCount = 0;

                        for(int i=0;i<jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String musculo = jsonObject.getString("musculo");

                            switch (musculo){
                                case "Pecho":
                                    pechoCount+=1;
                                    break;
                                case "Espalda":
                                    espaldaCount+=1;
                                    break;
                                case "Biceps":
                                    bicepsCount+=1;
                                    break;
                                case "Triceps":
                                    tricepsCount+=1;
                                    break;
                                case "Abdomen":
                                    abdomenCount+=1;
                                    break;
                                case "Pierna":
                                    piernaCount+=1;
                                    break;
                            }

                        }

                        Log.w(TAG, "Count pecho: " + pechoCount );
                        Log.w(TAG, "Count espalda: " + espaldaCount );
                        Log.w(TAG, "Count biceps: " + bicepsCount );
                        Log.w(TAG, "Count triceps: " + tricepsCount );
                        Log.w(TAG, "Count abdomen: " + abdomenCount );
                        Log.w(TAG, "Count pierna: " + piernaCount );


                        //Cursor cp = db.rawQuery("SELECT * FROM objetivo_fuerza WHERE musculo = ?", new String[]{"Pecho"});

                        if (pechoCount > 1){

                            LineDataSet dataSet = new LineDataSet(getDataPecho(), "Pecho");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), R.color.rojomaterial);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[pechoCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int) value];
                                }
                            };

                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);
                        }



                        //Cursor ce = db.rawQuery("SELECT * FROM objetivo_fuerza WHERE musculo = ?", new String[]{"Espalda"});

                        if (espaldaCount > 1){

                            LineDataSet dataSet = new LineDataSet(getDataEspalda(), "Espalda");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), R.color.azulmaterial);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[espaldaCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int)value];
                                }
                            };


                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);

                        }


                        if (bicepsCount > 1){

                            LineDataSet dataSet = new LineDataSet(getDataBiceps(), "Biceps");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), R.color.verdematerial);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[bicepsCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int) value];
                                }
                            };

                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);

                        }


                        if (tricepsCount>1){
                            LineDataSet dataSet = new LineDataSet(getDataTriceps(), "Triceps");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), R.color.amarillomaterial);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[tricepsCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int) value];
                                }
                            };

                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);
                        }


                        if (abdomenCount > 1){
                            LineDataSet dataSet = new LineDataSet(getDataAbdomen(), "Abdomen");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), android.R.color.holo_purple);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[abdomenCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int) value];
                                }
                            };

                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);
                        }


                        if (piernaCount > 1){
                            LineDataSet dataSet = new LineDataSet(getDataPierna(), "Pierna");
                            dataSet.setLineWidth(5f);
                            dataSet.setCircleRadius(5f);
                            dataSet.setValueTextSize(15f);
                            int colorr = ContextCompat.getColor(getApplicationContext(), R.color.naranjamaterial);
                            dataSet.setColor(colorr);

                            final String[] tiempo = new String[piernaCount];

                            for (int i=0; i<tiempo.length;i++){
                                tiempo[i] = "Registro "+ i;
                                //System.out.println(tiempo[i]);
                            }

                            /*ValueFormatter formatter = new ValueFormatter(){
                                @Override
                                public String getAxisLabel(float value, AxisBase axis) {
                                    return tiempo[(int) value];
                                }
                            };

                            xAxis.setValueFormatter(formatter);*/

                            dataSets.add(dataSet);
                        }


                        YAxis yAxisRight = lineChart.getAxisRight();
                        yAxisRight.setEnabled(false);

                        YAxis yAxisLeft = lineChart.getAxisLeft();
                        yAxisLeft.setGranularity(5f);
                        yAxisLeft.setAxisMinimum(20f);
                        //yAxisLeft.setAxisMaximum(100f);

                        LineData data = new LineData(dataSets);
                        lineChart.setData(data);
                        lineChart.animateX(3500);
                        lineChart.invalidate();


                    } else {

                        final Dialog dialog = new Dialog(ProgresoFuerzaActivity.this);
                        dialog.setContentView(R.layout.alert_info_layout);
                        dialog.setCancelable(false);
                        TextView mensaje = dialog.findViewById(R.id.mensaje_info);
                        TextView btnok = dialog.findViewById(R.id.positive_info);
                        TextView btncancel = dialog.findViewById(R.id.neutral_btn_info);
                        btncancel.setVisibility(View.GONE);
                        mensaje.setText(R.string.mensaje_info_nodata);
                        btnok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.show();

                        //Toast.makeText(getApplicationContext(), "Datos insuficientes", Toast.LENGTH_LONG).show();
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
                        Log.w(TAG, "Error get data: "+jsonObjectError.toString());


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

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);


    }

    private ArrayList getDataPecho(){
        ArrayList<Entry> entriesPecho = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Pecho")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesPecho.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }


        return entriesPecho;
    }

    private ArrayList getDataEspalda(){
        ArrayList<Entry> entriesEsp = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Espalda")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesEsp.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entriesEsp;
    }

    private ArrayList getDataBiceps(){
        ArrayList<Entry> entriesBic = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Biceps")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesBic.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entriesBic;
    }

    private ArrayList getDataTriceps(){
        ArrayList<Entry> entriesTric = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Triceps")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesTric.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entriesTric;
    }

    private ArrayList getDataAbdomen(){
        ArrayList<Entry> entriesAbd = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Abdomen")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesAbd.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entriesAbd;
    }

    private ArrayList getDataPierna(){
        ArrayList<Entry> entriesPierna = new ArrayList<>();

        try {

            float cont = 0;

            for (int i= 0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String musculo = jsonObject.getString("musculo");

                if (musculo.equals("Pierna")){
                    float peso = Float.valueOf(jsonObject.getString("peso_levantado"));
                    int repeticiones = Integer.parseInt(jsonObject.getString("repeticiones"));
                    float fuerza = (0.033f * repeticiones * peso) + peso;
                    entriesPierna.add(new Entry(cont, fuerza));
                    cont++;
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entriesPierna;
    }
}
