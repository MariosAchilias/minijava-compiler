all: compile

compile:
	jflex scanner.flex
	cp Lexer.java SourceToIR/Lexer.java
	sed -i '1ipackage SourceToSourceTranslator.SourceToIR;' SourceToIR/Lexer.java
	cp Lexer.java IRToJava/Lexer.java
	sed -i '1ipackage SourceToSourceTranslator.IRToJava;' IRToJava/Lexer.java

execute:
	java -cp ../java-cup-11b-runtime.jar;. Main

clean:
	rm -f *.class *~
