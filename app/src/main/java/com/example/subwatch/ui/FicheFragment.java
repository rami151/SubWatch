package com.example.subwatch.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.subwatch.R;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.databinding.FragmentFicheBinding;
import com.example.subwatch.domain.AmountFormatter;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Fragment displaying full details of a subscription (UC3, F3).
 */
public class FicheFragment extends Fragment {

    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRANCE);

    private FragmentFicheBinding binding;
    private FicheViewModel viewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFicheBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FicheViewModel.class);
        navController = NavHostFragment.findNavController(this);

        long subscriptionId = -1L;
        if (getArguments() != null) {
            subscriptionId = getArguments().getLong("subscriptionId", -1L);
        }
        viewModel.setSubscriptionId(subscriptionId);

        setupActions();
        observeViewModel();
    }

    private void setupActions() {
        binding.buttonEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("subscriptionId", viewModel.getSubscriptionId());
            navController.navigate(R.id.action_fiche_to_formulaire, args);
        });

        binding.buttonDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_confirm_delete, (dialog, which) ->
                        viewModel.deleteCurrent(() -> navController.navigateUp()))
                .show();
    }

    private void observeViewModel() {
        viewModel.getSubscription().observe(getViewLifecycleOwner(), this::bindSubscription);
    }

    private void bindSubscription(@Nullable SubscriptionWithCategory item) {
        if (item == null) {
            return;
        }

        Subscription sub = item.getSubscription();
        Category cat = item.getCategory();
        Clock clock = viewModel.getClock();

        // Title in top CollapsingToolbar
        CollapsingToolbarLayout collapsingToolbar = requireActivity().findViewById(R.id.collapsingToolbar);
        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(sub.getName());
        }

        // Amount & Cycle
        binding.textFicheAmount.setText(AmountFormatter.formatForDisplay(sub.getAmountMinor(), sub.getCurrency()));
        String cycleLabel = switch (sub.getBillingCycle()) {
            case WEEKLY -> getString(R.string.cycle_weekly);
            case MONTHLY -> getString(R.string.cycle_monthly);
            case QUARTERLY -> getString(R.string.cycle_quarterly);
            case YEARLY -> getString(R.string.cycle_yearly);
        };
        binding.textFicheCycle.setText("/ " + cycleLabel);

        // Due date row
        LocalDate nextDue = sub.getNextDueDate();
        LocalDate today = LocalDate.now(clock);
        long days = ChronoUnit.DAYS.between(today, nextDue);
        String dayMonth = nextDue.getDayOfMonth() + " " + nextDue.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE);

        String relativeText;
        if (sub.isTrial()) {
            if (days <= 0) {
                relativeText = getString(R.string.trial_end_today);
            } else if (days == 1) {
                relativeText = getString(R.string.trial_end_tomorrow);
            } else {
                relativeText = getString(R.string.trial_end_in_days, (int) days);
            }
        } else {
            if (days <= 0) {
                relativeText = getString(R.string.due_today);
            } else if (days == 1) {
                relativeText = getString(R.string.due_tomorrow);
            } else {
                relativeText = getString(R.string.due_in_days, (int) days);
            }
        }
        binding.textFicheDueDate.setText(dayMonth + " \u00B7 " + relativeText);

        // Start date row
        binding.textFicheStartDate.setText(FULL_DATE_FORMATTER.format(sub.getStartDate()));

        // Category row
        String categoryName = (cat != null && cat.getName() != null && !cat.getName().trim().isEmpty())
                ? cat.getName()
                : getString(R.string.category_uncategorized);
        binding.textFicheCategory.setText(categoryName);

        // Notes row
        String notes = sub.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            binding.textFicheNotes.setText(notes);
        } else {
            binding.textFicheNotes.setText(getString(R.string.fiche_no_notes));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
