#!/usr/bin/zsh

ulimit -c unlimited

cmd="nohup ./lhs_server -v=90 -logbuflevel=-1 -port=https -key=cert/codein.key -cert=cert/codein.cert"
#cmd="nohup ./codein"

eval exec ${cmd} > log/lhs_server.log 2>&1 &


