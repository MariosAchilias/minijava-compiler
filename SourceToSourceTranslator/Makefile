all: build

build:
	jflex scanner.flex
	cp Lexer.java SourceToIR/Lexer.java
	sed -i '1ipackage SourceToSourceTranslator.SourceToIR;' SourceToIR/Lexer.java
	cp Lexer.java IRToJava/Lexer.java
	sed -i '1ipackage SourceToSourceTranslator.IRToJava;' IRToJava/Lexer.java
	make -C SourceToIR
	make -C IRToJava

execute:
	java -cp "SourceToSourceTranslator/java-cup-11b-runtime.jar:." SourceToSourceTranslator.SourceToIR.Main > Translated.ir
	java -cp "SourceToSourceTranslator/java-cup-11b-runtime.jar:." SourceToSourceTranslator.IRToJava.Main < Translated.ir > Translated.java

clean:
	rm -f *.class *~ Lexer.java
	make -C SourceToIR clean
	make -C IRToJava clean
