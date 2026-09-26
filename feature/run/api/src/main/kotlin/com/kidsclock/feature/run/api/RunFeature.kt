package com.kidsclock.feature.run.api

/**
 * Public contract for the run feature ([feature/run/impl][com.kidsclock.feature.run.RunScreen]).
 *
 * Nothing depends on this yet: `app` currently depends on `feature/run/impl` directly, which is
 * fine for a single caller (docs/adr/0003-module-boundaries.md). This module exists so the feature
 * has the standard api/impl shape from day one, not bolted on once a second consumer shows up. Add
 * real contract types here — a navigation route/args, a result callback, an entry-point interface —
 * the first time something (another feature, or `app` wiring against a stable type) needs to
 * reference the run feature without depending on its implementation.
 */
object RunFeature {
    /** Stable identifier for this feature: manual-DI wiring today, a navigation route later. */
    const val ID: String = "run"
}
