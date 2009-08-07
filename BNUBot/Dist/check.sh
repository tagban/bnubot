#!/bin/sh
PIDFILE=bnubot.pid
if [ -f $PIDFILE ] ; then
	PID=`cat $PIDFILE`
	echo "Found $PIDFILE: looking for pid $PID"
	if [ "$(ps -ef | grep java | grep $PID | grep -v grep | wc -l)" -ge 1 ]; then
		LOGFILE="logs/x/`date +%F`.log"
		echo "it is still running; checking logfile \"$LOGFILE\" for errors"
		HASERRORS=`tail -1 $LOGFILE | grep -c "\[COLOR:#ce3e3e\]"`
		if [ $HASERRORS -gt 0 ]; then
			echo "has errors"
		else
			echo "no errors"
			exit
		fi
	fi
fi

# We determined that BNUBot is not running
./run.sh
