#!/bin/sh
java -cp `ls -1 lib/*.jar | tr '\n' ':'`BNUBot.jar net.bnubot.Main -stdout -cli -nogui
