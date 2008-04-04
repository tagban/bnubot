#!/bin/sh
PIDFILE=bnubot.pid
if [ -f $PIDFILE ] ; then
	echo "it is still running"
else
	echo "starting a new one"
	java -cp `ls -1 lib/*.jar | tr '\n' ':'`BNUBot.jar net.bnubot.Main -logfile log.txt 2> stderr.txt &
	pid=$!
	echo $pid > $PIDFILE
fi

