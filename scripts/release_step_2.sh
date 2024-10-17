
# add: -DignoreSnapshots in case you need to depend on snapshots in  this release (bad idea)
mvn release:prepare  -Dmaven.test.skip=true  -Pskip_integration_tests
