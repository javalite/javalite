# ActiveWebLessc Maven Plugin compiles Lessc files into CSS

## Online documentation

Please, refer to [Lessc documentation on the JavaLite site](http://javalite.io/lessc)

## Configuration with a single LESS file

```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>activeweb-lessc-maven-plugin</artifactId>
    <version>${activeweb.version}</version>
    <configuration>
        <lesscMain>src/main/webapp/less/bootstrap.less</lesscMain>
        <targetDirectory>target/web/css</targetDirectory>
        <targetFileName>bootstrap.css</targetFileName>
    </configuration>
    <executions>
        <execution>
            <goals><goal>compile</goal></goals>
        </execution>
    </executions>
</plugin>

```

## Configuration with a multiple LESS files


```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>activeweb-lessc-maven-plugin</artifactId>
    <version>${activeweb.version}</version>
    <configuration>
        <lessConfigs>
            <lessConfig implementation="org.javalite.lessc.maven.LessConfig">
                <lesscMain>src/main/webapp/less1/bootstrap.less</lesscMain>
                <targetDirectory>target/web1</targetDirectory>
                <targetFileName>bootstrap.css</targetFileName>
            </lessConfig>
            <lessConfig implementation="org.javalite.lessc.maven.LessConfig">
                <lesscMain>src/main/webapp/less2/bootstrap.less</lesscMain>
                <targetDirectory>target/web2</targetDirectory>
                <targetFileName>bootstrap.css</targetFileName>
            </lessConfig>
        </lessConfigs>
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
