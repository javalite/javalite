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

        scripts/javadoc.sh VERSION

9. Ensure no issues have the "In-Progress" label

	https://github.com/javalite/javalite/labels/In-Progress

9. Generate release notes:

	git tag -a javalite-2.4-j8 -m "release javalite-2.4-j8"

where `javalite-2.4-j8` is a tag for a new release     

	git push origin --tags

Generate release notes:

	groovy ReleaseNotes.groovy olderTag newerTag

> Ensure that issues  in the release are properly categorized by tags. If needed, make corrections and re-run the same command again...and again...and again...

  After that, go to https://github.com/javalite/javalite/releases  and add the release notes, then publish the release


   Add release notes link to:

        javalite-site/content/src/activejdbc/releases.md

10. Write a blog article

    Highlight major new features 
    
    
## If you messed up the release

 
1. Drop it from Sonatype.
2. Delete tags/releases: [tags/releases](https://github.com/javalite/javalite/releases)
    Example: 
	git tag -d javalite-2.4-j8
	git push origin :refs/tags/javalite-2.4-j8

3. Perform:  `git fetch --tags` because your local repo still have old tags
4. Erase release commits: 
    `git reset --hard HEAD~X` - depending how many you want  to kill
5. Push to repo: 
       `git push origin [branch] -f` - this will erase history, BE CAREFUL!
       
       Remember that the release notes now are based on issues, not  commits.
        
6. Start afresh

