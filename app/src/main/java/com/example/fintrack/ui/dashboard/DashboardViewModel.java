package com.example.fintrack.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// viewmodel for dashboard fragment
public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    // initialise live data with default text
    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment"); // set initial dashboard text
    }

    // provide live data to observers
    public LiveData<String> getText() {
        return mText;
    }
}
