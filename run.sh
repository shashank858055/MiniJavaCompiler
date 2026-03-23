#!/bin/bash
# run.sh - Quick runner for MiniJava programs
if [ -z "$1" ]; then
    echo "Usage: ./run.sh <file.java> [--lex|--parse|--check]"
    exit 1
fi
java -cp bin MiniJava "$@"
