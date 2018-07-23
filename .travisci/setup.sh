#!/bin/bash
# Sets Up the Build Environment on TravisCI

set -e # Exit with nonzero exit code if anything fails

# Install Maven
#export M2_HOME=$HOME/apache-maven-3.5.4
#if [ ! -d $M2_HOME/bin ]; then curl https://archive.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz | tar zxf - -C $HOME; fi
#export PATH=$M2_HOME/bin:$PATH

# Reduce Maven download log
export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

# Install OracleJDK8
#sudo apt-add-repository -y ppa:webupd8team/java
#echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
#sudo apt-get -qq update
#sudo apt-get -qq install -y oracle-java8-installer
