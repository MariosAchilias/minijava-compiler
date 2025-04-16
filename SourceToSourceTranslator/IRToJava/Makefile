all: compile

compile:
	java -jar ../java-cup-11b.jar -interface -parser Parser IRParser.cup
	javac -cp "../java-cup-11b-runtime.jar:." *.java

clean:
	rm -f *.class *~ Parser.java sym.java