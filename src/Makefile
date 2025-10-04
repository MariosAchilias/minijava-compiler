all: compile

javacc5.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/javacc5.jar --output javacc5.jar

jtb132di.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/jtb132di.jar --output jtb132di.jar

compile: javacc5.jar jtb132di.jar
	mkdir -p Parser
	cd Parser && java -jar ../jtb132di.jar -te minijava.jj
	cd Parser && java -jar ../javacc5.jar minijava-jtb.jj
	./fix_packages.sh
	javac Main.java

clean:
	rm -rf *.class Parser/*.class SymbolTable/*.class
	rm -rf Parser/syntaxtree Parser/visitor Parser/*.java Exceptions/ParseException.java
	rm -rf *.class ParserParser/*.class SymbolTable/*.class syntaxtree visitor minijava-jtb.jj *~ ParseException.java MiniJavaParser*.java JavaCharStream.java Token.java TokenMgrError.java