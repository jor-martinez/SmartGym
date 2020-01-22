package mx.infornet.smartgym;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.LogWriter;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProgresoPerderPesoActivity extends AppCompatActivity {

    private static final String TAG = "ProgresoPerderPeso";
    private String token, token_type;
    private JSONArray jsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso_perder_peso);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_prog_peso);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Progreso");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConexionSQLiteHelper  conn = new ConexionSQLiteHelper(getApplicationContext(), "usuarios", null, 4);
        SQLiteDatabase db = conn.getWritableDatabase();

        try {

            String query = "SELECT * FROM usuarios";

            Cursor cursor = db.rawQuery(query, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                token = cursor.getString(cursor.getColumnIndex("token"));
                token_type = cursor.getString(cursor.getColumnIndex("tokenType"));
            }

            cursor.close();

        }catch (Exception e){

            Toast toast = Toast.makeText(getApplicationContext(), "Error: "+  e.toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
        db.close();

        Log.w(TAG, "token: "+token);

        //hace la consulta a la base de datos
        StringRequest request = new StringRequest(Request.Method.GET, Config.GET_OBJ_PESO_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.w(TAG, "Response get data: "+ response );

                try {
                    jsonArray = new JSONArray(response);

                    if (jsonArray.length() > 1){

                        //grafica de puntos, progreso peso
                        LineChart lineChart = findViewById(R.id.chart_peso);
                        lineChart.getDescription().setText("Peso perdido");

                        LineDataSet dataSet = new LineDataSet(getData(), "Peso perdido");

                        dataSet.setColor(R.color.colorPrimary);
                        dataSet.setValueTextColor(R.color.colorPrimaryDark);
                        dataSet.setLineWidth(5f);
                        dataSet.setCircleRadius(5f);
                        dataSet.setValueTextSize(15f);

                        XAxis xAxis = lineChart.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


                        final String[] tiempo = new String[jsonArray.length()];

                        for (int i=0; i<tiempo.length;i++){
                            tiempo[i] = "Sem "+ i;
                            Log.w(TAG, "array tiempo: "+ tiempo[i]);
                        }

                        ValueFormatter formatter = new ValueFormatter(){
                            @Override
                            public String getAxisLabel(float value, AxisBase axis) {
                                return tiempo[(int) value];
                            }
                        };

                        xAxis.setGranularity(1f);
                        xAxis.setValueFormatter(formatter);


                        YAxis yAxisRight = lineChart.getAxisRight();
                        yAxisRight.setEnabled(false);

                        YAxis yAxisLeft = lineChart.getAxisLeft();
                        yAxisLeft.setGranularity(1f);
                        yAxisLeft.setAxisMinimum(40f);

                        LineData data = new LineData(dataSet);
                        lineChart.setData(data);
                        lineChart.animateX(2000);
                        lineChart.invalidate();

                        //Grafica de barras para ver el IMC

                        BarChart barChart = findViewById(R.id.barchart_peso);
                        barChart.getDescription().setText("IMC");
                        BarDataSet barDataSet = new BarDataSet(getDataBar(), "Indice de masa corporal");
                        barDataSet.setBarBorderWidth(0.9f);
                        barDataSet.setValueTextSize(15f);

                        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                        int colorr = ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
                        barDataSet.setColor(colorr);

                        BarData barData = new BarData(barDataSet);
                        barData.setBarWidth(0.5f);

                        LimitLine ll1 = new LimitLine(18.5f, "normal");
                        ll1.setLineWidth(4f);
                        ll1.enableDashedLine(10f, 10f, 0f);
                        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                        ll1.setTextSize(10f);
                        LimitLine ll2 = new LimitLine(25.0f, "Peso superior al normal");
                        ll2.setLineWidth(4f);
                        ll2.enableDashedLine(10f, 10f, 0f);
                        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                        ll2.setTextSize(10f);
                        LimitLine ll3 = new LimitLine(30.0f, "Obesidad");
                        ll3.setLineWidth(4f);
                        ll3.enableDashedLine(10f, 10f, 0f);
                        ll3.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                        ll3.setTextSize(10f);

                        YAxis leftAxis = barChart.getAxisLeft();
                        leftAxis.setAxisMinimum(10f);
                        leftAxis.setAxisMaximum(45f);
                        leftAxis.removeAllLimitLines();
                        leftAxis.addLimitLine(ll1);
                        leftAxis.addLimitLine(ll2);
                        leftAxis.addLimitLine(ll3);

                        YAxis yAxisRightb = barChart.getAxisRight();
                        yAxisRightb.setEnabled(false);

                        XAxis xAxisb = barChart.getXAxis();
                        xAxisb.setPosition(XAxis.XAxisPosition.BOTTOM);
                        final String[] prog = new String[]{"Inicio", "Fin"};
                        IndexAxisValueFormatter formatterb = new IndexAxisValueFormatter(prog);
                        xAxisb.setGranularity(1f);
                        xAxisb.setValueFormatter(formatterb);
                        barChart.setData(barData);
                        barChart.setFitBars(true);
                        barChart.animateXY(5000, 5000);
                        barChart.invalidate();


                    } else {

                        final Dialog dialog = new Dialog(ProgresoPerderPesoActivity.this);
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
                                onBackPressed();
                            }
                        });
                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        dialog.show();
                    }

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
                        Log.w(TAG, "Error get data: "+jsonObjectError.toString());


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

    private ArrayList getDataBar(){

        ArrayList<BarEntry> entries = new ArrayList<>();

        try {
            JSONObject inicialData = jsonArray.getJSONObject(0);

            float peso_inicial = Float.valueOf(inicialData.getString("peso_inicial"));
            float estatura_inicial = Float.valueOf(inicialData.getString("estatura"));

            float imc_ini = peso_inicial / (estatura_inicial*estatura_inicial);

            entries.add(new BarEntry(0,imc_ini));

            JSONObject finalData = jsonArray.getJSONObject(jsonArray.length()-1);

            float peso_final = Float.valueOf(finalData.getString("peso_actual"));
            float estatura_final = Float.valueOf(finalData.getString("estatura"));
            float imc_fin = peso_final / (estatura_final*estatura_final);

            entries.add(new BarEntry(1, imc_fin));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private ArrayList getData(){
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            for (int i=0; i<jsonArray.length();i++){

                if (i==0){
                    JSONObject primer = jsonArray.getJSONObject(i);
                    float peso = Float.valueOf(primer.getString("peso_inicial"));
                    entries.add(new Entry(i, peso));
                } else {
                    JSONObject losDemas = jsonArray.getJSONObject(i);
                    float peso = Float.valueOf(losDemas.getString("peso_actual"));
                    entries.add(new Entry(i, peso));
                }

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return entries;
    }

}
