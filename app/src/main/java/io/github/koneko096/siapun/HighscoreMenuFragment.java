package io.github.koneko096.siapun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.List;

import io.github.koneko096.siapun.databinding.FragmentHighscoreMenuBinding;

public class HighscoreMenuFragment extends Fragment {

    private FragmentHighscoreMenuBinding binding;
    private List<com.google.android.material.button.MaterialButton> paketSelector;
    private List<com.google.android.material.button.MaterialButton> mapelSelector;
    private int paket;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHighscoreMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Paket buttons
        paketSelector = new ArrayList<>(4);
        paketSelector.add(binding.paket1h);
        paketSelector.add(binding.paket2h);
        paketSelector.add(binding.paket3h);
        paketSelector.add(binding.paket4h);

        // Mapel buttons — separate section
        mapelSelector = new ArrayList<>(4);
        mapelSelector.add(binding.mapel1h);
        mapelSelector.add(binding.mapel2h);
        mapelSelector.add(binding.mapel3h);
        mapelSelector.add(binding.mapel4h);

        for (int i = 0; i < paketSelector.size(); i++) {
            final int paketIndex = i + 1;
            paketSelector.get(i).setOnClickListener(v -> {
                paket = paketIndex;
                showMapelSelection();
            });
        }

        binding.btnBack.setOnClickListener(v -> showPaketSelection());
    }

    private void showPaketSelection() {
        binding.layoutPaket.setVisibility(View.VISIBLE);
        binding.layoutMapel.setVisibility(View.GONE);
        binding.tvMenuHeader.setText("Riwayat Nilai");
    }

    private void showMapelSelection() {
        binding.layoutPaket.setVisibility(View.GONE);
        binding.layoutMapel.setVisibility(View.VISIBLE);
        binding.tvMenuHeader.setText("Paket " + paket + " — Pilih Mata Pelajaran");

        String[] mapels = AppConstants.MAPEL_NAMES;
        String[] codes = AppConstants.MAPEL_CODES;

        for (int i = 0; i < mapelSelector.size(); i++) {
            final int index = i;
            mapelSelector.get(i).setText("📖  " + mapels[i]);
            mapelSelector.get(i).setVisibility(View.VISIBLE);
            mapelSelector.get(i).setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt(AppConstants.KEY_PAKET, paket);
                args.putString(AppConstants.KEY_MAPEL, codes[index]);
                Navigation.findNavController(v).navigate(
                        R.id.action_highscoreMenuFragment_to_highscoreFragment, args);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
