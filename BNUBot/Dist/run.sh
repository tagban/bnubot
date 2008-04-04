#!/bin/sh
PIDFILE=bnubot.pid
if [ -f $PIDFILE ] ; then
	echo 'Killing BNUBot from PID file'
	PID=`cat $PIDFILE`
	kill -3 $PID
	if kill -9 $PID ; then
		echo "java process stopped"
		rm -f $PIDFILE
	else
		echo "java process could not be stopped!"
		exit
	fi
fi

echo "starting a new one"
java -cp `ls -1 lib/*.jar | tr '\n' ':'`BNUBot.jar net.bnubot.Main -logfile log.txt 2> stderr.txt &
pid=$!
echo $pid > $PIDFILE
