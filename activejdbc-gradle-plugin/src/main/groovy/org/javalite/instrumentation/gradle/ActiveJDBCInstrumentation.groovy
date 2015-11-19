package org.javalite.instrumentation.gradle

import javassist.ClassPool
import javassist.LoaderClassPath
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.javalite.instrumentation.Instrumentation

/**
 * Gradle task for performing ActiveJDBC instrumentation on a set of compiled {@code .class} files.
 */
class ActiveJDBCInstrumentation extends DefaultTask {

    /** The directory containing class files to be instrumented. */
    String classesDir = project.sourceSets.main.output.classesDir.getPath()

    /** The output directory to write back classes after instrumentation. */
    String outputDir = classesDir

    ActiveJDBCInstrumentation() {
        description = "Instrument compiled class files extending from 'org.javalite.activejdbc.Model'"
    }

    @TaskAction
    def instrument() {
        def parentLoader = Thread.currentThread().getContextClassLoader()
        def modelLoader = new GroovyClassLoader(parentLoader)

        def classesFile = project.file(classesDir)
        modelLoader.addURL(classesFile.toURI().toURL())

        def pool = new ClassPool()
        def path = new LoaderClassPath(modelLoader)
        pool.appendClassPath(path)

        Instrumentation instrumentation = new Instrumentation(pool)
        instrumentation.outputDirectory = outputDir
        instrumentation.instrument()
    }

}
