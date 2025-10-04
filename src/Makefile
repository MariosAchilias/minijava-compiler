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
	rm `find -name "*.class"`
	rm -rf Parser/syntaxtree Parser/visitor Parser/*.java Parser/minijava-jtb.jj Exceptions/ParseException.java