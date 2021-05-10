package org.javalite.openapi.parsers;


import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.tools.DocumentationTool;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

public class JavaDocletParser {

    public static class CustomDoclet implements Doclet {

        @Override
        public void init(Locale locale, Reporter reporter) {
        }

        @Override
        public String getName() {
            return "Custom";
        }

        @Override
        public Set<? extends Option> getSupportedOptions() {
            return Collections.emptySet();
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public boolean run(DocletEnvironment environment) {
            Elements elements = environment.getElementUtils();
            environment.getSpecifiedElements().forEach(element -> {
                System.out.println("================================= " + element.getKind() + " " + element.getSimpleName());
                System.out.println(elements.getDocComment(element));
                element.getEnclosedElements().forEach(e -> {
                    System.out.println("----- " + e.getKind() + " " + e.getSimpleName());
                    System.out.println(elements.getDocComment(e));

                    if (e instanceof ExecutableElement) {
                        ExecutableElement ee = (ExecutableElement) e;
                        System.out.println("PARAMETERS: " + ee.getParameters());
                    }

                });
            });
            return true;
        }
    }

    public static void main(String[] args) {
        DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
        if (documentationTool != null) {
            StandardJavaFileManager fileManager = documentationTool.getStandardFileManager(null, null, null);
            System.out.println(fileManager);
            System.out.println("Task: " + documentationTool.getTask(
                    null,
                    null,
                    diagnostic -> System.err.println(diagnostic),
                    CustomDoclet.class,
                    null,
                    fileManager.getJavaFileObjects("src/test/java/org/javalite/app/controllers/TestController.java")
            ).call());

        }
    }
}