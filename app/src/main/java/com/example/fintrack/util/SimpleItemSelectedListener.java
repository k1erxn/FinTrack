package com.example.fintrack.util;

import android.view.View;
import android.widget.AdapterView;

// base class for spinner listener override only one method
public abstract class SimpleItemSelectedListener
        implements AdapterView.OnItemSelectedListener {

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nothing to do when no selection
    }

    @Override
    public void onItemSelected(AdapterView<?> parent,
                               View view,
                               int position,
                               long id) {
        // delegate to simple method with index only
        onItemSelected(position);
    }

    // implement this method to handle selection by index
    public abstract void onItemSelected(int position);
}
