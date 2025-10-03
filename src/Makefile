all: compile

javacc5.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/javacc5.jar --output javacc5.jar

jtb132di.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/jtb132di.jar --output jtb132di.jar

compile: javacc5.jar jtb132di.jar
	java -jar jtb132di.jar -te minijava.jj
	java -jar javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -rf *.class SymbolTable/*.class syntaxtree visitor minijava-jtb.jj *~ ParseException.java MiniJavaParser*.java JavaCharStream.java Token.java TokenMgrError.java