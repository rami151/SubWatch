# SubWatch Progress

## Current step
Step 6: Dashboard and reminders

## Pending pull request
None.

## Done
| Step | Commit hash | Date |
|---|---|---|
| Step 1: Theme and navigation skeleton | `43f529d` | 2026-11-01 |
| Step 2: Billing and cost logic, tests first | `38208a4` | 2026-11-01 |
| Step 3: Room data layer per cahier section 8.3 | `b6b6b54` | 2026-11-01 |
| Step 4: Notification spike on branch spike/notifications (never merged) | `570cbce` | 2026-11-01 |
| Step 5: Formulaire, Abonnements list and Fiche, matching Figma designs | `f33e5f4` | 2026-11-24 |

## Decisions made during the build
- Pre-approved decisions D1 to D8 recorded in PLAN.md.
- **Design source of truth**: Switched from `docs/design/` images to the live [Figma design file](https://www.figma.com/design/3AGWKq5YJaOWEkUHlsqJN9/SubWatch) (`3AGWKq5YJaOWEkUHlsqJN9`). Theme tokens (light/dark) and typography (Gelasio Bold) corrected to match Figma.
- Step 5 decisions: common currency list (15 ISO currencies), decimal amount input in major units converted to minor units, sort mode reset on fragment creation, Archiver button visible but disabled on Fiche pending Step 7.

## Cahier changes to apply
- **RG3 rounding sentence**: Calculations are exact (annual cost in minor units = amount x cycles per year). Rounding (HALF_UP) is only applied when displaying monthly equivalent, and totals are summed before rounding.
- **RG6 is_trial flip**: When a trial end date has passed, `is_trial` becomes false and the next due date is recomputed.
- **RG1 cycle lengths and definition of today**: Weekly = 7 days, monthly = 1 month, quarterly = 3 months, yearly = 12 months. Today is local date from device clock (injected `Clock` in tests).
- **Section 8.4 self-rescheduling reminders**: Reminders use a self-rescheduling OneTimeWorkRequest (unique name, REPLACE), not a PeriodicWorkRequest.

## Manual checks pending
- Verify Step 1 UI on device / emulator: 3 bottom navigation tabs, large CollapsingToolbar with gear icon on main tabs, back arrow on Fiche and Settings, close (X) on Formulaire, bottom bar hidden on Formulaire and Settings.
- Verify Step 5 UI on device / emulator: Formulaire (creation & editing with date pickers), Abonnements list (sorting & empty state), and Fiche (details and deletion confirmation).

## Open questions
None.
