.PHONY: all clean distclean

CC=gcc
CXX=g++
LDFLAGS=-g -O2 -L../lhs_proto -L/g/pkg/lib -pthread -Wl,-rpath,/g/pkg/lib
CFLAGS=-I../lhs_proto -I../lhs_server/lib -I/g/pkg/include/ -pthread -g -O2 -MD -MP -Wall -Werror
CXXFLAGS=-I../lhs_proto -I../lhs_server/lib -I/g/pkg/include/ -pthread -g -O2 -MD -MP -Wall -Werror


all : auth_plugin.so mqttest


auth_plugin.so : auth_plugin.cc
	$(CXX) ${CXXFLAGS} -fPIC -shared $^ -o $@ $(LDFLAGS) -lpq_uv -lpq -ljemalloc -lrdkafka -lkafka_proto -lcommon_proto -lprotobuf

PROTOS=-L../lhs_proto -lprotobuf -lpush_proto -lcodein_proto -lcommon_proto -lmessaging_proto -luser_proto -lpush_proto
GRPCLIBS=`pkg-config --libs grpc++ grpc` -Wl,--no-as-needed -lgrpc++_reflection 

mqttest_OBJS = client_shared.o mqttest.o
mqttest : ${mqttest_OBJS}
	$(CXX) ${mqttest_OBJS} -o $@ $(LDFLAGS) ${PROTOS} -lmosquitto -ljemalloc

distclean : clean
	-rm -f *.orig

clean :
	rm -f *.so *.o *.d
