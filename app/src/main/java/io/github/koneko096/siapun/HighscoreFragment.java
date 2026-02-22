package io.github.koneko096.siapun;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.koneko096.siapun.databinding.FragmentHighscoreBinding;

public class HighscoreFragment extends Fragment {

    private FragmentHighscoreBinding binding;
    private int paket;
    private String mapel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHighscoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            paket = getArguments().getInt("paket");
            mapel = getArguments().getString("mapel");
        }

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        new Thread(() -> {
            List<String> scores = new StatisticsDbHelper(requireContext().getApplicationContext())
                    .getScores(paket, mapel);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    binding.recyclerView.setAdapter(new ScoreAdapter(scores));
                });
            }
        }).start();
    }

    private static class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {
        private final List<String> scores;

        ScoreAdapter(List<String> scores) {
            this.scores = scores;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.scoreText.setText(scores.get(position));
        }

        @Override
        public int getItemCount() {
            return scores.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView scoreText;

            ViewHolder(View view) {
                super(view);
                scoreText = view.findViewById(R.id.scoreText);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
