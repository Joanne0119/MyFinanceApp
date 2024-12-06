package com.example.myfinanceapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    static final String DB_NAME="FinanceDB";
    static final String TB_NAME="finance";
    static final int MAX=8;
    static final String[] FROM=new String[] {"category","info","amount"};
    SQLiteDatabase db;
    Cursor cur;
    SimpleCursorAdapter adapter;
    Button btnInsert, btnUpdate, btnDelete;
    ListView lv;

    final String tag=MainActivity.class.getSimpleName();
    private Spinner spnCategory;
    private EditText edtAmount, edtInfo;
    private TextView txvTotalIncome, txvTotalExpense, txvTotalBalance;
    private GridLayout incomeButtons, expenseButtons;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private double totalBalance = 0.0;
    private String selectedCategory = ""; // 儲存選中的類別


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 元件初始化
        spnCategory = findViewById(R.id.spnCategory);
        edtAmount = findViewById(R.id.edtAmount);
        txvTotalIncome = findViewById(R.id.txvTotalIncome);
        txvTotalExpense = findViewById(R.id.txvTotalExpense);
        txvTotalBalance = findViewById(R.id.txvTotalBalance);
        incomeButtons = findViewById(R.id.incomeButtons);
        expenseButtons = findViewById(R.id.expenseButtons);
        edtInfo = findViewById(R.id.edtInfo);
        btnInsert = findViewById(R.id.btnInsert);
//        btnUpdate = findViewById(R.id.btnUpdate);
//        btnDelete = findViewById(R.id.btnDelete);

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

        // 開啟資料庫
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        // 建立資料表
        String createTable = "CREATE TABLE IF NOT EXISTS " + TB_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category VARCHAR(32), " +
                "info VARCHAR(32), " +
                "amount VARCHAR(64))";
        db.execSQL(createTable);

        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        if (cur.getCount() == 0) {
            addData("🍔漢堡","全家49早餐組合", "$49");
            addData("💰薪水","11月薪水", "$45000");
        }

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item,
                cur,
                FROM,
                new int[]{R.id.category, R.id.info, R.id.amount},
                0
        );

        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        requery();

        btnInsert.setOnClickListener(this::onInsertUpdate);

        // 處理收入按鈕選中狀態
        for (Button btn : incomeBtns) {
            btn.setOnClickListener(v -> {
                clearButtonSelections(incomeBtns); // 取消其他按鈕的選中狀態
                btn.setSelected(true);             // 設置當前按鈕為選中狀態
                selectedCategory = btn.getText().toString(); // 記錄按鈕的文字內容
            });
        }

        // 處理支出按鈕選中狀態
        for (Button btn : expenseBtns) {
            btn.setOnClickListener(v -> {
                clearButtonSelections(expenseBtns); // 取消其他按鈕的選中狀態
                btn.setSelected(true);              // 設置當前按鈕為選中狀態
                selectedCategory = btn.getText().toString(); // 記錄按鈕的文字內容
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
        btnInsert.setOnClickListener(v -> updateTotals());

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
            if (category.contains("收入")) {
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
            edtInfo.setText("");
        }
    }

    private void addData(String selectedCategory, String info, String amount) {
        ContentValues cv = new ContentValues();
        cv.put(FROM[0], selectedCategory); // 使用選中的類別
        cv.put(FROM[1], info);
        cv.put(FROM[2], '$' + amount);
        db.insert(TB_NAME, null, cv);
    }

    private void update(String info, String amount, int id) {
        ContentValues cv = new ContentValues();
        cv.put(FROM[0], selectedCategory); // 更新選中的類別
        cv.put(FROM[1], info);
        cv.put(FROM[2], amount);
        db.update(TB_NAME, cv, "_id=" + id, null);
    }

    private void requery() {
        if (adapter.getCursor() != null && !adapter.getCursor().isClosed()) {
            adapter.getCursor().close();
        }
        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        adapter.changeCursor(cur);

        btnInsert.setEnabled(cur.getCount() < MAX);
//        btUpdate.setEnabled(false);
//        btDelete.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cur.moveToPosition(position);

        int categoryIndex = cur.getColumnIndex(FROM[0]);
        int infoIndex = cur.getColumnIndex(FROM[1]);
        int amountIndex = cur.getColumnIndex(FROM[2]);

//        if (categoryIndex != -1) edtInfo.setText(cur.getString(categoryIndex));
        if (infoIndex != -1) edtInfo.setText(cur.getString(infoIndex));
        if (amountIndex != -1) edtAmount.setText(cur.getString(amountIndex));

//        btUpdate.setEnabled(true);
//        btDelete.setEnabled(true);
    }

    public void onInsertUpdate(View v){
        String info = edtInfo.getText().toString().trim();
        String amount = edtAmount.getText().toString().trim();

        if (selectedCategory.isEmpty() || amount.isEmpty()) {
            // 顯示錯誤提示，確保所有欄位都有填寫
            Toast.makeText(this, "請選擇類別並填寫金額！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 新增資料
        addData(selectedCategory, info, amount);

        // 更新總收入、總支出與平衡
        updateTotals();
        requery(); // 重新查詢資料並更新列表
    }

    public void onDelete(View v){	// 刪除鈕的On Click事件方法
        db.delete(TB_NAME, "_id="+cur.getInt(0),null);
        requery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cur != null && !cur.isClosed()) cur.close();
        if (db != null && db.isOpen()) db.close();
    }

}