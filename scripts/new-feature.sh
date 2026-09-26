#!/usr/bin/env bash
# Scaffolds a new feature module pair: feature/<name>/api (pure Kotlin contract) and
# feature/<name>/impl (Android/Compose screen), matching feature/run's shape
# (docs/adr/0003-module-boundaries.md). Registers both in settings.gradle.kts and records their
# snapshot baselines. Does NOT wire the feature into `app` or invent screen content beyond a
# placeholder — those are product decisions, not scaffolding.
#
# Usage: scripts/new-feature.sh <name>   (lowercase letters/digits, e.g. "setup")
set -euo pipefail

name="${1:-}"
if [[ ! "$name" =~ ^[a-z][a-z0-9]*$ ]]; then
  echo "Usage: scripts/new-feature.sh <name>   (lowercase letters/digits, e.g. \"setup\")" >&2
  exit 1
fi

root="$(git rev-parse --show-toplevel)"
cd "$root"

dir="feature/$name"
if [ -e "$dir" ]; then
  echo "$dir already exists." >&2
  exit 1
fi

class="$(tr '[:lower:]' '[:upper:]' <<< "${name:0:1}")${name:1}"
pkg_api="com.kidsclock.feature.$name.api"
pkg_impl="com.kidsclock.feature.$name"

echo "Scaffolding feature/$name (api + impl)..."

# --- api: pure Kotlin contract, no Android, no Compose (kidsclock.feature.api) ---
api_kt_dir="$dir/api/src/main/kotlin/$(tr '.' '/' <<< "$pkg_api")"
mkdir -p "$api_kt_dir"

cat > "$dir/api/build.gradle.kts" <<EOF
plugins {
    id("kidsclock.feature.api")
}
EOF

cat > "$api_kt_dir/${class}Feature.kt" <<EOF
package $pkg_api

/**
 * Public contract for the $name feature ([feature/$name/impl][${pkg_impl}.${class}Screen]).
 *
 * Nothing depends on this yet — that's normal for a freshly scaffolded feature (see
 * docs/adr/0003-module-boundaries.md). \`app\` depends on \`feature/$name/impl\` directly until
 * something needs to reference this feature without depending on its implementation. Add real
 * contract types here then — a navigation route/args, a result callback, an entry-point interface
 * — not speculatively now.
 */
object ${class}Feature {
    /** Stable identifier for this feature: manual-DI wiring today, a navigation route later. */
    const val ID: String = "$name"
}
EOF

# --- impl: Android library + Compose, built from core/designsystem blocks (kidsclock.feature.impl) ---
impl_kt_dir="$dir/impl/src/main/kotlin/$(tr '.' '/' <<< "$pkg_impl")"
impl_test_dir="$dir/impl/src/test/kotlin/$(tr '.' '/' <<< "$pkg_impl")"
mkdir -p "$impl_kt_dir" "$impl_test_dir" "$dir/impl/src/main/res/values"

cat > "$dir/impl/build.gradle.kts" <<EOF
plugins {
    id("kidsclock.feature.impl")
}

android {
    namespace = "$pkg_impl"
}
EOF

cat > "$dir/impl/src/main/res/values/strings.xml" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="${name}_placeholder">$class</string>
</resources>
EOF

cat > "$impl_kt_dir/${class}Screen.kt" <<EOF
package $pkg_impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kidsclock.core.designsystem.components.KcScreen
import com.kidsclock.core.designsystem.components.KcText
import com.kidsclock.core.designsystem.components.KcTextStyle

/**
 * Placeholder screen scaffolded by scripts/new-feature.sh. Test tags: \`$name.screen\`,
 * \`$name.title\` (docs/UI_AUTOMATION.md). Replace with the real screen.
 */
@Composable
fun ${class}Screen() {
    KcScreen(testTag = "$name.screen") {
        KcText(
            text = stringResource(R.string.${name}_placeholder),
            testTag = "$name.title",
            style = KcTextStyle.Title,
        )
    }
}
EOF

cat > "$impl_test_dir/${class}ScreenSnapshotTest.kt" <<EOF
package $pkg_impl

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.kidsclock.core.designsystem.KidsClockTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h740dp-xhdpi")
class ${class}ScreenSnapshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun light() {
        compose.setContent { KidsClockTheme(darkTheme = false) { ${class}Screen() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/${class}Screen_light.png")
    }

    @Test
    fun dark() {
        compose.setContent { KidsClockTheme(darkTheme = true) { ${class}Screen() } }
        compose.onRoot().captureRoboImage("src/test/snapshots/${class}Screen_dark.png")
    }
}
EOF

# --- register in settings.gradle.kts ---
{
  echo "include(\":feature:$name:api\")"
  echo "include(\":feature:$name:impl\")"
} >> settings.gradle.kts

echo "Formatting and recording snapshot baselines..."
./gradlew ktlintFormat -q
./gradlew ":feature:$name:impl:recordRoborazziDebug" -q

echo "Verifying the new modules..."
./gradlew ":feature:$name:api:check" ":feature:$name:impl:check" -q

cat <<EOF

Created feature/$name/{api,impl}. Not done automatically (deliberate — product/wiring decisions,
not scaffolding):
  - app does not depend on ":feature:$name:impl" yet — add it in app/build.gradle.kts when ready
    to host the screen.
  - Replace the placeholder ${class}Screen and ${class}Feature with the real ones once something
    needs them.
EOF
