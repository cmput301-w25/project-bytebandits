package com.github.bytebandits.bithub.view;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.github.bytebandits.bithub.R;

/**
 * A dialog for filtering mood posts based on selected emotions or a search query.
 */
public class FilterDialog {
    private Context context;
    private FilterListener filterListener;

    /**
     * Constructs a filter dialog with the specified context and filter listener.
     *
     * @param context        The context in which the dialog is created.
     * @param filterListener The listener to handle filter selection and search queries.
     */
    public FilterDialog(Context context, FilterListener filterListener) {
        this.context = context;
        this.filterListener = filterListener;
    }

    /**
     * Displays the filter dialog, allowing users to select a mood or enter a search query.
     */
    public void showFilterDialog() {
        // Inflate your custom filter dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.filter_dialog, null);

        // Create the AlertDialog
        AlertDialog filterDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        ImageButton closeButton = dialogView.findViewById(R.id.close_button);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        EditText searchEditText = dialogView.findViewById(R.id.search_edit_text);

        closeButton.setOnClickListener(v -> filterDialog.dismiss());

        // Handle radio button selections
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

        // Handle search input
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterListener.onSearchQueryChanged(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterDialog.show();
    }

    /**
     * Listener interface for handling filter selection and search queries.
     */
    public interface FilterListener {

        /**
         * Called when a mood filter is selected.
         *
         * @param mood The selected mood as a string.
         */
        void onFilterSelected(String mood);

        /**
         * Called when the search query changes.
         *
         * @param query The updated search query.
         */
        void onSearchQueryChanged(String query); // New method for search
    }
}