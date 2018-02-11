#!/usr/bin/zsh

#ssl_key=cert/codein.key
#ssl_cert=cert/codein.cert
ssl_key=cert/gw.key
ssl_cert=cert/gw.pem

get_pid() {
	pids=`pidof $1`
	for p in `echo $pids`; do
		tmp=`ps -oargs -p $p | grep 'logbuflevel'`
		if [ -n "$tmp" ]; then
			return $p
		fi
	done
	return -1
}

stop_proc() {
	while (true) do
		get_pid $1
		pid=$?
		if (( $pid < 0 )) then
			return 0
		fi
		echo -n "stopping $pid ... "
		kill $pid
		i=0
		for (( ; i < $2 * 10; ++i)); do
			get_pid $1
			if (( $? < 0 )) then
				echo "ok"
				break
			fi
			sleep 0.1
		done
		if (( $i >= $2 * 10 )) then
			echo "fail!"
			break
		fi
	done

	return 1
}

server_status() {
	get_pid $1
	pid=$?
	if (( $pid < 0 )) then
		echo "$1 is not running ..."
	else
		stime=`ps -olstart -p $pid|grep -v STARTED`
		echo "$1($pid) is running since $stime ..."
	fi
}

if [ "$1" = "status" ]; then
	server_status lhs_server
	server_status http-lhs_server
	exit 0
fi

if [[ "$0" =~ '.*\.status$' ]]; then
	server_status lhs_server
	server_status http-lhs_server
	exit 0
fi

if ! ( stop_proc lhs_server 5 ) then
	echo "error stop process!"
	exit 1
fi

if ! ( stop_proc http-lhs_server 5 ) then
	echo "error stop http process!"
	exit 1
fi

if [[ "$0" =~ '.*\.stop$' ]]; then
	exit 0
fi

if [ "$1" = "stop" ]; then
	exit 0
fi

rm -f ./log/lhs_server.hn.*
rm -f ./log/http-lhs_server.hn.*

if [ -f server.new ]; then
	echo "updating lhs_server ..."
	mv server.new lhs_server
fi

ulimit -c unlimited
echo "starting lhs_server ..."
cmd="nohup ./lhs_server -v=90 -logbuflevel=-1 -port=https -key=${ssl_key} -cert=${ssl_cert}"
eval exec ${cmd} > log/server.log 2>&1 &

echo "starting http-lhs_server ..."
cmd="nohup ./http-lhs_server -v=90 -logbuflevel=-1 -port 10080 -no_mqtt"
eval exec ${cmd} > log/http-server.log 2>&1 &

