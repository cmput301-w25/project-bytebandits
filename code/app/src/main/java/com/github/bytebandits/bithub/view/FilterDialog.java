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

    public FilterDialog(Context context) {
        this.context = context;
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
        RadioButton anger = dialogView.findViewById(R.id.anger_radio_button);
        RadioButton confusion = dialogView.findViewById(R.id.confusion_radio_button);
        RadioButton disgust = dialogView.findViewById(R.id.disgust_radio_button);
        RadioButton fear = dialogView.findViewById(R.id.fear_radio_button);
        RadioButton happiness = dialogView.findViewById(R.id.happiness_radio_button);
        RadioButton sadness = dialogView.findViewById(R.id.sadness_radio_button);
        RadioButton shame = dialogView.findViewById(R.id.shame_radio_button);
        RadioButton suprise = dialogView.findViewById(R.id.suprise_radio_button);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(anger.isChecked())
                {
                    // do something
                }
                else if(confusion.isChecked())
                {
                    // do something

                }
                else if(disgust.isChecked())
                {
                    // do something

                }
                else if(fear.isChecked())
                {
                    // do something

                }
                else if(happiness.isChecked())
                {
                    // do something

                }
                else if(sadness.isChecked())
                {
                    // do something

                }
                else if(shame.isChecked())
                {
                    // do something

                }
                else if(suprise.isChecked())
                {
                    // do something

                }
            }
        });


        filterDialog.show();
    }
}
