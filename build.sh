#!/bin/bash
# build.sh - Compiles all MiniJava compiler source files
set -e
mkdir -p bin
echo "Compiling MiniJava compiler..."
javac -d bin src/*.java
echo "Build successful! Compiler is in bin/"
echo "Run with: java -cp bin MiniJava test/Factorial.java"
