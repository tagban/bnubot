#!/bin/sh
PIDFILE=bnubot.pid
if [ -f $PIDFILE ] ; then
        echo "it is still running"
else
        echo "starting a new one"
        java -cp mysql-connector-java-5.0.6-bin.jar:BNUBot.jar net.bnubot.Main -plugins net.bnubot.bot.html.HTMLOutputEventHandler > stdout.txt 2> stderr.txt &
        pid=$!
        echo $pid > $PIDFILE
fi

