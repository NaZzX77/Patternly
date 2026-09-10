# Engineering working agreement

For each substantive change, we will inspect the existing state, explain the design and tradeoffs, implement only the agreed scope, verify it proportionately, and update documentation.

Architecture decisions that are consequential or hard to reverse are recorded as ADRs in `docs/decisions/`. Feature-specific engineering explanations belong in the most relevant `docs/architecture/` or `docs/development/` document. Progress milestones belong in `docs/progress/`.

Configuration and secrets will be externalized when application code is introduced. Credentials will never be committed.
