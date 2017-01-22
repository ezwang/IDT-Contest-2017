#!/bin/bash

set -e

# build the jar
cd com.idtus.contest.winter2017.framework
mvn package

# validate output with provided jars
echo '=== TesterTypeCheck.jar'
java -jar target/com.idtus.contest.winter2017.framework.jar -jarToTestPath ../supporting_files/jars/TesterTypeCheck.jar -toolChain
echo '=== RegexPatternMatch.jar'
java -jar target/com.idtus.contest.winter2017.framework.jar -jarToTestPath ../supporting_files/jars/RegexPatternMatch.jar -toolChain
echo '=== CommandLineEncryption.jar'
java -jar target/com.idtus.contest.winter2017.framework.jar -jarToTestPath ../supporting_files/jars/CommandLineEncryption.jar -toolChain
echo '=== LeetConverter.jar'
java -jar target/com.idtus.contest.winter2017.framework.jar -jarToTestPath ../supporting_files/jars/LeetConverter.jar -toolChain
