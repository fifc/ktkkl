#!/usr/bin/zsh
logfile=`pwd -P`/log/cat.log
proc_name=node

get_pid() {
	pids=`pidof ${proc_name}`
	for p in `echo $pids`; do
		tmp=`ps -oargs -p $p | grep 'cat.js'`
		if [ -n "$tmp" ]; then
			return $p
		fi
	done
	return -1
}

stop_proc() {
	while (true) do
		get_pid
		pid=$?
		if (( $pid < 0 )) then
			return 0
		fi
		echo "stopping $pid ..."
		kill $pid
		i=0
		for (( ; i < 50; ++i)); do
			get_pid
			if (( $? < 0 )) then
				break
			fi
			sleep 0.1
		done
		if (( $i >= $1 * 10 )) then
			break
		fi
	done

	return 1
}

server_status() {
	get_pid
	pid=$?
	if (( $pid < 0 )) then
		echo "cat is not running ..."
	else
		stime=`ps -olstart -p $pid|grep -v STARTED`
		echo "kt($pid) is running since $stime ..."
	fi
}

if [ "$1" = "status" ]; then
	server_status
	exit 0
fi

if [[ "$0" =~ '.*\.status$' ]]; then
	server_status
	exit 0
fi

if ! ( stop_proc 5 ) then
	echo "error stop process!"
	exit 1
fi

if [[ "$0" =~ '.*\.stop$' ]]; then
	exit 0
fi

if [ "$1" = "stop" ]; then
	exit 0
fi


if [ -f cat.js.new ]; then
	echo "updating lhs_cat ..."
	mv cat.js.new cat.js
fi

nohup ${proc_name} cat.js > ${logfile} 2>&1 &
get_pid
pid=$?
if (( $pid < 0 )) then
	echo "failed"
else
	echo "pid: $pid"
fi


