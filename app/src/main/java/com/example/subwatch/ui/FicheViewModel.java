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
import java.util.Objects;

/**
 * ViewModel for the Fiche detail screen (UC3, F3).
 */
public class FicheViewModel extends AndroidViewModel {

    private final SubscriptionRepository repository;
    private final Clock clock;
    private final MutableLiveData<Long> subscriptionId = new MutableLiveData<>(-1L);
    private final LiveData<SubscriptionWithCategory> subscription;

    public FicheViewModel(@NonNull Application application) {
        this(application, SubscriptionRepository.getInstance(application), Clock.systemDefaultZone());
    }

    @VisibleForTesting
    public FicheViewModel(@NonNull Application application,
                          @NonNull SubscriptionRepository repository,
                          @NonNull Clock clock) {
        super(application);
        this.repository = repository;
        this.clock = clock;
        this.subscription = Transformations.switchMap(subscriptionId, id -> {
            if (id == null || id <= 0) {
                return new MutableLiveData<>(null);
            }
            return repository.getSubscriptionWithCategoryById(id);
        });
    }

    public void setSubscriptionId(long id) {
        if (!Objects.equals(subscriptionId.getValue(), id)) {
            subscriptionId.setValue(id);
        }
    }

    public long getSubscriptionId() {
        Long id = subscriptionId.getValue();
        return id != null ? id : -1L;
    }

    public LiveData<SubscriptionWithCategory> getSubscription() {
        return subscription;
    }

    public Clock getClock() {
        return clock;
    }

    public void deleteCurrent(Runnable onDeleted) {
        Long id = subscriptionId.getValue();
        if (id != null && id > 0) {
            repository.deleteById(id, onDeleted);
        }
    }
}
