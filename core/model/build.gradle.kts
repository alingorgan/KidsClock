plugins {
    id("kidsclock.kotlin.jvm")
}

// Guardrail: core/model is platform-neutral. No android.*/androidx.* imports and no project dependencies.
val verifyModelPurity =
    tasks.register("verifyModelPurity") {
        group = "verification"
        description = "Fails if core/model imports android.* or depends on another project module."
        val sources = fileTree("src") { include("**/*.kt") }
        val projectDeps =
            provider {
                configurations.flatMap { cfg ->
                    cfg.dependencies.filterIsInstance<ProjectDependency>().map { "${cfg.name} -> ${it.path}" }
                }
            }
        doLast {
            val offenders =
                sources.files.flatMap { file ->
                    file.readLines().mapIndexedNotNull { i, line ->
                        val t = line.trim()
                        if (t.startsWith("import android.") || t.startsWith("import androidx.")) {
                            "${file.relativeTo(projectDir)}:${i + 1}: $t"
                        } else {
                            null
                        }
                    }
                }
            val deps = projectDeps.get()
            if (offenders.isNotEmpty() || deps.isNotEmpty()) {
                throw GradleException(
                    buildString {
                        appendLine("core:model must stay pure Kotlin (see docs/ARCHITECTURE.md).")
                        offenders.forEach { appendLine("  forbidden import: $it") }
                        deps.forEach { appendLine("  forbidden project dependency: $it") }
                    },
                )
            }
        }
    }

tasks.named("check") { dependsOn(verifyModelPurity) }
