

.PHONY: run
run: DolphinTimes.jar
	java -jar ./DolphinTimes.jar DolphinTimes.properties

.PHONY: jar
jar: DolphinTimes.jar

.PHONY: clean
clean:
	-rm DolphinTimes.jar
	-rm -r build

DolphinTimes.jar: src/com/tudders/dolphin/times/* res/help/* res/images/* build/
	javac -sourcepath src -d build src/com/tudders/dolphin/times/Main.java
	jar cfm DolphinTimes.jar manifest -C build com -C res images -C res help

build/:
	mkdir -p build
