package com.example.myfinanceapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Spinner spnCategory;
    private EditText edtAmount;
    private TextView txvTotalIncome, txvTotalExpense, txvTotalBalance;
    private Button btnConfirm;
    private GridLayout incomeButtons, expenseButtons;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private double totalBalance = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spnCategory = findViewById(R.id.spnCategory);
        edtAmount = findViewById(R.id.edtAmount);
        txvTotalIncome = findViewById(R.id.txvTotalIncome);
        txvTotalExpense = findViewById(R.id.txvTotalExpense);
        txvTotalBalance = findViewById(R.id.txvTotalBalance);
        btnConfirm = findViewById(R.id.btnConfirm);
        incomeButtons = findViewById(R.id.incomeButtons);
        expenseButtons = findViewById(R.id.expenseButtons);

        Button[] incomeBtns = {
                findViewById(R.id.btnSalary),
                findViewById(R.id.btnBonus),
                findViewById(R.id.btnInterest),
                findViewById(R.id.btnGift),
                findViewById(R.id.btnAllowance),
                findViewById(R.id.btninOther)
        };

        Button[] expenseBtns = {
                findViewById(R.id.btnFood),
                findViewById(R.id.btnHousehold),
                findViewById(R.id.btnClothing),
                findViewById(R.id.btnTravel),
                findViewById(R.id.btnInvestment),
                findViewById(R.id.btnoutOther)
        };

        // 處理收入按鈕選中狀態
        for (Button btn : incomeBtns) {
            btn.setOnClickListener(v -> {
                clearButtonSelections(incomeBtns); // 取消其他按鈕的選中狀態
                btn.setSelected(true);             // 設置當前按鈕為選中狀態
            });
        }

        // 處理支出按鈕選中狀態
        for (Button btn : expenseBtns) {
            btn.setOnClickListener(v -> {
                clearButtonSelections(expenseBtns); // 取消其他按鈕的選中狀態
                btn.setSelected(true);              // 設置當前按鈕為選中狀態
            });
        }


        spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String category = spnCategory.getSelectedItem().toString();
                if (category.contains("收入")) {
                    incomeButtons.setVisibility(View.VISIBLE);
                    expenseButtons.setVisibility(View.GONE);
                } else if (category.contains("支出")) {
                    incomeButtons.setVisibility(View.GONE);
                    expenseButtons.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        btnConfirm.setOnClickListener(v -> updateTotals());
    }

    private void clearButtonSelections(Button[] buttons) {
        for (Button btn : buttons) {
            btn.setSelected(false);
        }
    }

    private void updateTotals() {
        String amountText = edtAmount.getText().toString();
        double amount;

        // 確保金額輸入框非空，且轉換為 double
        if (!amountText.isEmpty()) {
            amount = Double.parseDouble(amountText);

            // 判斷類別是收入還是支出
            String category = spnCategory.getSelectedItem().toString();
            if (category.equals("收入")) {
                totalIncome += amount;
                totalBalance += amount;
            } else {
                totalExpense += amount;
                totalBalance -= amount;
            }

            // 更新顯示的總收入和總支出
            txvTotalBalance.setText(String.format("收支平衡：$" + totalBalance));
            txvTotalIncome.setText(String.format("總收入：$" + totalIncome));
            txvTotalExpense.setText(String.format("總支出：$" + totalExpense));

            // 清空金額輸入框
            edtAmount.setText("");
        }
    }

}