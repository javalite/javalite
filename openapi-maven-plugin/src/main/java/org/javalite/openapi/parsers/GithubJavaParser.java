package org.javalite.openapi.parsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.metamodel.CommentMetaModel;
import com.github.javaparser.metamodel.CompilationUnitMetaModel;
import com.github.javaparser.metamodel.NodeMetaModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;



/**
 * 1. modifier (must be public)
 * 2. return value (must be void)
 * 3. Parameters (count must be 1 or 0),
 * 4. Cannot be static
 * 5. Need to get all the info even  if JavaDoc does not exist on a method.
 */

public class GithubJavaParser {
    public static void main(String[] args) throws FileNotFoundException {

        CompilationUnit compilationUnit =  StaticJavaParser.parse(new File("/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/controllers/PeopleController.java"));

        compilationUnit.findAll(MethodDeclaration.class).forEach(d-> {
            System.out.println("============================");
            System.out.println("Name: " + d.getName());
            System.out.println("Parameters: " + (d.getParameters().size() < 2? d.getParameters() : "Too many parameters"));
            System.out.println("Return type: " + (d.getType().isVoidType()?d.getType() : "Skipping non-void"));
            System.out.println("Modifiers: " + d.getModifiers());
            System.out.println("Comment: \n" + (d.getComment().isPresent()? d.getComment().get() : ""));
        });
    }
}

