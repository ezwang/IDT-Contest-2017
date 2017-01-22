#!/bin/bash

set -e

# build the jar
cd com.idtus.contest.winter2017.framework
mvn package

function test_jar() {
    OUTPUT=$(java -jar target/com.idtus.contest.winter2017.framework.jar -jarToTestPath $1 -toolChain)
    echo "$OUTPUT"
    # make sure all tests have passed
    TOTAL=$(echo "$OUTPUT" | sed -n 's/Total predefined tests run\: \([0-9]\+\)/\1/p')
    PASSED=$(echo "$OUTPUT" | sed -n 's/Number of predefined tests that passed\: \([0-9]\+\)/\1/p')
    if [ "$PASSED" -eq "$TOTAL" ]; then
        return 0
    else
        return 1
    fi
}

# validate output with provided jars
echo '=== TesterTypeCheck.jar'
test_jar ../supporting_files/jars/TesterTypeCheck.jar
echo '=== RegexPatternMatch.jar'
test_jar ../supporting_files/jars/RegexPatternMatch.jar
echo '=== CommandLineEncryption.jar'
test_jar ../supporting_files/jars/CommandLineEncryption.jar
echo '=== LeetConverter.jar'
test_jar ../supporting_files/jars/LeetConverter.jar
