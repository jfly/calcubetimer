CLASSPATH=.:CCTRequired.jar
EXEC=net.gnehzr.cct.main.CALCubeTimer

all:
	java -cp ${CLASSPATH} ${EXEC}

compile:
	javac -cp ${CLASSPATH} `find net/gnehzr -regex .*java`

server:
	java -cp ${CLASSPATH} net.gnehzr.cct.umts.server.CCTServer pass

clean:
	rm `find net/gnehzr -regex .*class`

rclean: clean
	rm cct.properties

cycle: compile all rclean
