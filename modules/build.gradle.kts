// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

import org.terasology.gradology.JAR_COLLECTION
import org.terasology.gradology.moduleDependencyArtifacts
import org.terasology.gradology.namedAttribute

plugins {
    `terasology-repositories`
    `java-platform`
}

@Suppress("PropertyName")
val CACHE_MODULES_DIR = rootProject.file("cacheModules")

javaPlatform {
    allowDependencies()
}

dependencies {
    // This platform depends on each of its subprojects.
    subprojects {
        runtime(this)
    }
}

val jarCollection: Configuration by configurations.creating {
    description = "Provides cacheModules with JAR_COLLECTION."

    isCanBeConsumed = true
    isCanBeResolved = false

    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, namedAttribute(Usage.JAVA_RUNTIME))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, namedAttribute(JAR_COLLECTION))
    }
}

val provideModuleDependencies by tasks.registering(Sync::class) {
    destinationDir = CACHE_MODULES_DIR

    val artifactsProvider = moduleDependencyArtifacts(configurations.named("classpath"))
    from(artifactsProvider.map { artifacts -> artifacts.map(ResolvedArtifactResult::getFile) })
}

artifacts {
    // The output of our jarCollection configuration comes from this task.
    add(jarCollection.name, provideModuleDependencies) {
        type = "jar-collection"
    }
}


// Allows using :modules:clean as a shortcut for running clean in each module.
tasks.named("clean").configure {
    val cleanPlatform = this
    subprojects {
        cleanPlatform.dependsOn(this.tasks.named("clean"))
    }
}
