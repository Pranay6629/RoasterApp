package com.monu.rbflightapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monu.rbflightapp.adapter.RoasterAdapter;
import com.monu.rbflightapp.dao.RoasterDao;
import com.monu.rbflightapp.database.DatabaseClient;
import com.monu.rbflightapp.entitty.Roaster;
import com.monu.rbflightapp.pojo.RoasterPojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String FETCHURL = "https://rosterbuster.aero/wp-content/uploads/dummy-response.json";
    private RecyclerView recyclerview;
    List<RoasterPojo> roasetr;
    private ArrayList<RoasterPojo> arrayList;
    private RoasterAdapter roasterAdapter;
    private RoasterDao roasterDao;
    private ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerview = findViewById(R.id.recyclerview);
        arrayList = new ArrayList<>();
        roasetr = new ArrayList<>();
//        roasterAdapter = new RoasterAdapter(this, arrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.setItemAnimator(new DefaultItemAnimator());
        recyclerview.setNestedScrollingEnabled(false);
//        recyclerview.setAdapter(roasterAdapter);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting() && arrayList != null) {
            fetchfromServer();
        } else {


            fetchfromRoom();
        }

        swipeRefreshLayout = findViewById(R.id.refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        if (networkInfo != null && networkInfo.isConnectedOrConnecting() && arrayList != null) {
            fetchfromServer();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            fetchfromRoom();
            Toast.makeText(MainActivity.this, "No Network is available", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void fetchfromRoom() {

        class GetTasks extends AsyncTask<Void, Void, List<Roaster>> {

            @Override
            protected List<Roaster> doInBackground(Void... voids) {

                List<Roaster> roasterList = DatabaseClient.getInstance(getApplicationContext()).getRoasterDatabase().roasterDao().getAll();
                arrayList.clear();
                for (Roaster roaster : roasterList){
                    RoasterPojo pojo = new RoasterPojo(roaster.getFlightnr(),
                            roaster.getDate(),
                            roaster.getAircraft_Type(),
                            roaster.getTail(),
                            roaster.getDeparture(),
                            roaster.getDestination(),
                            roaster.getTime_Depart(),
                            roaster.getTime_Arrive(),
                            roaster.getDutyID(),
                            roaster.getDutyCode(),
                            roaster.getCaptain(),
                            roaster.getFirst_Officer(),
                            roaster.getFlight_Attendant());

                    arrayList.add(pojo);

                }
                return roasterList;
            }

            @Override
            protected void onPostExecute(List<Roaster> roasters) {
                super.onPostExecute(roasters);

                roasterAdapter = new RoasterAdapter(getApplicationContext(), arrayList);
                recyclerview.setAdapter(roasterAdapter);
                recyclerview.hasFixedSize();
                roasterAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        }
            GetTasks gt = new GetTasks();
            gt.execute();

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Roaster> roasterList = DatabaseClient.getInstance(MainActivity.this).getRoasterDatabase().roasterDao().getAll();
//                arrayList.clear();
//                for (Roaster roaster : roasterList){
//                    RoasterPojo pojo = new RoasterPojo(roaster.getFlightnr(),
//                            roaster.getDate(),
//                            roaster.getAircraft_Type(),
//                            roaster.getTail(),
//                            roaster.getDeparture(),
//                            roaster.getDestination(),
//                            roaster.getTime_Depart(),
//                            roaster.getTime_Arrive(),
//                            roaster.getDutyID(),
//                            roaster.getDutyCode(),
//                            roaster.getCaptain(),
//                            roaster.getFirst_Officer(),
//                            roaster.getFlight_Attendant());
//
//                    arrayList.add(pojo);
//
//                }
//                roasterAdapter = new RoasterAdapter(getApplicationContext(), arrayList);
////                recyclerview.setAdapter(roasterAdapter);
//                // refreshing recycler view
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        roasterAdapter.notifyDataSetChanged();
//                    }
//                });
//
//            }
//        });
//        thread.start();
    }

    private void fetchfromServer() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, FETCHURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println("Response is ====>" + response.toString());

                try {
                    arrayList = new ArrayList<>();
                    if ("null".equals(String.valueOf(response))){

                    }else {
                        JSONArray array = new JSONArray(response);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject o = array.getJSONObject(i);
                            RoasterPojo pojo = new RoasterPojo(
                                    o.getString("Flightnr"),
                                    o.getString("Date"),
                                    o.getString("Aircraft Type"),
                                    o.getString("Tail"),
                                    o.getString("Departure"),
                                    o.getString("Destination"),
                                    o.getString("Time_Depart"),
                                    o.getString("Time_Arrive"),
                                    o.getString("DutyID"),
                                    o.getString("DutyCode"),
                                    o.getString("Captain"),
                                    o.getString("First Officer"),
                                    o.getString("Flight Attendant")
                            );
                            arrayList.add(pojo);

                        }
                        saveTask();
                    }

                        roasterAdapter = new RoasterAdapter(getApplicationContext(), arrayList);
                        recyclerview.setAdapter(roasterAdapter);
                        progressBar.setVisibility(View.GONE);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void saveTask(){
        class SaveTask extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                //arrayList.clear();
                for (int i = 0; i < arrayList.size(); i++){
                    Roaster roaster = new Roaster();
                    roaster.setFlightnr(arrayList.get(i).getFlightnr());
                    roaster.setDate(arrayList.get(i).getDate());
                    roaster.setAircraft_Type(arrayList.get(i).getAircraft_Type());
                    roaster.setTail(arrayList.get(i).getTail());
                    roaster.setDeparture(arrayList.get(i).getDeparture());
                    roaster.setDestination(arrayList.get(i).getDestination());
                    roaster.setTime_Depart(arrayList.get(i).getTime_Depart());
                    roaster.setTime_Arrive(arrayList.get(i).getTime_Arrive());
                    roaster.setDutyID(arrayList.get(i).getDutyID());
                    roaster.setDutyCode(arrayList.get(i).getDutyCode());
                    roaster.setCaptain(arrayList.get(i).getCaptain());
                    roaster.setFirst_Officer(arrayList.get(i).getFirst_Officer());
                    roaster.setFlight_Attendant(arrayList.get(i).getFlight_Attendant());
                    DatabaseClient.getInstance(getApplicationContext()).getRoasterDatabase().roasterDao().insert(roaster);
                }

                    return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        }

        SaveTask st = new SaveTask();
            st.execute();
    }
    

}