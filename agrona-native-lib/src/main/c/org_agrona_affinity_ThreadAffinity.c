#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <sched.h>
#include <sys/sysinfo.h>
#include <jni.h>
#include <errno.h>
#include <inttypes.h>
#include <stdlib.h>

#include "org_agrona_affinity_ThreadAffinity.h"

JNIEXPORT void JNICALL Java_org_agrona_affinity_ThreadAffinity_setAffinity(JNIEnv *env, jclass clz, jint cpu)
{
    cpu_set_t mask;
    CPU_ZERO(&mask);
    CPU_SET(cpu, &mask);
    if (sched_setaffinity(0, sizeof(mask), &mask) < 0)
    {
        // AERON_SET_ERR(errno, "failed to set thread affinity name=%s, cpu_affinity_no=%" PRIu8, name, cpu_affinity_no);
        // return -1;
        // TODO: Raise exception
    }
}


JNIEXPORT jint JNICALL Java_org_agrona_affinity_ThreadAffinity_getAffinity(JNIEnv *env, jclass clz)
{
    cpu_set_t mask;
    CPU_ZERO(&mask);
    if (sched_getaffinity(0, sizeof(mask), &mask) < 0)
    {
        // AERON_SET_ERR(errno, "%s", "failed to get thread affinity");
        return -1;
    }

    for (uint8_t i = 0; i < UINT8_MAX; i++)
    {
        if (CPU_ISSET(i, &mask))
        {
            return i;
            break;
        }
    }
    return 0;
}