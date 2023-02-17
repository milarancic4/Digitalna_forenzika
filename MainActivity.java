package com.example.test;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.test.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    Map<String, List<Sms>> allSms = new HashMap<String, List<Sms>>();
    Button findContactButton;
    Button drawGraphButton;

    Button findMostCommonSmsLengthButton;
    int numOfExchangedessages = 0;

    protected void onCreate(Bundle savedInstanceState) {

        final int REQUEST_CODE_ASK_PERMISSIONS = 123;
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), "Permission granted",Toast.LENGTH_SHORT ).show();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loadButton =(Button) findViewById(R.id.loadButton);
        loadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadAllSms();
            }
        });
        findContactButton =(Button) findViewById(R.id.findContactButton);
        findContactButton.setEnabled(false);
        findContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String contact = findContact(allSms);
                TextView textViewResult =(TextView) findViewById(R.id.findContactTextViewResult);
                textViewResult.setText("Number of contact is: "+contact+". \n"+numOfExchangedessages+" messages were exchanged.");
            }
        });
        drawGraphButton =(Button) findViewById(R.id.drawGraphButton);
        drawGraphButton.setEnabled(false);
        drawGraphButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onDrawGraph(allSms);
            }
        });
        findMostCommonSmsLengthButton =(Button) findViewById(R.id.findMostCommonSmsLengthButton);
        findMostCommonSmsLengthButton.setEnabled(false);
        findMostCommonSmsLengthButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int messageLen = findMostCommonSmsLength(allSms.get("sent"));
                TextView textViewResult =(TextView) findViewById(R.id.findMostCommonSmsLengthTextViewResult);
                textViewResult.setText("The messages I type most often have "+messageLen+" characters.");
            }
        });
    }
    private ArrayList getEntriesForNumSms(List<Sms> sms) {
        // creating a new array list
        ArrayList barEntriesArrayList = new ArrayList<>();

        HashMap<Integer,Integer> months=new HashMap<Integer,Integer>();
        for(int i = 0; i < 12; i++) {
            months.put(i, 0);
        }
        for(int i = 0; i < sms.size(); i++){
            long time =Long.parseLong(sms.get(i).getTime());

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((long)time);
                SimpleDateFormat df1 = new SimpleDateFormat("MM");
                String monthString =df1.format(calendar.getTime());
                int month = Integer.parseInt(monthString);
                month = month-1;
                Integer value = months.get(month) + 1;
                months.put(month , value);
        }
        for(Map.Entry<Integer, Integer> map  :  months.entrySet() ) {
            barEntriesArrayList.add(new BarEntry(new Float(map.getKey()), new Float(map.getValue())));
        }
        return barEntriesArrayList;
    }
    public void onDrawGraph(Map<String, List<Sms>> sms){
        String[] months = {"Jan", "Feb", "Mar", "Apr", "Maj", "Jun","Jul","Avg","Sept","Okt","Nov","Dec"};
        HashMap<Integer,Integer> smsLengths = getSmsLength(sms.get("sent"));
        drawGraph(months,getEntriesForNumSms(sms.get("sent")));
    }
    public int findMostCommonSmsLength(List<Sms> sms){
        HashMap<Integer,Integer> smsLengths = getSmsLength(sms);

        int maxValueInMap=(Collections.max(smsLengths.values()));  // This will return max value in the HashMap
        int key = Collections.max(smsLengths.entrySet(), Map.Entry.comparingByValue()).getKey();

        return key;
    }
    private HashMap<Integer,Integer> getSmsLength(List<Sms> sms) {
        HashMap<Integer,Integer> smsLen=new HashMap<Integer,Integer>();
        for(int i=0;i<sms.size();i++){
            String message = sms.get(i).getMsg();
            int messageLen = message.length();
            boolean isKeyPresent = smsLen.containsKey(messageLen);
            int value =isKeyPresent ? (smsLen.get(messageLen) + 1) : 1;
            smsLen.put(messageLen,value);
        }
        return smsLen;
    }
    public String findContact(Map<String, List<Sms>> allSms){
        HashMap<String,Integer> contactDictionary=new HashMap<String,Integer>();
        List<Sms> inbox = allSms.get("inbox");
        List<Sms> sent = allSms.get("sent");

        for(int i=0; i < inbox.size(); i++){
            String contact = inbox.get(i).getAddress();
            boolean isKeyPresent = contactDictionary.containsKey(contact);
            int value =isKeyPresent ? (contactDictionary.get(contact) + 1) : 1;
            contactDictionary.put(contact,value);
        }
        for(int i=0; i < sent.size(); i++){
            String contact = sent.get(i).getAddress();
            boolean isKeyPresent = contactDictionary.containsKey(contact);
            int value =isKeyPresent ? (contactDictionary.get(contact) + 1) : 1;
            contactDictionary.put(contact,value);
        }
        int maxValueInMap=(Collections.max(contactDictionary.values()));  // This will return max value in the HashMap
        numOfExchangedessages=maxValueInMap;
        String key = Collections.max(contactDictionary.entrySet(), Map.Entry.comparingByValue()).getKey();

        return key;
    }
    @SuppressLint("Range")
    public void loadAllSms() {
        List<Sms> inbox = new ArrayList<Sms>();
        List<Sms> sent = new ArrayList<Sms>();

        Sms objSms;
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = getContentResolver();

        String[] projection = new String[]{"_id","address","body","date","type"};
        String searchQuery = "date >= " +1641042001000L+ " and date < "+ 1672534800000L;

        Cursor c = cr.query(message, projection,  searchQuery, null, "date ASC");

        int totalSMS = c.getCount();
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                    inbox.add(objSms);
                } else {
                    objSms.setFolderName("sent");
                    sent.add(objSms);
                }
                c.moveToNext();
            }
            allSms.put("inbox",inbox);
            allSms.put("sent",sent);
            findContactButton.setEnabled(true);
            drawGraphButton.setEnabled(true);
            findMostCommonSmsLengthButton.setEnabled(true);
            Toast.makeText(getBaseContext(), "All sms loaded",Toast.LENGTH_SHORT ).show();
        }
        // Closes the Cursor, releasing all of its resources and making it completely invalid.
        c.close();
    }
    public void drawGraph(String[] xAxis,ArrayList entries){

        BarChart mChart = (BarChart) findViewById(R.id.chart);

        XAxis xaxis = mChart.getXAxis();
        xaxis.setDrawGridLines(false);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setDrawLabels(true);
        xaxis.setDrawAxisLine(false);
        xaxis.setValueFormatter(new IndexAxisValueFormatter(xAxis));
        xaxis.setTextColor(Color.WHITE);
        mChart.getXAxis().setLabelCount(xAxis.length);

        YAxis yAxisLeft = mChart.getAxisLeft();
        yAxisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxisLeft.setTextColor(Color.WHITE);
        mChart.getAxisRight().setEnabled(false);

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        Description description = mChart.getDescription();
        description.setEnabled(false);

        List<IBarDataSet> dataSets = new ArrayList<>();

        BarDataSet barDataSet = new BarDataSet(entries, " ");
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);

        mChart.setData(data);
        mChart.invalidate();

    }
    public class Sms{
        private String _id;
        private String _address;
        private String _msg;
        private String _readState; //"0" for have not read sms and "1" for have read sms
        private String _time;
        private String _folderName;

        public String getId(){
            return _id;
        }
        public String getAddress(){
            return _address;
        }
        public String getMsg(){
            return _msg;
        }
        public String getReadState(){
            return _readState;
        }
        public String getTime(){
            return _time;
        }
        public String getFolderName(){
            return _folderName;
        }


        public void setId(String id){
            _id = id;
        }
        public void setAddress(String address){
            _address = address;
        }
        public void setMsg(String msg){
            _msg = msg;
        }
        public void setReadState(String readState){
            _readState = readState;
        }
        public void setTime(String time){
            _time = time;
        }
        public void setFolderName(String folderName){
            _folderName = folderName;
        }

    }
}

