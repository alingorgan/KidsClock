#!/usr/bin/env bash
# Runs the checks relevant to the changed files: ktlint, module guardrails (verify*, including the
# UI automation guardrail and module dependency-direction guardrail), Android lint and unit +
# snapshot tests of the modules that changed. Instrumented (on-device) UI tests are not here: they
# need a device. Run them by hand, see docs/UI_AUTOMATION.md. Full `./gradlew check` is the
# pre-push gate.
# Usage: scripts/check-changed.sh < list-of-changed-paths   (one per line)
# Exit 0: everything passed or nothing to check. Exit 1: a check failed (output tail on stderr).
set -uo pipefail
cd "$(git rev-parse --show-toplevel)" || exit 1

tasks=()
add() { case " ${tasks[*]-} " in *" $1 "*) ;; *) tasks+=("$1") ;; esac; }
model() { add :core:model:test; add :core:model:verifyModelPurity; }
android() { add ":$1:testDebugUnitTest"; add ":$1:lintDebug"; add ":$1:verifyAccessibleInteractions"; }
snapshots() { add ":$1:verifySnapshotCoverage"; }
featureApi() { add ":feature:$1:api:test"; }
featureImpl() { android "feature:$1:impl"; snapshots "feature:$1:impl"; }
all() {
  model; android core:designsystem; snapshots core:designsystem
  featureApi run; featureImpl run
  android app; add :app:verifyNoInternetPermission; add ktlintCheck; add verifyDependencyRules
}

while IFS= read -r f; do
  case "$f" in
    core/model/*) model ;;
    core/designsystem/*) android core:designsystem; snapshots core:designsystem ;;
    feature/run/api/*) featureApi run ;;
    feature/run/impl/*) featureImpl run ;;
    app/*) android app; add :app:verifyNoInternetPermission ;;
    build-logic/*|gradle/*|build.gradle.kts|settings.gradle.kts|gradle.properties|.editorconfig) all ;;
  esac
  case "$f" in *.kt|*.kts|.editorconfig) add ktlintCheck ;; esac
  # Any build file, or the convention-plugin source that defines the dependency rules themselves,
  # can change which module depends on what — a .kt source file inside a module never can.
  case "$f" in *.kts|build-logic/*) add verifyDependencyRules ;; esac
done

[ ${#tasks[@]} -eq 0 ] && exit 0

echo "Checking changed modules: ${tasks[*]}" >&2
if out=$(./gradlew "${tasks[@]}" --console=plain -q 2>&1); then
  exit 0
fi
{
  echo "Checks failed for the changed files. Fix them, then commit again."
  echo "Tasks: ${tasks[*]}"
  echo "$out" | tail -60
} >&2
exit 1
