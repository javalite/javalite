# ActiveWebLessc Maven Plugin compiles Lessc files into CSS

## Configuration

```xml
<plugin>
    <groupId>org.javalite</groupId>
    <artifactId>activeweb-lessc-maven-plugin</artifactId>
    <version>1.11-SNAPSHOT</version>
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

This should be self-explanatory