package org.javalite.openapi;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.metamodel.CompilationUnitMetaModel;
import org.javalite.common.Inflector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EndpointFinder {

    public static void main(String[] args) {

        processController(Paths.get("/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/controllers/PeopleController.java"));
    }

     static void processController(Path path) {
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(path.toFile());
//            CompilationUnitMetaModel compilationUnitMetaModel = compilationUnit.getMetaModel();
//            compilationUnitMetaModel.getDeclaredPropertyMetaModels();
//
//            System.out.println("One: " + compilationUnit.getMetaModel());
//            System.out.println("Two: " + compilationUnitMetaModel.toString());





            System.out.println("===========================");
            String className = getControllerClassname(compilationUnit, path);
            System.out.println("Class name: " + className);




            List<Comment> comments = compilationUnit.getAllComments();

            System.out.println("Processing: " + path);

            for (Comment comment : comments) {
                if (comment.isJavadocComment()) {
                    Optional<Node> optionalNode = comment.getCommentedNode();
                    if (optionalNode.isPresent()) {
                        Node n = optionalNode.get();
                        //NodeMetaModel mm = n.getMetaModel();


                        if (n instanceof MethodDeclaration) {
                            MethodDeclaration md = (MethodDeclaration) n;
                            if(!isAPI(md)){
//                                System.out.println("Method: " + md.getName() + "() is not an API, skipping...");
                                continue;
                            }
                            System.out.println("------------------");
                            System.out.println("Method: " + Inflector.underscore(md.getName().toString()));
//                            System.out.println("Annotations: " + md.getAnnotations());
//                            System.out.println("Parameters: " + md.getParameters());
//                            System.out.println("Void: " + md.getType().isVoidType());
                            System.out.println("JavaDoc: ");
                            System.out.println(comment.getContent());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  1. modifier (must be public)
     *  2. return value (must be void)
     *  3. Parameters (count must be 1 or 0),
     *  4. Cannot be static
     *  5. Cannot be abstract
     */
    private  static boolean isAPI(MethodDeclaration md) {
        return md.isPublic()
                && md.getType().isVoidType()
                && md.getParameters().size() <= 1
                && !md.isStatic()
                && !md.isAbstract()
                && !md.isNative();
    }

    public static class ClassNameCollector extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
            super.visit(n, collector);
            if(n.getFullyQualifiedName().isPresent() && n.getFullyQualifiedName().get().endsWith("Controller") ){
                collector.add(n.getFullyQualifiedName().get());
            }
        }
    }

    private static String getControllerClassname(CompilationUnit compilationUnit, Path path){

        VoidVisitor<List<String>> classNameVisitor = new ClassNameCollector();
        List<String> classNames = new ArrayList<>();
        classNameVisitor.visit(compilationUnit,classNames);
        if(classNames.size() != 1){
            throw new OpenAPIException("Too many (or not enough) controllers in file: " + path);
        }
        return classNames.get(0);
    }

}
