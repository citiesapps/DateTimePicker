package com.appmea.datetimepicker;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;


public class DatePickerActivity extends AppCompatActivity {

    private static final int DEFAULT_MIN_YEAR = 1900;


    @BindView(R.id.lv_years)
    LoopView<StringLoopItem> yearLoopView;
    @BindView(R.id.lv_months)
    LoopView<StringLoopItem> monthLoopView;
    @BindView(R.id.lv_days)
    LoopView<StringLoopItem> dayLoopView;

    private int yearPos = 0;
    private int monthPos = 0;
    private int dayPos = 0;

    List<StringLoopItem> yearList = new ArrayList<>();
    List<StringLoopItem> monthList = new ArrayList<>();
    List<StringLoopItem> dayList = new ArrayList<>();

    private int minYear;
    private int maxYear;
    private int viewTextSize;

    //private boolean isLeapYear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_date_picker);

        minYear = DEFAULT_MIN_YEAR;
        viewTextSize = 25;
        maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        setSelectedDate();

        initialiseDateWheel();

    }

    private void initialiseDateWheel() {

        //do not loop,default can loop
        yearLoopView.setNotLoop();
        monthLoopView.setNotLoop();
        dayLoopView.setNotLoop();

        //set loopview text btnTextsize
        yearLoopView.setTextSize(viewTextSize);
        monthLoopView.setTextSize(viewTextSize);
        dayLoopView.setTextSize(viewTextSize);

//        //set checked listen
        yearLoopView.setListener(item -> {

            /*if (isLeapYear(item)) {
                isLeapYear = true;
            }else{
                isLeapYear = false;
            }*/
            yearPos = item;
            initDayPickerView();
        });

        monthLoopView.setListener(item -> {
            monthPos = item;
            initDayPickerView();
        });

        dayLoopView.setListener(item -> dayPos = item);

        initPickerViews(); // init year and month loop view
        initDayPickerView(); //init day loop view
    }

    public void setSelectedDate() {

        Calendar today = Calendar.getInstance();
        yearPos = today.get(Calendar.YEAR) - minYear;
        monthPos = today.get(Calendar.MONTH);
        dayPos = today.get(Calendar.DAY_OF_MONTH) - 1;

    }


    private void initPickerViews() {

        int yearCount = maxYear - minYear;

        for (int i = 0; i < yearCount; i++) {
            yearList.add(new StringLoopItem(format2LenStr(minYear + i)));
        }

        for (int j = 0; j < 12; j++) {
            monthList.add(new StringLoopItem(format2LenStr(j + 1)));
        }

        yearLoopView.setItems(yearList);
        yearLoopView.setInitPosition(yearPos);


        List<StringLoopItem> items = new ArrayList<>();
        DateFormatSymbols symbols = new DateFormatSymbols();
        String[] monthNames = symbols.getMonths();

        for (String month : monthNames) {
            items.add(new StringLoopItem(month));
        }
        monthLoopView.setItems(items);
        monthLoopView.setInitPosition(monthPos);
    }

    /**
     * Init day item
     */
    private void initDayPickerView() {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, minYear + yearPos);
        calendar.set(Calendar.MONTH, monthPos);

        for (int i = 0; i < 31; i++) {
            dayList.add(new StringLoopItem(format2LenStr(i + 1)));
        }


        dayLoopView.setItems(dayList);
        dayLoopView.setInitPosition(dayPos);
    }

    public static String format2LenStr(int num) {
        return (num < 10) ? "0" + num : String.valueOf(num);
    }

    public static boolean isLeapYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
    }
}