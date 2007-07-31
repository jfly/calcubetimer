CLASSPATH=.:contrib
EXEC=net.gnehzr.cct.main.CALCubeTimer
SERVER=net.gnehzr.cct.umts.server.CCTServer

PATHS=net/gnehzr scramblePlugins

all:
	java -cp ${CLASSPATH} ${EXEC}

compile:
	javac -cp ${CLASSPATH} `find ${PATHS} -regex .*java`

server:
	java -cp ${CLASSPATH} ${SERVER} pass

clean:
	rm -f `find ${PATHS} -regex .*class`

rclean: clean
	rm -f cct.properties

cycle: compile all rclean
