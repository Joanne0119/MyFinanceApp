package com.example.myfinanceapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {

    BarChart barChart;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        Button goBackButton = findViewById(R.id.button_back);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack(v);
            }
        });

        barChart = findViewById(R.id.barchart);
        pieChart = findViewById(R.id.piechart);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for(int i = 1; i < 10; i++){
            float value = (float) (i * 10.0);
            BarEntry barEntry = new BarEntry(i, value);
            PieEntry pieEntry = new PieEntry(i, value);
            barEntries.add(barEntry);
            pieEntries.add(pieEntry);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Employees");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setDrawValues(false);
        barChart.setData(new BarData(barDataSet));
        barChart.animateY(1500);
        barChart.getDescription().setText("Employees Chart");
        barChart.getDescription().setTextColor(Color.BLUE);

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Students");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieChart.setData(new PieData(pieDataSet));
        pieChart.animateXY(1500, 1500);
        pieChart.getDescription().setEnabled(false);
    }

    public void goBack(View v) {
        finish(); // 結束 Activity, 即可回到前一個 Activity
    }

}
