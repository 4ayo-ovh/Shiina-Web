install:
	apt install -y openjdk-21-jdk
	apt install -y maven
	mvn clean install

run:
	mvn exec:java -Dexec.mainClass="dev.osunolimits.main.App"
	