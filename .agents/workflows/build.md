# Build Workflow (/build)

When running the `/build` workflow:

1. Read `AGENTS.md`, `docs/PROGRESS.md`, and `docs/PLAN.md`.
2. Check `docs/PROGRESS.md`:
   - If a pull request is pending in `Pending pull request`, show its review message again in chat, STOP, and wait for user approval or changes.
   - Otherwise, locate the first unfinished step in `docs/PLAN.md` (or the current step indicated in `docs/PROGRESS.md`).
3. Follow the **Pull-request protocol** in `AGENTS.md`:
   - Start from an up-to-date `main`. Create a branch `step-N-short-name`.
   - Implement only the step's scope. Make small commits with format `'F1: create subscription form'`.
   - Run verification commands (`./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, plus step-specific tests). Fix failures (stop after 3 failed attempts on the same issue).
   - Review your diff with the `subwatch-review` skill and fix any flagged issues.
   - Send the PR-style review message in chat, STOP and wait for approval ('approve' or list of changes).
   - Once 'approve' is received, switch to `main`, merge `--no-ff`, update `docs/PROGRESS.md` and `docs/PLAN.md` in one small commit, push to `origin main`, delete the local step branch, report the pushed commit hash.
   - Proceed to the next step.
