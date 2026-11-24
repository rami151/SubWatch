package com.example.subwatch.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.subwatch.R;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.databinding.ItemSubscriptionBinding;
import com.example.subwatch.domain.AmountFormatter;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * RecyclerView adapter for subscriptions in the Abonnements tab (F5).
 */
public class SubscriptionAdapter extends ListAdapter<SubscriptionWithCategory, SubscriptionAdapter.ViewHolder> {

    private final Clock clock;
    private final Consumer<SubscriptionWithCategory> onItemClick;

    public SubscriptionAdapter(@NonNull Clock clock, @NonNull Consumer<SubscriptionWithCategory> onItemClick) {
        super(DIFF_CALLBACK);
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.onItemClick = Objects.requireNonNull(onItemClick, "onItemClick must not be null");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubscriptionBinding binding = ItemSubscriptionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), clock, onItemClick);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemSubscriptionBinding binding;

        public ViewHolder(@NonNull ItemSubscriptionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(@NonNull SubscriptionWithCategory item,
                         @NonNull Clock clock,
                         @NonNull Consumer<SubscriptionWithCategory> onItemClick) {
            Context context = itemView.getContext();
            Subscription sub = item.getSubscription();
            Category cat = item.getCategory();

            // Date block (day + short month)
            LocalDate nextDue = sub.getNextDueDate();
            binding.textDateDay.setText(String.valueOf(nextDue.getDayOfMonth()));
            String monthName = nextDue.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE);
            binding.textDateMonth.setText(monthName);

            // Category initial circle
            String catName = (cat != null && cat.getName() != null && !cat.getName().trim().isEmpty())
                    ? cat.getName()
                    : context.getString(R.string.category_uncategorized);
            int catColor = cat != null ? cat.getColor() : 0xFF5C675F;
            String initial = catName.substring(0, 1).toUpperCase(Locale.FRANCE);
            binding.textCategoryInitial.setText(initial);
            if (binding.textCategoryInitial.getBackground() instanceof GradientDrawable) {
                ((GradientDrawable) binding.textCategoryInitial.getBackground().mutate()).setColor(catColor);
            }

            // Subscription name
            binding.textSubscriptionName.setText(sub.getName());

            // Relative date
            LocalDate today = LocalDate.now(clock);
            long days = ChronoUnit.DAYS.between(today, nextDue);
            boolean isTrial = sub.isTrial();
            String relativeText;
            if (isTrial) {
                if (days <= 0) {
                    relativeText = context.getString(R.string.trial_end_today);
                } else if (days == 1) {
                    relativeText = context.getString(R.string.trial_end_tomorrow);
                } else {
                    relativeText = context.getString(R.string.trial_end_in_days, (int) days);
                }
            } else {
                if (days <= 0) {
                    relativeText = context.getString(R.string.due_today);
                } else if (days == 1) {
                    relativeText = context.getString(R.string.due_tomorrow);
                } else {
                    relativeText = context.getString(R.string.due_in_days, (int) days);
                }
            }
            binding.textDueRelative.setText(relativeText);

            // Billing cycle + category
            String cycleText = switch (sub.getBillingCycle()) {
                case WEEKLY -> context.getString(R.string.cycle_weekly);
                case MONTHLY -> context.getString(R.string.cycle_monthly);
                case QUARTERLY -> context.getString(R.string.cycle_quarterly);
                case YEARLY -> context.getString(R.string.cycle_yearly);
            };
            binding.textCycleAndCategory.setText(cycleText + " \u00B7 " + catName);
            binding.badgeTrial.setVisibility(isTrial ? View.VISIBLE : View.GONE);

            // Amount
            binding.textAmount.setText(AmountFormatter.formatForDisplay(sub.getAmountMinor(), sub.getCurrency()));
            binding.textAfterTrial.setVisibility(isTrial ? View.VISIBLE : View.GONE);

            // Item click
            itemView.setOnClickListener(v -> onItemClick.accept(item));
        }
    }

    private static final DiffUtil.ItemCallback<SubscriptionWithCategory> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SubscriptionWithCategory>() {
                @Override
                public boolean areItemsTheSame(@NonNull SubscriptionWithCategory oldItem,
                                               @NonNull SubscriptionWithCategory newItem) {
                    return oldItem.getSubscription().getId() == newItem.getSubscription().getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull SubscriptionWithCategory oldItem,
                                                  @NonNull SubscriptionWithCategory newItem) {
                    return Objects.equals(oldItem, newItem);
                }
            };
}
