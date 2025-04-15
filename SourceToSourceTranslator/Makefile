all: compile

compile:
	jflex scanner.flex
	copy Lexer.java SourceToIR/Lexer.java
	copy Lexer.java IRToJava/Lexer.java

execute:
	java -cp ../java-cup-11b-runtime.jar;. Main

clean:
	rm -f *.class *~
