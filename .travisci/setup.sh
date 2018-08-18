#!/bin/bash
# Sets Up the Build Environment on TravisCI

set -e # Exit with nonzero exit code if anything fails

# Reduce Maven download log
#export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

# Replace maven settings.xml file
cp -f .travisci/settings.xml ~/.m2/