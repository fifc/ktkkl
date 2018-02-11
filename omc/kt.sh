#!/usr/bin/zsh
# sed -i 's|\(/root/\.gradle/[^:]*\)/\([^:]*\)\.jar|$J/\2.jar|g' r.sh

OPTIONS='-Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant'
#JAVA=/opt/java/bin/java
JAVA=java
logfile=`pwd -P`/log/kt.log
proc_name=java

get_pid() {
	pids=`pidof ${proc_name}`
	for p in `echo $pids`; do
		tmp=`ps -oargs -p $p | grep 'lhs_kt.*.war'`
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
		echo "kt is not running ..."
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



if [ -f lhs_kt.war.new ]; then
	echo "updating lhs_kt ..."
	mv lhs_kt.war.new lhs_kt.war
fi

nohup ${JAVA} -jar lhs_kt.war > ${logfile} 2>&1 &
get_pid
pid=$?
if (( $pid < 0 )) then
	echo "failed"
else
	echo "pid: $pid"
fi

exit 0

cd kt || exit

class_path=WEB-INF/classes
for jar in WEB-INF/lib/*.jar; do
	class_path=${class_path}:${jar}
done

#nohup ${JAVA} ${OPTIONS}  -cp ${class_path} tv.codein.kt.ApplicationKt > ${logfile} 2>&1 &
