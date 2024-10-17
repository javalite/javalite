
# add: -DignoreSnapshots in case you need to depend on snapshots in  this release (bad idea)
mvn release:prepare -Pskip_tests,skip_integration_tests,gen-sources
