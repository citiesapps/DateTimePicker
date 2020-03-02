package com.appmea.datetimepicker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appmea.datetimepicker.views.DatePickerDialogFragment;

import org.joda.time.DateTime;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.appmea.datetimepicker.views.DatePickerDialogFragment.FIELD_ALL;
import static com.appmea.datetimepicker.views.DatePickerDialogFragment.NONE;


public class DatePickerActivity extends AppCompatActivity implements DateSelectListener {

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);


        findViewById(R.id.tv_start).setOnClickListener(v -> {
            DatePickerDialogFragment.startFragment(this,
                    new DatePickerDialogFragment.Builder()
                            .withFields(FIELD_ALL)
                            .withLoops(NONE)
                            .withMinDateTime(new DateTime(1950, 1, 10, 12, 0))
//                            .withMaxDateTime(new DateTime(2015, 4, 23, 12, 0))
                            .withTextSize((int) (getResources().getDisplayMetrics().density * 24))
            );
        });
    }

    @Override
    public void onDateSelected(DateTime dateTime) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, Utils.getDayOfWeekMonthAbbrYearTime().print(dateTime), Toast.LENGTH_SHORT);
        toast.show();
    }
}