mvn clean source:jar  javadoc:jar  deploy -Pinstrument
activejdbc-gradle-plugin/gradlew -b activejdbc-gradle-plugin/build.gradle clean build uploadArchives