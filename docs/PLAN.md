# SubWatch Implementation Plan

## Pre-approved decisions

- **D1**: Trials are excluded from totals and statistics. The dashboard shows '+ X / mois après l'essai' under the totals.
- **D2**: Statistics and ranking use the default currency only (RG9), with a visible notice naming the excluded subscriptions.
- **D3**: Reminders use a self-rescheduling OneTimeWorkRequest (unique name, REPLACE), not a PeriodicWorkRequest.
- **D4**: Costs: annual cost in minor units = stored amount x cycles per year (weekly 52, monthly 12, quarterly 4, yearly 1). Monthly equivalent = annual / 12, rounded HALF_UP for display only. Totals are summed on annual values before rounding.
- **D5**: When a trial end date has passed, is_trial becomes false and the next due date is recomputed.
- **D6**: Reminder selection logic lives in a pure Java class ReminderSelector (domain/) with unit tests. The Worker only calls it.
- **D7**: CSV export goes to the app-specific files directory and is shared through FileProvider and the system share sheet.
- **D8**: Defaults: reminder delay 3 days (range 0 to 14), time 09:00, default currency EUR, theme follows the system.

## Theme tokens (Material 3 roles, DayNight)

- **Light**: background `#F5F2E9`, surface `#FFFDF7`, surfaceVariant `#EAE7DC`, primary `#285644`, onPrimary `#F5F2E9`, onSurface `#26322D`, onSurfaceVariant `#5C675F`, outlineVariant `#DEDDD2`, secondaryContainer `#EDF2E9`, error `#B3261E`, warning `#8A5A00`.
- **Dark**: background `#202824`, surface `#29332E`, surfaceVariant `#35413A`, primary `#A3C5A9`, onPrimary `#202824`, onSurface `#EDF0E7`, onSurfaceVariant `#B1BCB1`, outlineVariant `#465249`, secondaryContainer `#303D34`, error `#F2B8B5`, warning `#E6B450`.

## Steps (each one is one pull request)

- [x] **Step 1: Theme and navigation skeleton**: theme tokens, serif font for headlines only (res/font if present, else fontFamily serif), portrait only, edge-to-edge, one Activity with Navigation, 3 bottom tabs, Fiche/Formulaire/Paramètres destinations, large top app bar with gear icon, back arrows, Formulaire without bottom bar.
- [x] **Step 2: Billing and cost logic, tests first**: read docs/billing-cases.md. Write the JUnit tests for sections 1 to 3 (D01 to D25, S1, S2, C01 to C11, T-A to T-D) and see them fail, then implement BillingCalculator (due dates, Clock) and CostCalculator (costs and totals per currency) in domain/, pure Java.
- [ ] **Step 3: Room data layer per cahier section 8.3**: entities, converters, DAO, database with exportSchema and 7 pre-filled categories, Repository with a single-thread executor, instrumented DAO tests.
- [ ] **Step 4: Notification spike on branch spike/notifications (never merged)**: channel, POST_NOTIFICATIONS, a temporary button on Paramètres firing a notification in 1 minute through WorkManager. In your review message give me 5 phone tests (app open, swiped away, rebooted, permission denied, battery saver) and wait for my results before closing the step.
- [ ] **Step 5: Formulaire, Abonnements list and Fiche, matching the mockups**: validation (F1, RG10), amounts as minor units, MaterialDatePicker, sort menu, extended FAB, delete confirmation dialog.
- [ ] **Step 6: Dashboard and reminders**: per-currency summary blocks, upcoming list, trials strip, permission banner, ReminderSelector with tests, ReminderWorker, minimal reminder settings. Milestone: build a debug APK and ask me to use it for a day and report T3, T4, T7 before closing the step.
- [ ] **Step 7: P2 features**: archive and reactivate, filters and search chips, Statistiques with a Canvas pie chart (no chart library), complete Paramètres (theme, default currency, CSV export), dark theme pass.
- [ ] **Step 8: Quality and release prep**: audit of every F and RG into docs/AUDIT.md, manifest and Gradle audit, log audit, debug-only action inserting 200 subscriptions, R8 enabled with an unsigned assembleRelease. I create the signing keystore myself.
- [ ] **Step 9: Report support**: an outline of the project report from docs/PROGRESS.md and the git log. Do not write the report text.
