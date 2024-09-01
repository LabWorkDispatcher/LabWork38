package com.example.laba30.UI;

import static com.example.laba30.data.Constants.APP_KEY_DISPLAY_REMAINING_WORKS_AMOUNT;
import static com.example.laba30.data.Constants.APP_KEY_GROUP;
import static com.example.laba30.data.Constants.APP_KEY_NAME;
import static com.example.laba30.data.Constants.APP_KEY_SURNAME;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_ACCEPTED_AMOUNT;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_ACCEPTED_DATES;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_COMPLETED_AMOUNT;
import static com.example.laba30.data.Constants.APP_KEY_WORKS_COMPLETED_DATES;
import static com.example.laba30.data.Constants.APP_MIN_AMOUNT_WORKS_ACCEPTED;
import static com.example.laba30.data.Constants.APP_MIN_AMOUNT_WORKS_COMPLETED;
import static com.example.laba30.data.Constants.APP_MIN_LENGTH_GROUP;
import static com.example.laba30.data.Constants.APP_MIN_LENGTH_NAME;
import static com.example.laba30.data.Constants.APP_MIN_LENGTH_SURNAME;
import static com.example.laba30.utils.Utils.getPersonalDataByKey;
import static com.example.laba30.utils.Utils.getPersonalDataKey;
import static com.example.laba30.utils.Utils.getResourcesString;
import static com.example.laba30.utils.Utils.moveToActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.example.laba30.R;
import com.example.laba30.data.PersonalDataFormat;
import com.example.laba30.databases.PDataEntity;
import com.example.laba30.databinding.ActivityMainBinding;
import com.example.laba30.databinding.BasicPopupWindowBinding;
import com.example.laba30.viewmodels.PDataViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private BasicPopupWindowBinding popupBinding;

    private boolean nameETFocus = false, surnameETFocus = false;

    private PDataViewModel pDataViewModel;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle prevActivityBundle = getIntent().getExtras();
        if (prevActivityBundle != null) {
            binding.userName.setText(prevActivityBundle.getString(APP_KEY_NAME));
            binding.userSurname.setText(prevActivityBundle.getString(APP_KEY_SURNAME));
            binding.userGroup.setText(prevActivityBundle.getString(APP_KEY_GROUP));
            binding.worksCompleted.setText("" + prevActivityBundle.getInt(APP_KEY_WORKS_COMPLETED_AMOUNT));
            binding.worksAccepted.setText("" + prevActivityBundle.getInt(APP_KEY_WORKS_ACCEPTED_AMOUNT));
        }

        pDataViewModel = new ViewModelProvider(this).get(PDataViewModel.class);
        pDataViewModel.getAllData().observe(this, this::setupSpinner);

        binding.goFurtherButton.setOnClickListener(view -> {
            String nameText = binding.userName.getText().toString(),
                    surnameText = binding.userSurname.getText().toString(),
                    groupText = binding.userGroup.getText().toString(),
                    worksCompletedText = binding.worksCompleted.getText().toString(),
                    worksAcceptedText = binding.worksAccepted.getText().toString();

            if (nameText.isEmpty() || surnameText.isEmpty() || groupText.isEmpty() || worksCompletedText.isEmpty() || worksAcceptedText.isEmpty()) {
                Toast.makeText(this, getResString(R.string.res_toast_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (nameText.length() < APP_MIN_LENGTH_NAME) {
                Toast.makeText(this, getResString(R.string.res_toast_minimal_name_length, APP_MIN_LENGTH_NAME), Toast.LENGTH_SHORT).show();
                return;
            }
            if (surnameText.length() < APP_MIN_LENGTH_NAME) {
                Toast.makeText(this, getResString(R.string.res_toast_minimal_surname_length, APP_MIN_LENGTH_SURNAME), Toast.LENGTH_SHORT).show();
                return;
            }
            if (groupText.length() < APP_MIN_LENGTH_GROUP) {
                Toast.makeText(this, getResString(R.string.res_toast_minimal_group_length, APP_MIN_LENGTH_GROUP), Toast.LENGTH_SHORT).show();
                return;
            }

            int completedWorksAmount = Integer.parseInt(worksCompletedText), acceptedWorksAmount = Integer.parseInt(worksAcceptedText);
            if (completedWorksAmount < APP_MIN_AMOUNT_WORKS_COMPLETED) {
                Toast.makeText(this, getResString(R.string.res_toast_minimal_works_amount_completed, APP_MIN_AMOUNT_WORKS_COMPLETED), Toast.LENGTH_SHORT).show();
                return;
            }
            if (acceptedWorksAmount < APP_MIN_AMOUNT_WORKS_ACCEPTED) {
                Toast.makeText(this, getResString(R.string.res_toast_minimal_works_amount_accepted, APP_MIN_AMOUNT_WORKS_ACCEPTED), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, AcceptedWorksDateInsertionActivity.class);
            i.putExtra(APP_KEY_NAME, nameText);
            i.putExtra(APP_KEY_SURNAME, surnameText);
            i.putExtra(APP_KEY_GROUP, groupText);
            i.putExtra(APP_KEY_WORKS_COMPLETED_AMOUNT, completedWorksAmount);
            i.putExtra(APP_KEY_WORKS_ACCEPTED_AMOUNT, acceptedWorksAmount);
            moveToActivity(this, i);
        });

        binding.userName.setOnFocusChangeListener((view, b) -> {
            nameETFocus = b;
            if (!nameETFocus && !surnameETFocus) {
                checkSPreferences();
            }
        });
        binding.userSurname.setOnFocusChangeListener((view, b) -> {
            surnameETFocus = b;
            if (!nameETFocus && !surnameETFocus) {
                checkSPreferences();
            }
        });
    }

    private void setupSpinner(List<PDataEntity> pDataList) {
        ArrayList<PersonalDataFormat> resultList = new ArrayList<>();
        for (PDataEntity pData : pDataList) {
            resultList.add(new Gson().fromJson(pData.info_pData, PersonalDataFormat.class));
        }

        ArrayList<String> spinnerList = new ArrayList<>();
        for (PersonalDataFormat i : resultList) {
            String printName = i.name + " " + i.surname;

            int appearanceAmount = 0;
            for (int j = spinnerList.size(); j > 0; j--) {
                String comparisonName;
                if (j == 1) {
                    comparisonName = printName;
                } else {
                    comparisonName = printName + " (" + j + ")";
                }
                if (spinnerList.contains(comparisonName)) {
                    appearanceAmount = j;
                    break;
                }
            }
            if (appearanceAmount > 0) {
                printName += (" (" + (appearanceAmount+1) + ")");
            }

            spinnerList.add(printName);
        }
        if (spinnerList.isEmpty()) { binding.chooseFromDBSpinner.setEnabled(false); return; }

        spinnerList.add(0, "");
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item, spinnerList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.chooseFromDBSpinner.setAdapter(spinnerAdapter);
        binding.chooseFromDBSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) { return; }
                PersonalDataFormat chosenItem = resultList.get(i - 1);

                Intent intent = new Intent(binding.getRoot().getContext(), FinalActivity.class);
                intent.putExtra(APP_KEY_NAME, chosenItem.name);
                intent.putExtra(APP_KEY_SURNAME, chosenItem.surname);
                intent.putExtra(APP_KEY_GROUP, chosenItem.group);
                intent.putExtra(APP_KEY_WORKS_ACCEPTED_AMOUNT, chosenItem.acceptedWorksAmount);
                intent.putExtra(APP_KEY_WORKS_COMPLETED_AMOUNT, chosenItem.completedWorksAmount);
                intent.putParcelableArrayListExtra(APP_KEY_WORKS_ACCEPTED_DATES, chosenItem.acceptedWorksDates);
                intent.putParcelableArrayListExtra(APP_KEY_WORKS_COMPLETED_DATES, chosenItem.completedWorksDates);
                intent.putExtra(APP_KEY_DISPLAY_REMAINING_WORKS_AMOUNT, true);

                binding.chooseFromDBText.setText("");
                moveToActivity((AppCompatActivity)binding.getRoot().getContext(), intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        binding.chooseFromDBSpinner.setEnabled(true);
    }

    private void checkSPreferences() {
        PersonalDataFormat pData = getPersonalDataByKey(this, getPersonalDataKey(binding.userName.getText().toString() + binding.userSurname.getText().toString()));
        if (pData != null) {
            popupBinding = BasicPopupWindowBinding.inflate(getLayoutInflater());
            View popupView = popupBinding.getRoot();

            int width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            int height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupBinding.popupText.setText(getResString(R.string.res_popup_data_replacement));

            popupBinding.noButton.setOnClickListener(view -> {
                popupWindow.dismiss();
            });
            popupBinding.yesButton.setOnClickListener(view -> {
                Intent i = new Intent(this, FinalActivity.class);
                i.putExtra(APP_KEY_NAME, pData.name);
                i.putExtra(APP_KEY_SURNAME, pData.surname);
                i.putExtra(APP_KEY_GROUP, pData.group);
                i.putExtra(APP_KEY_WORKS_COMPLETED_AMOUNT, pData.completedWorksAmount);
                i.putExtra(APP_KEY_WORKS_ACCEPTED_AMOUNT, pData.acceptedWorksAmount);
                i.putParcelableArrayListExtra(APP_KEY_WORKS_COMPLETED_DATES, pData.completedWorksDates);
                i.putParcelableArrayListExtra(APP_KEY_WORKS_ACCEPTED_DATES, pData.acceptedWorksDates);
                moveToActivity(this, i);
            });

            popupWindow.setOnDismissListener(() -> {
                //popupWindowIsActive = false;
            });

            popupWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
        }
    }

    private String getResString(int id)
    {
        return getResourcesString(getResources(), id);
    }

    private String getResString(int id, int number)
    {
        return getResourcesString(getResources(), id, number);
    }
}