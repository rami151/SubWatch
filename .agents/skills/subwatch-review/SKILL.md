---
name: subwatch-review
description: Review a change in SubWatch against the cahier and simplicity rules; use before every commit
---

# SubWatch Review Skill

Use this skill before every commit and before preparing a pull-request review message.

## Review Steps

1. **Requirements Coverage**:
   - List the functional (F) and business rule (RG) numbers that the change touches.
   - Verify whether each touched requirement is fully met and adheres to decisions D1–D8.

2. **Strict Architectural & Rule Checks**:
   - **No Kotlin source files**: Only Java 17 for app code (Kotlin DSL `.kts` for Gradle scripts only).
   - **No Compose**: Only XML + ViewBinding.
   - **Money precision**: Money must be stored and computed as integer minor units (`long` / `int` or integer minor unit math). Flag any usage of `float` or `double`.
   - **Date APIs**: Use `java.time` only (`LocalDate`, `Clock`). Flag any usage of `java.util.Date` or `java.util.Calendar`.
   - **Due Date occurrences**: `n = anchor + n * cycle`, never chained from previous dates (RG1, RG2).
   - **Database threading**: Room operations must never run on the main thread (Repository single-thread executor or LiveData/WorkManager background thread).
   - **No deprecated APIs**: Flag deprecated Android or Java APIs.
   - **No new dependencies**: Dependencies must be strictly as allowed in `PLAN.md`.
   - **No INTERNET permission**: Ensure `android.permission.INTERNET` is never present in `AndroidManifest.xml`.

3. **Code Simplicity & Cleanliness**:
   - Flag any dead code or unused methods/fields.
   - Flag unused imports.
   - Flag speculative abstractions and single-implementation interfaces/wrappers.
   - Flag comments that merely restate what the code does.

4. **Testing & Verification**:
   - Name any untested behavior, edge cases, or missing JUnit / instrumented tests.
   - Verify that test cases from `docs/billing-cases.md` match and are preserved.

5. **Output Format**:
   - Output a concise list of findings ordered by severity (Blocker, Major, Minor).
   - Do NOT include praise or filler text.
