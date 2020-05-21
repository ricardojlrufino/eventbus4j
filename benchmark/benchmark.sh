#!/bin/bash

# java -XX:+PrintCompilation -verbose:gc -jar target/benchmarks.jar
java -Xms256m -Xmx256m -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar target/benchmarks.jar
