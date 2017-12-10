
d=/root
f=""

cd $d

if [ -d $d/lhs_kt ]; then
	rm -rf $d/lhs_kt/gradle* $d/lhs_kt/build
	f="$f lhs_kt"
fi

if [ -d $d/robot ]; then
	rm -rf $d/robot/gradle* $d/robot/build $d/robot/lib
	f="$f robot"
fi

if [ -d $d/lhs_server ]; then
	cd $d/lhs_server && make clean
	cd $d/lhs_server/lib && make clean
	f="$f lhs_server"
fi

if [ -d $d/lhs_os ]; then
	cd $d/lhs_os && make clean
	f="$f lhs_os"
fi

if [ -d $d/lhs_push ]; then
	cd $d/lhs_push && make clean
	f="$f lhs_push"
fi

if [ -d $d/lhs_mqtt ]; then
	cd $d/lhs_mqtt && make clean
	f="$f lhs_mqtt"
fi

if [ -d $d/lhs_cat ]; then
	cd $d/lhs_cat && rm -rf node_modules
	f="$f lhs_cat"
fi
cd $d
#rm -rf lhs_server/.git lhs_proto/.git lhs_kt/.git robot/.git lhs_os/.git lhs_push/.git lhs_mqtt/.git lhs_cat/.git

tar Jcf /tmp/tx $f
if [ $? -ne 0 ]; then
	echo "tr error!"
	#exit 1
fi

ls -l /tmp

scp /tmp/tx root@iin.im: && echo 'sc ok!' 

