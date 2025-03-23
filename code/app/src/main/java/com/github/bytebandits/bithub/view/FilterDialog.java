package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.bytebandits.bithub.R;

public class FilterDialog {
    private Context context;
    private FilterListener filterListener;

    public FilterDialog(Context context, FilterListener filterListener) {
        this.context = context;
        this.filterListener = filterListener;
    }

    public void showFilterDialog() {
        // Inflate your custom filter dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.filter_dialog, null);

        // Create the AlertDialog
        AlertDialog filterDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.all_radio_button) {
                    filterListener.onFilterSelected("all");
                } else if (checkedId == R.id.anger_radio_button) {
                    filterListener.onFilterSelected("anger");
                } else if (checkedId == R.id.confusion_radio_button) {
                    filterListener.onFilterSelected("confusion");
                } else if (checkedId == R.id.disgust_radio_button) {
                    filterListener.onFilterSelected("disgust");
                } else if (checkedId == R.id.fear_radio_button) {
                    filterListener.onFilterSelected("fear");
                } else if (checkedId == R.id.happiness_radio_button) {
                    filterListener.onFilterSelected("happiness");
                } else if (checkedId == R.id.sadness_radio_button) {
                    filterListener.onFilterSelected("sadness");
                } else if (checkedId == R.id.shame_radio_button) {
                    filterListener.onFilterSelected("shame");
                } else if (checkedId == R.id.suprise_radio_button) {
                    filterListener.onFilterSelected("suprise");
                } else if (checkedId == R.id.last_week_radio_button) {
                    filterListener.onFilterSelected("last_week");
                }
            }
        });

        filterDialog.show();
    }

    public interface FilterListener {
        void onFilterSelected(String mood);
    }
}
