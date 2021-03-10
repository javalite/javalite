# ActiveWeb SASS Maven Plugin compiles SASS files into CSS

## Online documentation

Please, refer to [SASS documentation on the JavaLite site](http://javalite.io/sass)

## Configuration of a SASS compiler plugin 

```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>activeweb-sass-maven-plugin</artifactId>
    <version>${javalite.version}</version>
    <configuration>
        <sassConfigs>
            <sassConfig implementation="org.javalite.sass.maven.SassConfig">
                <sassMain>src/main/sass/main.sass</sassMain>
                <targetDirectory>target/web</targetDirectory>
                <targetFileName>bootstrap.css</targetFileName>
            </sassConfig>
            <!-- add additional sassConfig section to process multiple files -->
        </sassConfigs>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
