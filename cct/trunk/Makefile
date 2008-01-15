CLASSPATH=.:mailapi.jar:substance.jar
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
	rm -rf profiles/Guest/*

cycle: compile all rclean

dist: compile
	-mkdir dist
	-jar cmf files/cct.mf ${DIST} net/gnehzr/cct/{*,umts/client}/*.{class,png,ttf,jpg,html}
	cd contrib && jar uf ../${DIST} `find|sed -n '/\.svn/d;/META-INF/d;/class$$/p;/png$$/p;/properties$$/p;/license$$/p;/ttf$$/p;/gif$$/p'`
	jar cmf files/cctserver.mf ${SERVERDIST} net/gnehzr/cct/umts/{,server/}*.class
	cp ${DISTCOPYFILES} dist/
	-for i in `find ${DISTCOPYDIRS}|grep /$$` ; do mkdir dist/$$i ; done
	for i in ${DISTCOPYDIRS} ; do cp `find $$i|grep -v svn|grep -v /$$` dist/$$i ; done
