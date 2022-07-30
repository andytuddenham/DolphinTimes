
.PHONY: jar
jar: DolphinTimes.jar

.PHONY: run
run: DolphinTimes.jar
	java -jar ./DolphinTimes.jar

.PHONY: release
release: DolphinTimes.zip

DolphinTimes.zip: DolphinTimes.zip(DolphinTimes.jar) DolphinTimes.zip(dolpintimes.properties) DolphinTimes.zip(res/images/icon.ico)

DolphinTimes.zip(%):
	zip -j $@ $%

DolphinTimes.zip(dolpintimes.properties): res/config/dolphintimes.properties
	@echo ==== Copying "res/config/dolphintimes.properties" make sure this is okay for release. ====
	-zip -j $@ $^

.PHONY: clean
clean:
	-rm DolphinTimes.jar
	-rm -r build

DolphinTimes.jar: src/com/tudders/dolphin/times/* res/help/* res/images/* build/
	javac -sourcepath src -d build src/com/tudders/dolphin/times/Main.java
	jar cfm DolphinTimes.jar manifest -C build com -C res images -C res help

build/:
	mkdir -p build
