package com.example.myfinanceapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    static final String DB_NAME="FinanceDB";
    static final String TB_NAME="finance";
    static final int MAX=8;
    static final String TOTALS_TABLE = "totals";
    static final String[] FROM=new String[] {"category","info","amount", "date"};
    SQLiteDatabase db;
    Cursor cur;
    SimpleCursorAdapter adapter;
    Button btnInsert, btnUpdate, btnDelete, gotoSettingButton;
    ListView lv;

    final String tag=MainActivity.class.getSimpleName();
    final String tagName = "LIU";
    private Spinner spnCategory;
    private EditText edtAmount, edtInfo, edtDate;
    private TextView txvTotalIncome, txvTotalExpense, txvTotalBalance;
    private GridLayout incomeButtons, expenseButtons;
    private double totalIncome = 0.0;
    private double totalExpense = 0.0;
    private double totalBalance = 0.0;
    private String selectedCategory = ""; // 儲存選中的類別
    // 獲取今天的日期
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String today = dateFormat.format(new Date());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 元件初始化
        spnCategory = findViewById(R.id.spnCategory);
        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.datePicker);
        txvTotalIncome = findViewById(R.id.txvTotalIncome);
        txvTotalExpense = findViewById(R.id.txvTotalExpense);
        txvTotalBalance = findViewById(R.id.txvTotalBalance);
        incomeButtons = findViewById(R.id.incomeButtons);
        expenseButtons = findViewById(R.id.expenseButtons);
        edtInfo = findViewById(R.id.edtInfo);
        btnInsert = findViewById(R.id.btnInsert);
//        btnUpdate = findViewById(R.id.btnUpdate);
//        btnDelete = findViewById(R.id.btnDelete);
        gotoSettingButton = findViewById(R.id.settingButton);

        gotoSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSecondActivity(v);
            }
        });

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

        // 刪除舊資料庫（僅用於開發測試時）
        this.deleteDatabase(DB_NAME);

        // 開啟資料庫
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        // 建立資料表
        String createTable = "CREATE TABLE IF NOT EXISTS " + TB_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category VARCHAR(32), " +
                "info VARCHAR(32), " +
                "amount VARCHAR(64)," +
                "date VARCHAR(32))";
        db.execSQL(createTable);

        String createTotalTable = "CREATE TABLE IF NOT EXISTS totals " +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "totalIncome REAL, " +
                "totalExpense REAL, " +
                "totalBalance REAL)";
        db.execSQL(createTotalTable);

        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        if (cur.getCount() == 0) {
            addData(getString(R.string.default_category_1),getString(R.string.default_info_1), "49", today);
            addData(getString(R.string.default_category_2),getString(R.string.default_info_2), "45000", today);

            updateInitialTotals();
        }

        Cursor totalCursor = db.rawQuery("SELECT * FROM " + TOTALS_TABLE, null);
        if (totalCursor.moveToFirst()) { // 確保游標指向第一筆資料
            int incomeIndex = totalCursor.getColumnIndex("totalIncome");
            int expenseIndex = totalCursor.getColumnIndex("totalExpense");
            int balanceIndex = totalCursor.getColumnIndex("totalBalance");

            // 確認欄位是否存在
            if (incomeIndex != -1 && expenseIndex != -1 && balanceIndex != -1) {
                totalIncome = totalCursor.getDouble(incomeIndex);
                totalExpense = totalCursor.getDouble(expenseIndex);
                totalBalance = totalCursor.getDouble(balanceIndex);

                // 更新 UI
                txvTotalIncome.setText(String.format(getString(R.string.total_income), totalIncome));
                txvTotalExpense.setText(String.format(getString(R.string.total_expense), totalExpense));
                txvTotalBalance.setText(String.format(getString(R.string.balance), totalBalance));
            } else {
                Log.e(tagName, "資料表欄位不完整！");
            }
        } else {
            // 如果資料表沒有資料，初始化數據
            totalIncome = 0.0;
            totalExpense = 0.0;
            totalBalance = 0.0;

            ContentValues cv = new ContentValues();
            cv.put("totalIncome", totalIncome);
            cv.put("totalExpense", totalExpense);
            cv.put("totalBalance", totalBalance);
            db.insert(TOTALS_TABLE, null, cv);
        }
        totalCursor.close();

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item,
                cur,
                FROM,
                new int[]{R.id.category, R.id.info, R.id.amount, R.id.date},
                0
        );

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            // 檢查是否是我們想設定背景顏色的欄位
            if (view.getId() == R.id.category || view.getId() == R.id.info || view.getId() == R.id.amount) {
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

                // 根據類別設置背景顏色
                View parent = (View) view.getParent();
                if (category.contains("薪水") || category.contains("Salary") || category.contains("給料") || category.contains("월급") || category.contains("獎金") || category.contains("Bonus") || category.contains("ボーナス") || category.contains("보너스") || category.contains("Interest") || category.contains("利息") || category.contains("이자") || category.contains("贈與") || category.contains("Gift") || category.contains("贈り物") || category.contains("선물") || category.contains("零用錢") || category.contains("Allowance") || category.contains("お小遣い") || category.contains("용돈") || category.contains("\uD83D\uDCE5")) {
                    parent.setBackgroundColor(Color.rgb(221, 240, 222)); // 收入顯示綠色
                } else {
                    parent.setBackgroundColor(Color.rgb(245, 217, 215)); // 支出顯示紅色
                }
                return false; // 繼續執行其他綁定操作
            }
            return false;
        });

        lv = findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        requery();

        btnInsert.setOnClickListener(v -> {
            Log.d(tagName, "新增按鈕被點擊");
            onInsertUpdate(v);
        });

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
                if (category.contains("收入") || category.contains("Income") || category.contains("収入") || category.contains("지출")) {
                    incomeButtons.setVisibility(View.VISIBLE);
                    expenseButtons.setVisibility(View.GONE);
                } else if (category.contains("支出") || category.contains("Expense") || category.contains("수입")) {
                    incomeButtons.setVisibility(View.GONE);
                    expenseButtons.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}

        });

        EditText datePicker = findViewById(R.id.datePicker);


        // 為日期輸入框添加點擊事件
        datePicker.setOnClickListener(v -> showDatePickerDialog(datePicker));

    }
    public void gotoSecondActivity(View v) {
        Intent it = new Intent(this, SecondActivity.class); //建立 Intent 並設定目標 Activity
        startActivity(it); // 啟動 Intent 中的目標 Activity
    }

    private void clearButtonSelections(Button[] buttons) {
        for (Button btn : buttons) {
            btn.setSelected(false);
        }
    }

    private void updateInitialTotals() {
        Cursor initialCursor = db.rawQuery("SELECT * FROM " + TB_NAME, null);

        double initialIncome = 0.0;
        double initialExpense = 0.0;

        while (initialCursor.moveToNext()) {
            int categoryIndex = initialCursor.getColumnIndexOrThrow("category");
            int amountIndex = initialCursor.getColumnIndexOrThrow("amount");

            String category = initialCursor.getString(categoryIndex);
            String amountStr = initialCursor.getString(amountIndex).replace("$", ""); // 移除金額中的「$」
            double amount = Double.parseDouble(amountStr);

            // 判斷是收入還是支出
            if (category.contains("收入") || category.contains("Income") || category.contains("収入") || category.contains("지출") ||category.contains("薪水") || category.contains("Salary") || category.contains("給料") || category.contains("월급")) {
                initialIncome += amount;
            } else {
                initialExpense += amount;
            }
        }

        initialCursor.close();

        // 計算收支平衡
        totalIncome = initialIncome;
        totalExpense = initialExpense;
        totalBalance = totalIncome - totalExpense;

        // 更新 totals 表
        ContentValues cv = new ContentValues();
        cv.put("totalIncome", totalIncome);
        cv.put("totalExpense", totalExpense);
        cv.put("totalBalance", totalBalance);
        db.update(TOTALS_TABLE, cv, null, null);

        txvTotalIncome.setText(String.format(getString(R.string.total_income), totalIncome));
        txvTotalExpense.setText(String.format(getString(R.string.total_expense), totalExpense));
        txvTotalBalance.setText(String.format(getString(R.string.balance), totalBalance));
        edtInfo.setText("");
        edtAmount.setText("");

    }

    private void addData(String selectedCategory, String info, String amount, String date) {
        ContentValues cv = new ContentValues();
        cv.put(FROM[0], selectedCategory); // 使用選中的類別
        cv.put(FROM[1], info);
        cv.put(FROM[2], '$' + amount);
        cv.put(FROM[3], date);
        long result = db.insert(TB_NAME, null, cv);
        if (result != -1) {
            Log.d(tagName, "新增成功，ID：" + result);
        } else {
            Log.e(tagName, "新增失敗");
        }
        requery();
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
        Log.d(tagName, "目前資料筆數：" + cur.getCount());
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
        int dateIndex = cur.getColumnIndex(FROM[3]);

        if (infoIndex != -1) edtInfo.setText(cur.getString(infoIndex));
        if (amountIndex != -1) {
            String amountString = cur.getString(amountIndex);
            if (amountString != null && amountString.length() > 1) {
                // 去掉第一個字元，從第二個字元開始
                edtAmount.setText(amountString.substring(1));
            } else {
                // 如果字串為空或只有一個字元，設置為空
                edtAmount.setText("");
            }
        }
        if (dateIndex != -1) edtDate.setText(cur.getString(dateIndex));
//        btUpdate.setEnabled(true);
//        btDelete.setEnabled(true);
    }

    public void onInsertUpdate(View v){
        String info = edtInfo.getText().toString().trim();
        String amount = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        Log.d(tagName, "進入");
        if(date.isEmpty()){
            // 設置為今天的日期
            date = today;
        }

        if (selectedCategory.isEmpty() || amount.isEmpty()) {
            // 顯示錯誤提示，確保所有欄位都有填寫
            Toast.makeText(this, getText(R.string.toast), Toast.LENGTH_SHORT).show();
            return;
        }

        // 新增資料
        addData(selectedCategory, info, amount, date);

        // 更新總收入、總支出與平衡
        updateInitialTotals();
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

    private void showDatePickerDialog(EditText dateField) {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            dateField.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

}