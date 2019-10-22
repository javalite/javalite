## Release steps

1. mvn release:clean
2. mvn release:prepare (ls pwd)
3. mvn release:perform
4. Follow steps to release:
	Navigate to Staging repositories: https://oss.sonatype.org/index.html#stagingRepositories,
	close and release artifacts (personal email / dev ls pwd)
5. Create an issue to release a new Gradle plugin: https://github.com/cschabl/activejdbc-gradle-plugin
6. Search the project for previous snapshot version and switch to latest snapshot (test projects)
7. Update and test all examples to the latest released version (create a new issue on Github)
8. Create JavaDoc for released version and update new SNAPSHOT version by executing: 

        scripts/release_javadoc.sh VERSION

    Update JavaDocs page:

        javalite-site/content/src/activejdbc/javadoc.md

9. Generate release notes:

   Generate commmit log:

        git log --format=format:"* %ci %an [%s](https://github.com/javalite/activejdbc/commit/%h)" --since "2015-01-30" > release_notes.txt

   Add release notes to:

        javalite-site/content/src/activejdbc/releases.md

10. Write a blog article

    Highlight major  new features  
    
    
    
        
## If you messed up the release

 
1. Drop it from Sonatype.
2. Delete tags/releases: [tags/releases](https://github.com/javalite/javalite/releases)
3. Perform:  `git fetch --tags` because your local repo still have old tags
4. Erase release commits: 
    `git reset --hard HEAD~X` - depending how many you want  to kill
5. Push to repo: 
       `git push origin [branch] -f` - this will erace history, BE CAREFUL!
6. Start afresh
