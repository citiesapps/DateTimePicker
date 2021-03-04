package com.appmea.datetimepicker.example;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appmea.datetimepicker.DateSelectListener;
import com.appmea.datetimepicker.Utils;
import com.appmea.datetimepicker.example.databinding.ActivityDatePickerBinding;
import com.appmea.datetimepicker.views.DatePickerDialogFragment;
import com.appmea.datetimepicker.views.TimePickerDialogFragment;

import org.joda.time.DateTime;

import timber.log.Timber;

import static com.appmea.datetimepicker.views.DatePickerDialogFragment.FIELD_ALL;
import static com.appmea.datetimepicker.views.DatePickerDialogFragment.NONE;


public class DatePickerActivity extends AppCompatActivity implements DateSelectListener {

    private              Toast toast;
    private static final int   LISTENER_TIME = 0;
    private static final int   LISTENER_DATE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());

        ActivityDatePickerBinding binding = ActivityDatePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvTimePicker.setOnClickListener(v -> TimePickerDialogFragment.startFragment(this,
                                                                                                         new TimePickerDialogFragment.Builder(LISTENER_TIME)
                                                                                                                 .withTextSize((int) (getResources().getDisplayMetrics().density * 24))
                                                                                                                 .withTitle("Test Title")
                                                                                                                 .withButtonColor(0xFFF23123)
                                                                                                                 .withTextColor(0xFFDff81F)
                                                                                                                 .withSelectedTextColor(0xFF8FFF12)
        ));

        binding.tvDatePicker.setOnClickListener(v -> DatePickerDialogFragment.startFragment(this,
                                                                                                         new DatePickerDialogFragment.Builder(LISTENER_DATE)
                                                                                                                 .withFields(FIELD_ALL)
                                                                                                                 .withLoops(NONE)
                                                                                                                 .withTitle("Hallo Title")
                                                                                                                 .withHeaderTextColor(0xFFFF0000)
                                                                                                                 .withHeaderBackgroundColor(0xFF1242D4)
                                                                                                                 .withMinDateTime(new DateTime(2000, 4, 10, 12, 0))
                                                                                                                 .withMaxDateTime(new DateTime(2021, 8, 5, 12, 0))
                                                                                                                 .withTextSize((int) (getResources().getDisplayMetrics().density * 24))
                                                                                                                 .withButtonText("Bitte Auswählen")
                                                                                                                 .withButtonColor(0xFFF23123)
                                                                                                                 .withTextColor(0xFFD3B81F)
                                                                                                                 .withSelectedTextColor(0xFF8F3A12)
        ));

    }

    @Override
    public void onDateSelected(int listenerId, DateTime dateTime) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, Utils.getDayOfWeekMonthAbbrYearTime().print(dateTime), Toast.LENGTH_SHORT);
        toast.show();
    }
}