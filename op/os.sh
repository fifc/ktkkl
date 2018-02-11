#!/usr/bin/zsh

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
	server_status lhs_os
	exit 0
fi

if [[ "$0" =~ '.*\.status$' ]]; then
	server_status lhs_os
	exit 0
fi

if ! ( stop_proc lhs_os 5 ) then
	echo "error stop process!"
	exit 1
fi

if [[ "$0" =~ '.*\.stop$' ]]; then
	exit 0
fi

rm -f ./log/lhs_os.hn.*

if [ -f os.new ]; then
	echo "updating lhs_os ..."
	mv os.new lhs_os
fi

echo "starting lhs_os ..."

ulimit -c unlimited
cmd="nohup ./lhs_os -v=90 -logbuflevel=-1"
eval exec ${cmd} > log/os.log 2>&1 &
#./lhs_os -v=90 -logbuflevel=-1

