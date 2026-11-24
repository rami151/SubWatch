package com.example.subwatch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.subwatch.R;
import com.example.subwatch.databinding.FragmentSubscriptionsBinding;

/**
 * Fragment listing active subscriptions with sorting, empty state, and extended FAB (F5).
 */
public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private SubscriptionsViewModel viewModel;
    private SubscriptionAdapter adapter;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubscriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SubscriptionsViewModel.class);
        navController = NavHostFragment.findNavController(this);

        setupRecyclerView();
        setupActions();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new SubscriptionAdapter(viewModel.getClock(), item -> {
            Bundle args = new Bundle();
            args.putLong("subscriptionId", item.getSubscription().getId());
            navController.navigate(R.id.action_subscriptions_to_fiche, args);
        });

        binding.recyclerViewSubscriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSubscriptions.setAdapter(adapter);
    }

    private void setupActions() {
        binding.buttonSort.setOnClickListener(this::showSortMenu);

        View.OnClickListener addListener = v -> {
            Bundle args = new Bundle();
            args.putLong("subscriptionId", -1L);
            navController.navigate(R.id.action_subscriptions_to_formulaire, args);
        };
        binding.fabAdd.setOnClickListener(addListener);
        binding.buttonEmptyAdd.setOnClickListener(addListener);
    }

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.inflate(R.menu.menu_sort);
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_due_date) {
                viewModel.setSortMode(SubscriptionsViewModel.SortMode.DUE_DATE);
                return true;
            } else if (itemId == R.id.sort_name) {
                viewModel.setSortMode(SubscriptionsViewModel.SortMode.NAME);
                return true;
            } else if (itemId == R.id.sort_cost) {
                viewModel.setSortMode(SubscriptionsViewModel.SortMode.COST);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void observeViewModel() {
        viewModel.getSortMode().observe(getViewLifecycleOwner(), mode -> {
            if (mode == null) return;
            int stringRes = switch (mode) {
                case NAME -> R.string.sort_by_name;
                case COST -> R.string.sort_by_cost;
                default -> R.string.sort_by_due_date;
            };
            binding.textSortMode.setText(stringRes);
        });

        viewModel.getActiveCount().observe(getViewLifecycleOwner(), count -> {
            int active = count != null ? count : 0;
            if (active <= 1) {
                binding.textActiveCount.setText(getString(R.string.active_count_singular, active));
            } else {
                binding.textActiveCount.setText(getString(R.string.active_count_plural, active));
            }
        });

        viewModel.getSubscriptions().observe(getViewLifecycleOwner(), subscriptions -> {
            boolean isEmpty = subscriptions == null || subscriptions.isEmpty();
            if (isEmpty) {
                binding.layoutSortBar.setVisibility(View.GONE);
                binding.recyclerViewSubscriptions.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                adapter.submitList(null);
            } else {
                binding.layoutSortBar.setVisibility(View.VISIBLE);
                binding.recyclerViewSubscriptions.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                adapter.submitList(subscriptions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
