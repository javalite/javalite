package org.javalite.instrumentation.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin the registers an {@link ActiveJDBCInstrumentation} task to run as part of the classes step.
 */
class ActiveJDBCGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def instrumentModels = project.tasks.create('instrumentModels', ActiveJDBCInstrumentation)
        instrumentModels.group = "build"

        // use it as doLast action, because Gradle takes hashes of class files for incremental build afterwards
        project.tasks.compileJava.doLast {
            instrumentModels.instrument()
        }
    }

}
