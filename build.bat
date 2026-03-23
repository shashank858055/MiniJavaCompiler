@echo off
echo Compiling MiniJava compiler...
if not exist bin mkdir bin
javac -d bin src\*.java
echo Build successful! 
echo Run with: java -cp bin MiniJava test\Factorial.java
