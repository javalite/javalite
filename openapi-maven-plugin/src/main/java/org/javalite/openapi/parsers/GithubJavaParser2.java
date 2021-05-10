package org.javalite.openapi.parsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.metamodel.CompilationUnitMetaModel;
import com.github.javaparser.metamodel.NodeMetaModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;


/**
 * 1. modifier (must be public)
 * 2. return value (must be void)
 * 3. Parameters (count must be 1 or 0),
 * 4. Cannot be static
 * 5. Need to get all the info even  if JavaDoc does not exist on a method.
 */

public class GithubJavaParser2 {
    public static void main(String[] args) throws FileNotFoundException {

        CompilationUnit compilationUnit =  StaticJavaParser.parse(new File("/home/igor/projects/javalite/javalite/openapi-maven-plugin/src/test/project/activeweb-openapi-example/src/main/java/app/controllers/PeopleController.java"));
        CompilationUnitMetaModel compilationUnitMetaModel = compilationUnit.getMetaModel();
        compilationUnitMetaModel.getDeclaredPropertyMetaModels();


        List<Comment> comments = compilationUnit.getAllComments();

        for (Comment comment : comments) {
            if(comment.isJavadocComment()){

                System.out.println("---------------------------------------------------------------------------");
                Optional<Node> optionalNode = comment.getCommentedNode();

                if(optionalNode.isPresent() ){

                    Node n = optionalNode.get();
                    NodeMetaModel mm = n.getMetaModel();


                    if(n instanceof MethodDeclaration){

                        MethodDeclaration md = (MethodDeclaration) n;
                        System.out.println("Method: "+ (md.isPublic()? "public" : "NOT PUBLIC!") + " " + (md.getType().isVoidType()? "void" : "NOT VOID!!") + " " +  md.getName());


                        System.out.println("Parameters: " + md.getParameters());
                        System.out.println("JavaDoc: ");
                        System.out.println(comment.getContent());
//                        CommentMetaModel model = comment.getMetaModel();
//                        System.out.println("---");
                    }

                    /*
                     * type == void - this is a return value
                     */
                }else{
                    System.out.println("Found node without JavaDoc: " + optionalNode );
                }
            }
        }
    }
}
