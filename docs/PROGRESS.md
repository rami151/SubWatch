# SubWatch Progress

## Current step
Step 1: Theme and navigation skeleton

## Pending pull request
Step 1: Theme and navigation skeleton (branch `step-1-theme-nav`)

## Done
| Step | Commit hash | Date |
|---|---|---|

## Decisions made during the build
- Pre-approved decisions D1 to D8 recorded in PLAN.md.

## Cahier changes to apply
- **RG3 rounding sentence**: Calculations are exact (annual cost in minor units = amount x cycles per year). Rounding (HALF_UP) is only applied when displaying monthly equivalent, and totals are summed before rounding.
- **RG6 is_trial flip**: When a trial end date has passed, `is_trial` becomes false and the next due date is recomputed.
- **RG1 cycle lengths and definition of today**: Weekly = 7 days, monthly = 1 month, quarterly = 3 months, yearly = 12 months. Today is local date from device clock (injected `Clock` in tests).
- **Section 8.4 self-rescheduling reminders**: Reminders use a self-rescheduling OneTimeWorkRequest (unique name, REPLACE), not a PeriodicWorkRequest.

## Manual checks pending
- Verify Step 1 UI on device / emulator: 3 bottom navigation tabs, large CollapsingToolbar with gear icon on main tabs, back arrow on Fiche and Settings, close (X) on Formulaire, bottom bar hidden on Formulaire and Settings.

## Open questions
None.
