// Applied here (not per-module): it inspects every subproject's declared dependencies, so it
// must run against the root project. See docs/adr/0003-module-boundaries.md.
plugins {
    id("kidsclock.dependency.rules")
}
