package io.github.koneko096.siapun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.koneko096.siapun.databinding.FragmentExerciseMenuBinding;

public class ExerciseMenuFragment extends Fragment {

    private FragmentExerciseMenuBinding binding;
    private List<com.google.android.material.button.MaterialButton> paketSelector;
    private List<com.google.android.material.button.MaterialButton> mapelSelector;
    private int paket;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Paket (package) buttons
        paketSelector = new ArrayList<>(4);
        paketSelector.add(binding.paket1e);
        paketSelector.add(binding.paket2e);
        paketSelector.add(binding.paket3e);
        paketSelector.add(binding.paket4e);

        // Mapel (subject) buttons — separate set in the layout
        mapelSelector = new ArrayList<>(4);
        mapelSelector.add(binding.mapel1);
        mapelSelector.add(binding.mapel2);
        mapelSelector.add(binding.mapel3);
        mapelSelector.add(binding.mapel4);

        // When a paket button is clicked, load matching subjects
        for (int i = 0; i < paketSelector.size(); i++) {
            final int paketIndex = i + 1; // 1-based
            paketSelector.get(i).setOnClickListener(v -> {
                paket = paketIndex;
                showMapelSelection();
            });
        }

        // Back button returns to paket selection
        binding.btnBack.setOnClickListener(v -> showPaketSelection());
    }

    /** Switch UI to show the paket (package) section */
    private void showPaketSelection() {
        binding.layoutPaket.setVisibility(View.VISIBLE);
        binding.layoutMapel.setVisibility(View.GONE);
        binding.tvMenuHeader.setText("Pilih Paket");
    }

    /** Switch UI to show the mapel (subject) section for the current paket */
    private void showMapelSelection() {
        binding.layoutPaket.setVisibility(View.GONE);
        binding.layoutMapel.setVisibility(View.VISIBLE);
        binding.tvMenuHeader.setText("Paket " + paket + " — Pilih Mata Pelajaran");

        String[] mapels = AppConstants.MAPEL_NAMES;
        String[] codes = AppConstants.MAPEL_CODES;

        boolean anyVisible = false;
        for (int i = 0; i < mapelSelector.size(); i++) {
            final int index = i;
            String path = "questions/v" + paket + "/" + codes[i] + ".json";
            if (assetExists(path)) {
                mapelSelector.get(i).setText("📖  " + mapels[i]);
                mapelSelector.get(i).setVisibility(View.VISIBLE);
                mapelSelector.get(i).setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt(AppConstants.KEY_PAKET, paket);
                    args.putString(AppConstants.KEY_MAPEL, codes[index]);
                    Navigation.findNavController(v).navigate(
                            R.id.action_exerciseMenuFragment_to_exerciseFragment, args);
                });
                anyVisible = true;
            } else {
                mapelSelector.get(i).setVisibility(View.GONE);
            }
        }

        // Show "no subjects" message if none are available
        binding.tvNoSubject.setVisibility(anyVisible ? View.GONE : View.VISIBLE);
    }

    private boolean assetExists(String path) {
        try (InputStream is = requireContext().getAssets().open(path)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
