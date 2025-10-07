ifeq ($(OS), Windows_NT)
	PS = powershell
	MKDIR = New-Item -ItemType directory -Force
	SCRIPT = fix_packages.ps1
	RM_CLASSFILES = Remove-Item -Path *.class -Recurse -Force
else
	MKDIR = mkdir -p
	SCRIPT = fix_packages.sh
	RM_CLASSFILES = rm `find -name "*.class"`
endif

all: compile

javacc5.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/javacc5.jar --output javacc5.jar

jtb132di.jar:
	curl https://cgi.di.uoa.gr/~compilers/tools/jtb132di.jar --output jtb132di.jar

compile: javacc5.jar jtb132di.jar
	$(PS) $(MKDIR) Parser
	cd Parser && java -jar ../jtb132di.jar -te minijava.jj
	cd Parser && java -jar ../javacc5.jar minijava-jtb.jj
	$(PS) ./$(SCRIPT)
	javac Main.java

clean:
	$(PS) $(RM_CLASSFILES)
	rm -rf Parser/syntaxtree Parser/visitor Parser/*.java Parser/minijava-jtb.jj Exceptions/ParseException.java