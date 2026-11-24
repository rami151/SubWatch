package com.example.subwatch.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.repository.SubscriptionRepository;
import com.example.subwatch.domain.AmountFormatter;
import com.example.subwatch.domain.BillingCalculator;
import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for subscription creation and editing (F1, F2, RG10).
 */
public class FormulaireViewModel extends AndroidViewModel {

    public static final List<String> COMMON_CURRENCIES = Collections.unmodifiableList(Arrays.asList(
            "EUR", "USD", "GBP", "CHF", "CAD", "AUD", "JPY", "KWD", "BHD", "CNY", "INR", "BRL", "MAD", "TND", "SAR"
    ));

    public static final String FIELD_NAME = "name";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_TRIAL_END_DATE = "trialEndDate";
    public static final String FIELD_NOTES = "notes";

    private final SubscriptionRepository repository;
    private final Clock clock;

    private long subscriptionId = -1L;
    private boolean isLoaded = false;
    private Instant existingCreatedAt;
    private SubscriptionStatus existingStatus = SubscriptionStatus.ACTIVE;
    private LocalDate existingLastNotifiedDue;

    private final MutableLiveData<String> name = new MutableLiveData<>("");
    private final MutableLiveData<String> amount = new MutableLiveData<>("");
    private final MutableLiveData<String> currency = new MutableLiveData<>("EUR");
    private final MutableLiveData<BillingCycle> billingCycle = new MutableLiveData<>(BillingCycle.MONTHLY);
    private final MutableLiveData<LocalDate> startDate = new MutableLiveData<>();
    private final MutableLiveData<Long> categoryId = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isTrial = new MutableLiveData<>(false);
    private final MutableLiveData<LocalDate> trialEndDate = new MutableLiveData<>(null);
    private final MutableLiveData<String> notes = new MutableLiveData<>("");

    private final MutableLiveData<Map<String, String>> errors = new MutableLiveData<>(new HashMap<>());
    private final SingleLiveEvent<Boolean> saveSuccess = new SingleLiveEvent<>();
    private final LiveData<List<Category>> allCategories;

    public FormulaireViewModel(@NonNull Application application) {
        this(application, SubscriptionRepository.getInstance(application), Clock.systemDefaultZone());
    }

    @VisibleForTesting
    public FormulaireViewModel(@NonNull Application application,
                               @NonNull SubscriptionRepository repository,
                               @NonNull Clock clock) {
        super(application);
        this.repository = repository;
        this.clock = clock;
        this.allCategories = repository.getAllCategories();
        this.startDate.setValue(LocalDate.now(clock));
    }

    public LiveData<String> getName() {
        return name;
    }

    public void setName(String value) {
        name.setValue(value);
    }

    public LiveData<String> getAmount() {
        return amount;
    }

    public void setAmount(String value) {
        amount.setValue(value);
    }

    public LiveData<String> getCurrency() {
        return currency;
    }

    public void setCurrency(String value) {
        currency.setValue(value);
    }

    public LiveData<BillingCycle> getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle value) {
        billingCycle.setValue(value);
    }

    public LiveData<LocalDate> getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate value) {
        startDate.setValue(value);
    }

    public LiveData<Long> getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable Long value) {
        categoryId.setValue(value);
    }

    public LiveData<Boolean> getIsTrial() {
        return isTrial;
    }

    public void setIsTrial(boolean value) {
        isTrial.setValue(value);
    }

    public LiveData<LocalDate> getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(@Nullable LocalDate value) {
        trialEndDate.setValue(value);
    }

    public LiveData<String> getNotes() {
        return notes;
    }

    public void setNotes(String value) {
        notes.setValue(value);
    }

    public LiveData<Map<String, String>> getErrors() {
        return errors;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public boolean isEditMode() {
        return subscriptionId > 0;
    }

    public long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Initializes the ViewModel for edit mode by loading the subscription data.
     */
    public void loadSubscription(long id) {
        if (id <= 0 || (isLoaded && subscriptionId == id)) {
            return;
        }
        this.subscriptionId = id;

        // Fetch synchronously on repository executor or observer
        LiveData<Subscription> liveSub = repository.getSubscriptionById(id);
        liveSub.observeForever(new androidx.lifecycle.Observer<Subscription>() {
            @Override
            public void onChanged(Subscription sub) {
                if (sub != null && !isLoaded) {
                    isLoaded = true;
                    name.setValue(sub.getName());
                    amount.setValue(AmountFormatter.formatToDecimalString(sub.getAmountMinor(), sub.getCurrency()));
                    currency.setValue(sub.getCurrency());
                    billingCycle.setValue(sub.getBillingCycle());
                    startDate.setValue(sub.getStartDate());
                    categoryId.setValue(sub.getCategoryId());
                    isTrial.setValue(sub.isTrial());
                    trialEndDate.setValue(sub.getTrialEndDate());
                    notes.setValue(sub.getNotes() != null ? sub.getNotes() : "");
                    existingCreatedAt = sub.getCreatedAt();
                    existingStatus = sub.getStatus();
                    existingLastNotifiedDue = sub.getLastNotifiedDue();
                }
                liveSub.removeObserver(this);
            }
        });
    }

    /**
     * Validates all form fields per F1 and RG10.
     *
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        Map<String, String> newErrors = new HashMap<>();

        // Nom validation (F1)
        String currentName = name.getValue();
        if (currentName == null || currentName.trim().isEmpty()) {
            newErrors.put(FIELD_NAME, "Champ requis. Saisissez le nom de l'abonnement.");
        } else if (currentName.trim().length() > 50) {
            newErrors.put(FIELD_NAME, "50 caractères maximum.");
        }

        // Montant validation (F1, RG8)
        String currentAmount = amount.getValue();
        String currentCurrency = currency.getValue() != null ? currency.getValue() : "EUR";
        if (currentAmount == null || currentAmount.trim().isEmpty()) {
            newErrors.put(FIELD_AMOUNT, "Champ requis. Saisissez un montant.");
        } else {
            try {
                long amountMinor = AmountFormatter.parseToMinorUnits(currentAmount, currentCurrency);
                if (amountMinor <= 0) {
                    newErrors.put(FIELD_AMOUNT, "Le montant doit être supérieur à 0.");
                }
            } catch (Exception e) {
                newErrors.put(FIELD_AMOUNT, "Le montant doit être supérieur à 0.");
            }
        }

        // Date de début validation
        LocalDate currentStartDate = startDate.getValue();
        if (currentStartDate == null) {
            newErrors.put(FIELD_START_DATE, "Saisissez une date de début.");
        }

        // Période d'essai validation (F7, RG10)
        Boolean trial = isTrial.getValue();
        if (Boolean.TRUE.equals(trial)) {
            LocalDate currentTrialEnd = trialEndDate.getValue();
            if (currentTrialEnd == null) {
                newErrors.put(FIELD_TRIAL_END_DATE, "Saisissez la date de fin d'essai.");
            } else if (currentStartDate != null && !currentTrialEnd.isAfter(currentStartDate)) {
                // RG10: strictly after startDate
                newErrors.put(FIELD_TRIAL_END_DATE, "La date de fin d'essai doit suivre la date de début.");
            }
        }

        // Notes validation (F1: 200 chars max)
        String currentNotes = notes.getValue();
        if (currentNotes != null && currentNotes.length() > 200) {
            newErrors.put(FIELD_NOTES, "200 caractères maximum.");
        }

        errors.setValue(newErrors);
        return newErrors.isEmpty();
    }

    /**
     * Saves the subscription (insert if new, update if edit).
     */
    public void save() {
        if (!validate()) {
            return;
        }

        String currentName = name.getValue().trim();
        String currentCurrency = currency.getValue() != null ? currency.getValue() : "EUR";
        long amountMinor = AmountFormatter.parseToMinorUnits(amount.getValue(), currentCurrency);
        BillingCycle cycle = billingCycle.getValue() != null ? billingCycle.getValue() : BillingCycle.MONTHLY;
        LocalDate start = startDate.getValue();
        boolean trial = Boolean.TRUE.equals(isTrial.getValue());
        LocalDate trialEnd = trial ? trialEndDate.getValue() : null;
        Long catId = categoryId.getValue();
        String currentNotes = notes.getValue();
        String cleanNotes = (currentNotes != null && !currentNotes.trim().isEmpty()) ? currentNotes.trim() : null;

        // Compute next due date (RG1, F8)
        BillingCalculator calculator = new BillingCalculator(clock);
        LocalDate nextDue;
        if (trial && trialEnd != null) {
            nextDue = calculator.nextDueDateWithTrial(trialEnd, cycle);
        } else {
            nextDue = calculator.nextDueDate(start, cycle);
        }

        Instant now = Instant.now(clock);

        if (isEditMode()) {
            Subscription updated = new Subscription.Builder()
                    .id(subscriptionId)
                    .name(currentName)
                    .amountMinor(amountMinor)
                    .currency(currentCurrency)
                    .billingCycle(cycle)
                    .startDate(start)
                    .nextDueDate(nextDue)
                    .categoryId(catId)
                    .isTrial(trial)
                    .trialEndDate(trialEnd)
                    .status(existingStatus != null ? existingStatus : SubscriptionStatus.ACTIVE)
                    .lastNotifiedDue(existingLastNotifiedDue)
                    .notes(cleanNotes)
                    .createdAt(existingCreatedAt != null ? existingCreatedAt : now)
                    .updatedAt(now)
                    .build();
            repository.update(updated, () -> saveSuccess.postValue(true));
        } else {
            Subscription created = new Subscription.Builder()
                    .name(currentName)
                    .amountMinor(amountMinor)
                    .currency(currentCurrency)
                    .billingCycle(cycle)
                    .startDate(start)
                    .nextDueDate(nextDue)
                    .categoryId(catId)
                    .isTrial(trial)
                    .trialEndDate(trialEnd)
                    .status(SubscriptionStatus.ACTIVE)
                    .notes(cleanNotes)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            repository.insert(created, id -> saveSuccess.postValue(true));
        }
    }
}
