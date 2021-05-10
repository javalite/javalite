package org.javalite.openapi.parsers.newer;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.tools.DocumentationTool;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 1. modifier (must be public)
 * 2. return value (must be void)
 * 3. Parameters (count must be 1 or 0),
 * 4. Cannot be static
 * 5. Need to get all the info even  if JavaDoc does not exist on a method.
 */
public class JavaDocParser {
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
            environment.getSpecifiedElements().forEach(specifiedElement -> {


                System.out.println("specifiedElement ---> " + specifiedElement + ", class: " + specifiedElement.getClass());


                specifiedElement.getEnclosedElements().forEach(enclosedElement -> {
                    if (enclosedElement.getKind() == ElementKind.METHOD) {
                        System.out.println("************** METHOD: " + enclosedElement.getSimpleName() + "************");
                        if (enclosedElement instanceof ExecutableElement) {
                            ExecutableElement methodElement = (ExecutableElement) enclosedElement;
                            // #1 #4
                            Set<Modifier> modifiers = methodElement.getModifiers();
                            if (modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC) && !methodElement.isVarArgs()) { //skip method with variable number of arguments
                                System.out.println(modifiers);
                                //#2
                                if (methodElement.getReturnType().getKind() == TypeKind.VOID) {
                                    System.out.println("Return type: " + methodElement.getReturnType());
                                    //#3
                                    List<? extends VariableElement> parameters = methodElement.getParameters();
                                    if (parameters.size() < 2) {
                                        boolean skip = false;
                                        for(VariableElement param : parameters) {
                                            TypeKind typeKind = param.asType().getKind();
                                            if (typeKind != TypeKind.DECLARED) {
                                                skip = true;
                                                break;
                                            }
                                        }
                                        if (!skip) {
                                            //Required action!
                                            System.out.println("Javadoc: " + elements.getDocComment(enclosedElement));
                                            parameters.forEach(param -> {
                                                System.out.println("Parameter: " + param.asType().toString() + " " + param.getSimpleName());
                                            });
                                        } else {
                                            System.out.println("Skip! Wrong parameters!");
                                        }
                                    } else {
                                        System.out.println("Skip! Too many parameters!");
                                    }
                                } else {
                                    System.out.println("Skip! Return value is not void!");
                                }
                            } else {
                                System.out.println("Skip! Not public or static method!");
                            }
                        }
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
            documentationTool.getTask(null, null, diagnostic -> System.err.println(diagnostic), CustomDoclet.class, null,
                    fileManager.getJavaFileObjects("/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/controllers/PeopleController.java",
                            "/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/models/Person.java",
                            "/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/models/Address.java")
            ).call();
        }
    }
}