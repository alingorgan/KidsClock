#!/usr/bin/env bash
# Claude PreToolUse hook (matcher: Bash).
#  - `git commit`: run the changed-file checks via scripts/check-changed.sh.
#  - `git push`: run the full `./gradlew check`.
# Exit 2 blocks the command and shows the failure to Claude. The git hooks in .githooks/ run the same
# checks for commits and pushes made outside Claude.
set -uo pipefail

cmd=$(jq -r '.tool_input.command // ""')
is_git() { echo "$cmd" | grep -Eq "(^|[;&|[:space:]])git[[:space:]]+(-[^[:space:]]+[[:space:]]+)*$1([[:space:]]|\$)"; }

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

if is_git push; then
  echo "Running ./gradlew check before push..." >&2
  if ! out=$(./gradlew check --console=plain -q 2>&1); then
    { echo "./gradlew check failed; push blocked."; echo "$out" | tail -60; } >&2
    exit 2
  fi
  exit 0
fi

is_git commit || exit 0

files=$(git diff --cached --name-only --diff-filter=ACMR)
# `git commit -a` also commits tracked modifications that are not staged yet.
if echo "$cmd" | grep -Eq 'commit[^;&|]*[[:space:]](-[a-zA-Z]*a[a-zA-Z]*|--all)([[:space:]]|$)'; then
  files=$(printf '%s\n%s\n' "$files" "$(git diff --name-only --diff-filter=ACMR)")
fi

echo "$files" | scripts/check-changed.sh || exit 2
