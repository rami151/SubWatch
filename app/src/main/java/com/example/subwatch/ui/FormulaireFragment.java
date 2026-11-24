package com.example.subwatch.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.subwatch.R;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.databinding.FragmentFormulaireBinding;
import com.example.subwatch.domain.BillingCycle;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Fragment for creating and editing a subscription (F1, F2, RG10).
 */
public class FormulaireFragment extends Fragment {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FragmentFormulaireBinding binding;
    private FormulaireViewModel viewModel;
    private NavController navController;

    private final List<Category> currentCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFormulaireBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FormulaireViewModel.class);
        navController = NavHostFragment.findNavController(this);

        long subscriptionId = -1L;
        if (getArguments() != null) {
            subscriptionId = getArguments().getLong("subscriptionId", -1L);
        }

        if (subscriptionId > 0) {
            viewModel.loadSubscription(subscriptionId);
            CollapsingToolbarLayout collapsingToolbar = requireActivity().findViewById(R.id.collapsingToolbar);
            if (collapsingToolbar != null) {
                collapsingToolbar.setTitle(getString(R.string.title_formulaire_edit));
            }
        }

        setupDropdowns();
        setupDatePickers();
        setupInputWatchers();
        observeViewModel();
    }

    private void setupDropdowns() {
        // Currencies
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                FormulaireViewModel.COMMON_CURRENCIES
        );
        binding.dropdownCurrency.setAdapter(currencyAdapter);
        binding.dropdownCurrency.setOnItemClickListener((parent, view, position, id) -> {
            String selected = FormulaireViewModel.COMMON_CURRENCIES.get(position);
            viewModel.setCurrency(selected);
        });

        // Billing cycles
        List<String> cycleLabels = List.of(
                getString(R.string.cycle_weekly),
                getString(R.string.cycle_monthly),
                getString(R.string.cycle_quarterly),
                getString(R.string.cycle_yearly)
        );
        List<BillingCycle> cycleValues = List.of(
                BillingCycle.WEEKLY,
                BillingCycle.MONTHLY,
                BillingCycle.QUARTERLY,
                BillingCycle.YEARLY
        );
        ArrayAdapter<String> cycleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                cycleLabels
        );
        binding.dropdownCycle.setAdapter(cycleAdapter);
        binding.dropdownCycle.setOnItemClickListener((parent, view, position, id) ->
                viewModel.setBillingCycle(cycleValues.get(position)));

        // Categories (observed dynamically from Room)
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            currentCategories.clear();
            List<String> categoryLabels = new ArrayList<>();
            categoryLabels.add(getString(R.string.category_uncategorized)); // null category

            if (categories != null) {
                currentCategories.addAll(categories);
                for (Category cat : categories) {
                    categoryLabels.add(cat.getName());
                }
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categoryLabels
            );
            binding.dropdownCategory.setAdapter(categoryAdapter);

            updateSelectedCategoryLabel(viewModel.getCategoryId().getValue());
        });

        binding.dropdownCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                viewModel.setCategoryId(null);
            } else {
                Category cat = currentCategories.get(position - 1);
                viewModel.setCategoryId(cat.getId());
            }
        });

        // Trial switch
        binding.switchTrial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setIsTrial(isChecked);
            binding.layoutTrialEndDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void updateSelectedCategoryLabel(@Nullable Long selectedCatId) {
        if (selectedCatId == null) {
            binding.dropdownCategory.setText(getString(R.string.category_uncategorized), false);
            return;
        }
        for (Category cat : currentCategories) {
            if (cat.getId() == selectedCatId) {
                binding.dropdownCategory.setText(cat.getName(), false);
                return;
            }
        }
        binding.dropdownCategory.setText(getString(R.string.category_uncategorized), false);
    }

    private void setupDatePickers() {
        View.OnClickListener startPickerListener = v -> {
            LocalDate current = viewModel.getStartDate().getValue();
            showDatePicker(current != null ? current : LocalDate.now(), selectedDate -> {
                viewModel.setStartDate(selectedDate);
                binding.editStartDate.setText(selectedDate.format(DATE_FORMATTER));
            });
        };
        binding.editStartDate.setOnClickListener(startPickerListener);
        binding.layoutStartDate.setEndIconOnClickListener(startPickerListener);

        View.OnClickListener trialPickerListener = v -> {
            LocalDate current = viewModel.getTrialEndDate().getValue();
            LocalDate start = viewModel.getStartDate().getValue();
            LocalDate defaultDate = current != null ? current : (start != null ? start.plusDays(7) : LocalDate.now());
            showDatePicker(defaultDate, selectedDate -> {
                viewModel.setTrialEndDate(selectedDate);
                binding.editTrialEndDate.setText(selectedDate.format(DATE_FORMATTER));
            });
        };
        binding.editTrialEndDate.setOnClickListener(trialPickerListener);
        binding.layoutTrialEndDate.setEndIconOnClickListener(trialPickerListener);
    }

    private void showDatePicker(@NonNull LocalDate initialDate, @NonNull Consumer<LocalDate> onDateSelected) {
        long epochMilli = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setSelection(epochMilli)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                LocalDate date = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();
                onDateSelected.accept(date);
            }
        });

        picker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void setupInputWatchers() {
        binding.editName.addTextChangedListener(new SimpleTextWatcher(viewModel::setName));
        binding.editAmount.addTextChangedListener(new SimpleTextWatcher(viewModel::setAmount));
        binding.editNotes.addTextChangedListener(new SimpleTextWatcher(viewModel::setNotes));

        binding.buttonSave.setOnClickListener(v -> {
            // Push latest input values
            viewModel.setName(Objects.requireNonNull(binding.editName.getText()).toString());
            viewModel.setAmount(Objects.requireNonNull(binding.editAmount.getText()).toString());
            viewModel.setNotes(Objects.requireNonNull(binding.editNotes.getText()).toString());

            viewModel.save();
        });
    }

    private void observeViewModel() {
        viewModel.getName().observe(getViewLifecycleOwner(), name -> {
            if (!Objects.equals(binding.editName.getText() != null ? binding.editName.getText().toString() : "", name)) {
                binding.editName.setText(name);
            }
        });

        viewModel.getAmount().observe(getViewLifecycleOwner(), amount -> {
            if (!Objects.equals(binding.editAmount.getText() != null ? binding.editAmount.getText().toString() : "", amount)) {
                binding.editAmount.setText(amount);
            }
        });

        viewModel.getCurrency().observe(getViewLifecycleOwner(), currency ->
                binding.dropdownCurrency.setText(currency, false));

        viewModel.getBillingCycle().observe(getViewLifecycleOwner(), cycle -> {
            if (cycle != null) {
                int index = switch (cycle) {
                    case WEEKLY -> 0;
                    case MONTHLY -> 1;
                    case QUARTERLY -> 2;
                    case YEARLY -> 3;
                };
                String[] labels = new String[]{
                        getString(R.string.cycle_weekly),
                        getString(R.string.cycle_monthly),
                        getString(R.string.cycle_quarterly),
                        getString(R.string.cycle_yearly)
                };
                binding.dropdownCycle.setText(labels[index], false);
            }
        });

        viewModel.getStartDate().observe(getViewLifecycleOwner(), startDate -> {
            if (startDate != null) {
                binding.editStartDate.setText(startDate.format(DATE_FORMATTER));
            }
        });

        viewModel.getCategoryId().observe(getViewLifecycleOwner(), this::updateSelectedCategoryLabel);

        viewModel.getIsTrial().observe(getViewLifecycleOwner(), isTrial -> {
            boolean checked = Boolean.TRUE.equals(isTrial);
            if (binding.switchTrial.isChecked() != checked) {
                binding.switchTrial.setChecked(checked);
            }
            binding.layoutTrialEndDate.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        viewModel.getTrialEndDate().observe(getViewLifecycleOwner(), trialEnd -> {
            if (trialEnd != null) {
                binding.editTrialEndDate.setText(trialEnd.format(DATE_FORMATTER));
            } else {
                binding.editTrialEndDate.setText("");
            }
        });

        viewModel.getNotes().observe(getViewLifecycleOwner(), notes -> {
            if (!Objects.equals(binding.editNotes.getText() != null ? binding.editNotes.getText().toString() : "", notes)) {
                binding.editNotes.setText(notes);
            }
        });

        viewModel.getErrors().observe(getViewLifecycleOwner(), this::displayErrors);

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                navController.navigateUp();
            }
        });
    }

    private void displayErrors(Map<String, String> errors) {
        if (errors == null) return;

        setFieldError(binding.layoutName, errors.get(FormulaireViewModel.FIELD_NAME));
        setFieldError(binding.layoutAmount, errors.get(FormulaireViewModel.FIELD_AMOUNT));
        setFieldError(binding.layoutStartDate, errors.get(FormulaireViewModel.FIELD_START_DATE));
        setFieldError(binding.layoutTrialEndDate, errors.get(FormulaireViewModel.FIELD_TRIAL_END_DATE));
        setFieldError(binding.layoutNotes, errors.get(FormulaireViewModel.FIELD_NOTES));
    }

    private void setFieldError(com.google.android.material.textfield.TextInputLayout layout, @Nullable String error) {
        if (error != null) {
            layout.setError(error);
        } else {
            layout.setError(null);
            layout.setErrorEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class SimpleTextWatcher implements TextWatcher {
        private final Consumer<String> onTextChanged;

        SimpleTextWatcher(Consumer<String> onTextChanged) {
            this.onTextChanged = onTextChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onTextChanged.accept(s != null ? s.toString() : "");
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
