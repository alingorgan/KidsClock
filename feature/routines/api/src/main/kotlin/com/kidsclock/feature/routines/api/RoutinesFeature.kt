package com.kidsclock.feature.routines.api

/**
 * Public contract for the routines feature ([feature/routines/impl][com.kidsclock.feature.routines.RoutinesScreen]).
 *
 * Nothing depends on this yet — that's normal for a freshly scaffolded feature (see
 * docs/adr/0003-module-boundaries.md). `app` depends on `feature/routines/impl` directly until
 * something needs to reference this feature without depending on its implementation. Add real
 * contract types here then — a navigation route/args, a result callback, an entry-point interface
 * — not speculatively now.
 */
object RoutinesFeature {
    /** Stable identifier for this feature: manual-DI wiring today, a navigation route later. */
    const val ID: String = "routines"
}
