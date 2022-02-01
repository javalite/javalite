package org.javalite.activeweb.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;


@Mojo(name = "help")
public class HelpMojo extends AbstractMojo {


    @Override
    public void execute() {

        System.out.println("""
                This plugin has the following goals:
                
                * routes - prints all existing routes in the given ActiveWeb project.
                * generate - will generate OpenAPI documentation from those routes  that include an OpenAPI documentaion.
                * help - prints this message.
              
                
                ******* Additional information for the "generate" goal *******
                
                The "generate" goal is  designed to generate the OpenAPI- compliant documentation from
                sources and other supporting files. Here is a typical configuration of the plugin for this goal:
                                <plugin>
                                    <groupId>org.javalite</groupId>
                                    <artifactId>aw-mate</artifactId>
                                    <version>${javalite.version}</version>
                                    <configuration>
                                        <format>JSON</format>
                                        <templateFile>src/main/openapi/base.json</templateFile>
                                        <apiLocation>src/main/openapi</apiLocation>
                                        <targetFile>target/openapi.json</targetFile>
                                    </configuration>
                                </plugin>
                            
                Configuration properties:
                
                * "format" - JSON or YAML. Current implementation only supports JSON. Yaml is to come in the future.
                * "templateFile" - this is a file that is used  as a template. This template will have a single
                                   merge field "paths", see below for an example:
                                                                 
                                   {
                                     "openapi": "3.0.0",
                                     "info": {
                                       "title": "Simple API overview",
                                       "version": "3.0.0"
                                     },
                                     "paths":{
                                        
                                      }
                                   }
                                   
                                    Please, note  that the JavaLite implementation is quite unsophisticated.
                                    It does not know anything about the actual OpenAPI specification and does not 
                                    really care. All it does is checks that it has a valid JSON format. 
                                    As such, it supports any OpenAPI specification version. 
                      
                * "targetFile"    - where to write the output.
                * "apiLocation"   - a directory that contains JSON files specific to endpoints and their respective HTTP 
                                    methods. The format for such file names is:
                                   
                                    app.controllers.CustomController#index-get.json
                    
                                    where:
                                      * app.controllers - a fully qualified package name
                                      * CustomController - simple class name of the controller
                                      * index - HTTP action (method name)
                                      * get - HTTP method
                                      
                                      An example of a such file content: 
                                     
                                                    {
                                                      "operationId": "1234",
                                                      "summary": "Get custom stuff",
                                                      "responses": {
                                                        "200": {
                                                          "description": "200 response",
                                                          "content": {
                                                            "application/json": {
                                                              "examples": {
                                                              "stuff" : [1, 2, 3]
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }
                        
                                    As you can see,  you can have different OpenAPI specifications for the same action, 
                                    but different HTTP methods.
                        
                                    NOTE: the "apiLocation" parameter is optional. If you do not want to have these docs 
                                    in separate files, you can have then directly inside your controller class as 
                                    arguments to the action  annotations.
                                    
                                    Here is an example:
                        
                                        public class CustomController extends AppController {
                                        
                                            @GET(""\"
                                                    {
                                                      "operationId": "1234",
                                                      "summary": "Get custom stuff",
                                                      "responses": {
                                                        "200": {
                                                          "description": "200 response",
                                                          "content": {
                                                            "application/json": {
                                                              "examples": {
                                                              "stuff" : [1, 2, 3]
                                                              }
                                                            }
                                                          }
                                                        }
                                                      }
                                                    }""\")
                                            public void index(){}
                                            }
                                            
                                    Thus, any action on ActiveWeb controllers can have any number of annotations, 
                                    where you can enter the OpenAPI documentation for that specific HTTP method.
                                    
                                    ******************************** ATTENTION ***************************************
                                    Only one method of providing OpenAPI documentation for **the same endpoint** is 
                                    allowed. Either you use a file as described above or you can use an action 
                                    annotation, but not both. You can easily mix and match docs in annotations and 
                                    files in the same project, as long as there is no conflict on the same 
                                    endpoint/HTTP method!
                                    **********************************************************************************
                """);
    }
}
