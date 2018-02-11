#ifndef _SYS_CALL_UTILS_
#define _SYS_CALL_UTILS_

#ifndef _GNU_SOURCE
#define _GNU_SOURCE         /* See feature_test_macros(7) */
#endif

#include <unistd.h>
#include <sys/syscall.h>   /* For SYS_xxx definitions */

#ifndef __NR_gettid
#if ! defined __i386__
#error "Must define __NR_gettid for non-x86 platforms"
#endif
#define __NR_gettid 224
#endif

#define get_tid() syscall(__NR_gettid)

#endif // _SYS_CALL_UTILS_
