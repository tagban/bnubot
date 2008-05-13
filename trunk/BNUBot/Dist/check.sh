#!/bin/sh
PIDFILE=bnubot.pid
if [ -f $PIDFILE ] ; then
    PID=`cat $PIDFILE`
    echo "Found $PIDFILE: looking for pid $PID"
    if [ "$(ps -ef | grep java | grep $PID | grep -v grep | wc -l)" -ge 1 ]; then
        echo "it is still running"
        exit
    fi
fi

# We determined that BNUBot is not running
./run.sh
