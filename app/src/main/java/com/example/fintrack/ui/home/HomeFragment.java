// File: app/src/main/java/com/example/fintrack/ui/home/HomeFragment.java
package com.example.fintrack.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fintrack.R;
import com.example.fintrack.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // init viewmodel (if you have HomeViewModel showing data on home)
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        // inflate layout via view binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // observe any live data (e.g. a welcome text)
        homeViewModel.getText()
                .observe(getViewLifecycleOwner(), binding.textHome::setText);

        // navigate to AddTransactionFragment on FAB click
        binding.fabAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigation_home_to_addTransactionFragment)
        );



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;   // avoid memory leaks
    }
}
