# SubWatch: BillingCalculator test cases

Source of truth for the unit tests of `domain/BillingCalculator`. Every expected value below was derived by hand from RG1, RG2, RG3 and RG8 of `docs/cahier-des-charges.md`, then confirmed with an independent reference calculation (0 mismatches out of 25 date cases, 11 cost cases, 4 total cases).

**Rule for the agent:** never change an expected value. If you think one is wrong, stop and say why.

## Conventions

- Dates are `java.time.LocalDate`, format `yyyy-MM-dd`. "Today" comes from an injected `java.time.Clock`, fixed by each test.
- The anchor is the start date, or the trial end date for a trial (RG1).
- Cycle lengths: weekly = 7 days, monthly = 1 month, quarterly = 3 months, yearly = 12 months.
- Occurrence `n` = anchor + n cycles, always computed from the anchor, never from the previous occurrence (RG1).
- Next due = the first occurrence on or after today, so n starts at 0 (RG1).
- If the anchor day does not exist in the target month, use the last day of that month. The anchor day itself never changes (RG2).
- Amounts are stored as integers in the smallest currency unit (RG8). EUR 15.99 is stored as 1599, JPY 1000 as 1000, KWD 12.500 as 12500.

## 1. Next due date

| ID | Anchor | Cycle | Today | Expected next due | n | Rule | Why |
|---|---|---|---|---|---|---|---|
| D01 | 2026-01-31 | monthly | 2026-02-01 | 2026-02-28 | 1 | RG2 | February 2026 has no 31st, so the last day |
| D02 | 2026-01-31 | monthly | 2026-03-01 | 2026-03-31 | 2 | RG2 | March has a 31st again: the anchor day comes back |
| D03 | 2026-01-31 | monthly | 2026-04-01 | 2026-04-30 | 3 | RG2 | April has 30 days |
| D04 | 2028-01-31 | monthly | 2028-02-01 | 2028-02-29 | 1 | RG2 | 2028 is a leap year |
| D05 | 2028-02-29 | yearly | 2028-03-01 | 2029-02-28 | 1 | RG2 | 2029 is not a leap year (RG2 names 29 February) |
| D06 | 2028-02-29 | yearly | 2031-03-01 | 2032-02-29 | 4 | RG2 | 2032 is a leap year: the 29th comes back |
| D07 | 2026-01-31 | quarterly | 2026-02-01 | 2026-04-30 | 1 | RG1, RG2 | quarterly = 3 months, April has 30 days |
| D08 | 2026-01-31 | quarterly | 2026-05-01 | 2026-07-31 | 2 | RG1, RG2 | July has 31 days |
| D09 | 2026-12-28 | weekly | 2026-12-29 | 2027-01-04 | 1 | RG1 | 7 days later, across the year boundary |
| D10 | 2026-10-12 | monthly | 2026-11-12 | 2026-11-12 | 1 | RG1 | an occurrence equal to today counts (greater than or equal) |
| D11 | 2026-12-01 | monthly | 2026-10-15 | 2026-12-01 | 0 | RG1 | anchor in the future: n = 0 |
| D12 | 2026-10-15 | monthly | 2026-10-15 | 2026-10-15 | 0 | RG1 | today is the anchor day |
| D13 | 2026-12-15 | monthly | 2026-12-16 | 2027-01-15 | 1 | RG1 | month rollover across the year boundary |
| D14 | 2026-01-30 | monthly | 2026-02-01 | 2026-02-28 | 1 | RG2 | anchor day 30 does not exist in February |
| D15 | 2026-01-30 | monthly | 2026-03-01 | 2026-03-30 | 2 | RG2 | March has a 30th: no drift to 28 |
| D16 | 2026-01-29 | monthly | 2026-02-01 | 2026-02-28 | 1 | RG2 | 29th does not exist in February 2026 |
| D17 | 2028-01-29 | monthly | 2028-02-01 | 2028-02-29 | 1 | RG2 | 29th exists in February 2028 |
| D18 | 2028-02-29 | monthly | 2028-03-01 | 2028-03-29 | 1 | RG2 | anchor day is 29: March 29, not March 31 |
| D19 | 2024-01-31 | monthly | 2026-10-15 | 2026-10-31 | 33 | RG1, RG2 | 33 months after the anchor, many occurrences skipped |
| D20 | 2025-06-10 | yearly | 2026-06-11 | 2027-06-10 | 2 | RG1 | the 2026 occurrence (06-10) is already past |
| D21 | 2026-11-30 | quarterly | 2026-12-01 | 2027-02-28 | 1 | RG1, RG2 | 3 months after Nov 30 lands in February 2027 |
| D22 | 2026-10-17 (trial end) | monthly | 2026-10-15 | 2026-10-17 | 0 | RG1, F8 | before the end of a trial, the next due date is the trial end |
| D23 | 2026-10-17 (trial end) | monthly | 2026-10-18 | 2026-11-17 | 1 | RG1, F8 | after the trial end, normal billing from that anchor |
| D24 | 2027-02-28 | yearly | 2027-03-01 | 2028-02-28 | 1 | RG2 | anchor day 28: stays 28 even though 2028 has a 29 February |
| D25 | 2024-01-01 | weekly | 2026-10-15 | 2026-10-19 | 146 | RG1 | 1018 days elapsed, the next multiple of 7 is 1022 days |

### Occurrence sequences

These catch the "chained from the previous date" bug.

| ID | Anchor | Cycle | Occurrences n = 0 to 5 |
|---|---|---|---|
| S1 | 2026-01-31 | monthly | 2026-01-31, 2026-02-28, 2026-03-31, 2026-04-30, 2026-05-31, 2026-06-30 |
| S2 | 2028-02-29 | yearly | 2028-02-29, 2029-02-28, 2030-02-28, 2031-02-28, 2032-02-29 (n = 0 to 4) |

A chained implementation gives 2026-01-31, 02-28, 03-28, 04-28, 05-28, 06-28 for S1. That is wrong.

## 2. Costs

Method (exact, no drift): annual cost in minor units = stored amount x cycles per year (weekly 52, monthly 12, quarterly 4, yearly 1). Monthly equivalent = annual / 12, rounded HALF_UP only for display. Mathematically this is RG3.

| ID | Amount | Currency (decimals) | Cycle | Stored | Annual | Monthly equivalent |
|---|---|---|---|---|---|---|
| C01 | 15.99 | EUR (2) | monthly | 1599 | 191.88 | 15.99 |
| C02 | 10.00 | EUR (2) | weekly | 1000 | 520.00 | 43.33 |
| C03 | 9.99 | EUR (2) | weekly | 999 | 519.48 | 43.29 |
| C04 | 30.00 | EUR (2) | quarterly | 3000 | 120.00 | 10.00 |
| C05 | 10.00 | EUR (2) | quarterly | 1000 | 40.00 | 3.33 |
| C06 | 120.00 | EUR (2) | yearly | 12000 | 120.00 | 10.00 |
| C07 | 99.99 | EUR (2) | yearly | 9999 | 99.99 | 8.33 (exact 8.3325) |
| C08 | 100.00 | EUR (2) | yearly | 10000 | 100.00 | 8.33 (exact 8.3333) |
| C09 | 1000 | JPY (0) | monthly | 1000 | 12000 | 1000 |
| C10 | 12.500 | KWD (3) | monthly | 12500 | 150.000 | 12.500 |
| C11 | 0.06 | EUR (2) | yearly | 6 | 0.06 | 0.01 (exact 0.005, tie rounds up) |

## 3. Totals

Totals are summed on exact annual values, then rounded once (RG4: one total per currency, never summed across currencies).

| ID | Subscriptions | Expected |
|---|---|---|
| T-A | EUR monthly: 15.99, 5.99, 1.99 | monthly 23.97, annual 287.64 |
| T-B | EUR yearly: 100.00 and 100.00 | monthly 16.67, annual 200.00 (not 16.66, which is the sum of rounded values) |
| T-C | EUR monthly 15.99 and USD monthly 10.00 | EUR: 15.99 / 191.88. USD: 10.00 / 120.00. Two separate totals |
| T-D | EUR monthly 15.99, 5.99, 1.99 plus Duolingo 12.99 in trial | 23.97 / 287.64, only if trials are excluded from totals (decision pending, see below) |

## 4. Findings in the cahier (to fix)

1. **RG3 does not say when to round.** Taken literally ("annual = monthly equivalent x 12" with the monthly value rounded first), weekly 10.00 gives 519.96 instead of 520.00 (C02), yearly 100.00 gives 99.96 (C08), and two yearly 100.00 give 16.66 instead of 16.67 (T-B). Proposed addition to RG3: *Les calculs sont exacts (coût annuel en unités mineures = montant x nombre de cycles par an). L'arrondi (HALF_UP) n'est appliqué qu'à l'affichage du coût mensuel, et les totaux sont additionnés avant arrondi.*
2. **BigDecimal pitfall.** `BigDecimal.divide(...)` without a scale or rounding mode throws `ArithmeticException` on non-terminating results such as x 52 / 12. Multiply first (exact), then divide by 12 with an explicit scale and `RoundingMode.HALF_UP`.
3. **Trial end is not covered by RG6.** After the trial end date passes, `is_trial` should become false and the next due date should be recomputed. Cases D22 and D23 assume this. Proposed addition to RG6.
4. **RG1 leaves cycle lengths implicit.** State them: weekly 7 days, quarterly 3 months, yearly 12 months (D05, D07, D09).
5. **"Today" is not defined.** Add: the local date from the device clock. Tests fix it with a `Clock`.
6. **Trials in totals (T-D) is undecided.** The mockups exclude trials. Decide and write it into the cahier before building the dashboard.
