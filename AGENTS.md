# AGENTS.md

## Project Rules & Architecture

- **Stack**: Java 17, XML + ViewBinding, NO Kotlin source files, NO Compose. Gradle scripts in Kotlin DSL (.kts). minSdk 26, compileSdk/targetSdk 36. One Activity + Fragments + Jetpack Navigation. Fragment -> ViewModel (LiveData) -> Repository -> DAO (Room via annotationProcessor). WorkManager only, no exact alarms. No INTERNET permission. No new dependency unless PLAN.md lists it.
- **Domain**: java.time only with an injected Clock. Money as integer minor units with exact arithmetic, never float or double. Due date occurrence n = anchor + n cycles, never chained (RG1, RG2). Business logic in domain/ as pure Java with JUnit tests.
- **Code**: no database access on the main thread; no deprecated APIs; UI text in strings.xml (French); colors and dimens in resources; match the mockups, never redesign; simple commented code, no dead code, no speculative abstractions.
- **Integrity**: Never edit the cahier, the mockups, or the expected values in docs/billing-cases.md.

## Pull-request protocol
I review each step like a pull request. Nothing reaches GitHub before I approve.
1. Start from an up-to-date main. Create a branch step-N-short-name.
2. Implement only the step's scope. Make small commits, message format 'F1: create subscription form'.
3. Run the step's verification commands (gradlew testDebugUnitTest, gradlew assembleDebug, plus the step's own). Fix failures. After 3 failed attempts on the same problem, stop and report.
4. Review your own diff with the subwatch-review skill and fix what it flags.
5. Send a PR-style review message in chat, then STOP and wait:
   - Title and a 2-sentence summary.
   - Changes: git diff --stat main...HEAD, grouped by area.
   - Requirements covered (F and RG numbers) and tests (commands and counts).
   - Decisions or deviations from the plan or the cahier.
   - Read first: the 3 files most worth my attention, and why.
   - Manual checks for me (phone, visual match against the mockups).
   - Explain-it check: 2 or 3 things I should be able to explain about this step.
   - End with: reply 'approve' or list the changes you want.
6. If I request changes, fix them on the same branch with new commits and send an updated review message. Repeat until I approve.
7. Only after I write 'approve': switch to main, merge the branch with --no-ff, update docs/PROGRESS.md and tick the step in docs/PLAN.md in one small commit, run git push origin main, then delete the local branch. Report the pushed commit hash. If the push fails, show me the error and stop. Do not change remotes, credentials or git settings.
8. Never push before approval. Never force push, never reset --hard, never rewrite history. Only push main.
9. Then start the next step. Never stack work on an unapproved branch.
10. Questions: if something is unclear and not settled by the cahier, the mockups or the decisions in PLAN.md, ask all questions in ONE message with a recommended default. If the work can continue with the default, continue and flag it in the review message.
11. Long sessions: after about 2 steps, or if you lose track, update docs/PROGRESS.md and tell me to open a new session and run /build.
