@echo off
if "%1"=="" (
    echo Usage: run.bat ^<file.java^> [--lex^|--parse^|--check]
    exit /b 1
)
java -cp bin MiniJava %*
