package com.example.laba30.UI;

import static com.example.laba30.data.Constants.APP_KEY_GROUP;
import static com.example.laba30.data.Constants.APP_KEY_NAME;
import static com.example.laba30.data.Constants.APP_KEY_SURNAME;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_ACCEPTED_AMOUNT;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_ACCEPTED_DATES;
import static com.example.laba30.utils.Utils.getCalendarPos;
import static com.example.laba30.utils.Utils.getResourcesString;
import static com.example.laba30.utils.Utils.moveToActivity;
import static com.example.laba30.utils.Utils.setMaxDate;
import static com.example.laba30.utils.Utils.setMinDate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.laba30.R;
import com.example.laba30.data.Date;
import com.example.laba30.databinding.ActivityAcceptedWorksDateInsertionBinding;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("SetTextI18n")
public class AcceptedWorksDateInsertionActivity extends AppCompatActivity {
    private ActivityAcceptedWorksDateInsertionBinding binding;
    private Bundle prevActivityBundle;
    private int currWorkNumber = 0, totalWorksAmount;

    private ArrayList<Date> dateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAcceptedWorksDateInsertionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prevActivityBundle = getIntent().getExtras();
        binding.textView1.setText(prevActivityBundle.getString(APP_KEY_NAME) + " " + prevActivityBundle.getString(APP_KEY_SURNAME) + "\n" + prevActivityBundle.getString(APP_KEY_GROUP));
        totalWorksAmount = prevActivityBundle.getInt(APP_KEY_WORKS_ACCEPTED_AMOUNT);
        dateList = new ArrayList<>();

        setMinDate(binding.datePicker, -30);
        setMaxDate(binding.datePicker, 0);

        binding.goFurtherButton.setOnClickListener(view -> {
            Date chosenDate = new Date(binding.datePicker.getYear(), binding.datePicker.getMonth()+1, binding.datePicker.getDayOfMonth());
            dateList.add(currWorkNumber, chosenDate);
            currWorkNumber++;
            setMinDate(binding.datePicker, getCalendarPos(chosenDate, -30, 0));
            updateActivity();
        });

        Timer event_timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> updateActivity());
            }
        };
        event_timer.schedule(timerTask, 100L);
    }

    private String getResString(int id)
    {
        return getResourcesString(getResources(), id);
    }

    private String getResString(int id, int number)
    {
        return getResourcesString(getResources(), id, number);
    }

    private void updateActivity() {
        if (currWorkNumber < totalWorksAmount) {
            binding.datePicker.setVisibility(View.VISIBLE);
            binding.dateTextView.setText(getResString(R.string.res_text_insert_when_was_accepted_numbered, currWorkNumber+1));
        } else {
            moveToNextActivity();
        }
    }

    private void moveToNextActivity() {
        Intent i = new Intent(this, CompletedWorksDateInsertionActivity.class);
        i.putExtras(prevActivityBundle);
        i.putParcelableArrayListExtra(APP_KEY_WORKS_ACCEPTED_DATES, dateList);
        moveToActivity(this, i);
    }
}
