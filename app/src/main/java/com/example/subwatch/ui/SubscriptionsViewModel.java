package com.example.subwatch.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.data.repository.SubscriptionRepository;

import java.time.Clock;
import java.util.List;

/**
 * ViewModel for the Abonnements list screen (F5).
 */
public class SubscriptionsViewModel extends AndroidViewModel {

    public enum SortMode {
        DUE_DATE,
        NAME,
        COST
    }

    private final SubscriptionRepository repository;
    private final Clock clock;

    private final MutableLiveData<SortMode> sortMode = new MutableLiveData<>(SortMode.DUE_DATE);
    private final LiveData<List<SubscriptionWithCategory>> subscriptions;
    private final LiveData<Integer> activeCount;

    public SubscriptionsViewModel(@NonNull Application application) {
        this(application, SubscriptionRepository.getInstance(application), Clock.systemDefaultZone());
    }

    @VisibleForTesting
    public SubscriptionsViewModel(@NonNull Application application,
                                  @NonNull SubscriptionRepository repository,
                                  @NonNull Clock clock) {
        super(application);
        this.repository = repository;
        this.clock = clock;
        this.activeCount = repository.getActiveCount();

        this.subscriptions = Transformations.switchMap(sortMode, mode -> {
            if (mode == null) mode = SortMode.DUE_DATE;
            return switch (mode) {
                case NAME -> repository.getActiveSubscriptionsWithCategorySortedByName();
                case COST -> repository.getActiveSubscriptionsWithCategorySortedByCost();
                default -> repository.getActiveSubscriptionsWithCategorySortedByDueDate();
            };
        });
    }

    public LiveData<SortMode> getSortMode() {
        return sortMode;
    }

    public void setSortMode(@NonNull SortMode mode) {
        if (sortMode.getValue() != mode) {
            sortMode.setValue(mode);
        }
    }

    public LiveData<List<SubscriptionWithCategory>> getSubscriptions() {
        return subscriptions;
    }

    public LiveData<Integer> getActiveCount() {
        return activeCount;
    }

    public Clock getClock() {
        return clock;
    }
}
