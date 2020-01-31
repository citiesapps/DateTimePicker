package com.appmea.datetimepicker;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.appmea.datetimepicker.views.DatePickerDialogFragment;

import org.joda.time.DateTime;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.appmea.datetimepicker.views.DatePickerDialogFragment.FIELD_ALL;
import static com.appmea.datetimepicker.views.DatePickerDialogFragment.NONE;


public class DatePickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);


        findViewById(R.id.tv_start).setOnClickListener(v -> {
            DatePickerDialogFragment.startFragment(this,
                    new DatePickerDialogFragment.Builder()
                            .withFields(FIELD_ALL)
                            .withLoops(NONE)
                            .withMinDateTime(new DateTime(2000, 1, 1, 12, 0))
                            .withMaxDateTime(new DateTime(2003, 8, 21, 12, 0))
                            .withTextSize((int) (getResources().getDisplayMetrics().density * 24))
            );
        });
    }
}