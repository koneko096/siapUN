package io.github.koneko096.siapun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Locale;

import io.github.koneko096.siapun.databinding.FragmentResultBinding;

public class ResultFragment extends Fragment {

    private FragmentResultBinding binding;
    private float score;
    private int paket;
    private String mapel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            score = getArguments().getFloat("score");
            paket = getArguments().getInt("paket");
            mapel = getArguments().getString("mapel");
        }

        binding.score.setText(String.format(Locale.US, "%.1f", score));

        // Save result (database)
        new Thread(() -> {
            new StatisticsDbHelper(requireContext().getApplicationContext())
                    .insertScore(mapel, paket, score);
        }).start();

        binding.history.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("paket", paket);
            args.putString("mapel", mapel);
            Navigation.findNavController(v).navigate(R.id.action_resultFragment_to_highscoreFragment, args);
        });

        binding.cancel.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_resultFragment_to_titleFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
