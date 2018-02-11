#!/usr/bin/zsh

ulimit -c unlimited

cmd="nohup ./lhs_os -v=90 -logbuflevel=-1"
#cmd="nohup ./codein"

eval exec ${cmd} > log/lhs_os.log 2>&1 &


