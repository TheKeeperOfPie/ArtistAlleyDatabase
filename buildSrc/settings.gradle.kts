apply(rootProject.projectDir.resolve("../versions.gradle.kts"))
dependencyResolutionManagement {
    @Suppress("UNCHECKED_CAST")
    (extra["versions"] as (DependencyResolutionManagement) -> Unit)(this)
}
