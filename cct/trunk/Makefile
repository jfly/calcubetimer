CLASSPATH=.:mailapi.jar:substance-lite-feel.jar:javazoom.jar:pircbot.jar
EXEC=net.gnehzr.cct.main.CALCubeTimer
SERVER=net.gnehzr.cct.umts.server.CCTServer

PATHS=net/gnehzr/ scramblePlugins/ say/swing/

DIST=dist/CALCubeTimer.jar
SERVERDIST=dist/CCTServer.jar
DISTCOPYDIRS=guiLayouts/ scramblePlugins/
DISTCOPYFILES=defaults.properties

all:
	java -cp ${CLASSPATH} ${EXEC}

compile:
	javac -cp ${CLASSPATH} `find ${PATHS} -regex .*java`

warnings:
	javac -cp ${CLASSPATH} -Xlint:unchecked `find ${PATHS} -regex .*java`

server:
	java -cp ${CLASSPATH} ${SERVER} pass

clean:
	rm -f `find ${PATHS} -regex .*class`
	rm -rf dist

rclean: clean
	rm -rf profiles/Guest

cycle: compile all rclean
