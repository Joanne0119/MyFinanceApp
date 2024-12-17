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

    PieChart pieChart1, pieChart2;

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

        pieChart1 = findViewById(R.id.pieChart_income);
        pieChart2 = findViewById(R.id.pieChart_expend);

        ArrayList<Float> pieChart1Values = (ArrayList<Float>) getIntent().getSerializableExtra("pie_chart_1_values");
        ArrayList<Float> pieChart2Values = (ArrayList<Float>) getIntent().getSerializableExtra("pie_chart_2_values");

        // 生成第一個 PieChart
        ArrayList<PieEntry> pieEntries1 = new ArrayList<>();
        for (int i = 0; i < pieChart1Values.size(); i++) {
            pieEntries1.add(new PieEntry(pieChart1Values.get(i), "Label " + (i + 1)));
        }
        PieDataSet pieDataSet1 = new PieDataSet(pieEntries1, "Income");
        pieDataSet1.setColors(ColorTemplate.PASTEL_COLORS);
        pieChart1.setData(new PieData(pieDataSet1));
        pieChart1.animateXY(1500, 1500);
        pieChart1.getDescription().setEnabled(false);

        // 生成第二個 PieChart
        ArrayList<PieEntry> pieEntries2 = new ArrayList<>();
        for (int i = 0; i < pieChart2Values.size(); i++) {
            pieEntries2.add(new PieEntry(pieChart2Values.get(i), "Label " + (i + 1)));
        }
        PieDataSet pieDataSet2 = new PieDataSet(pieEntries2, "Expense");
        pieDataSet2.setColors(ColorTemplate.COLORFUL_COLORS);
        pieChart2.setData(new PieData(pieDataSet2));
        pieChart2.animateXY(1500, 1500);
        pieChart2.getDescription().setEnabled(false);
    }

    public void goBack(View v) {
        finish(); // 結束 Activity, 即可回到前一個 Activity
    }

}
