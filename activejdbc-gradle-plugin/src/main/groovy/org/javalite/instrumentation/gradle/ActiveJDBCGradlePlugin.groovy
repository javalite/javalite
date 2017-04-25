package org.javalite.instrumentation.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin the registers an {@link ActiveJDBCInstrumentation} task to run as part of the classes step.
 */
class ActiveJDBCGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.tasks.create('instrumentModels', ActiveJDBCInstrumentation)
        project.tasks.instrumentModels.dependsOn << 'compileJava'
        project.tasks.classes.dependsOn << 'instrumentModels'
    }

}
