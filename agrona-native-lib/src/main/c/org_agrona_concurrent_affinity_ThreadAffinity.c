#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <sched.h>
#include <sys/sysinfo.h>
#include <jni.h>
#include <errno.h>
#include <inttypes.h>
#include <stdlib.h>
#include <unistd.h>

#include "org_agrona_concurrent_affinity_ThreadAffinity.h"


JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinityFor(
    JNIEnv *env, jclass clz, jint tid, jint cpu)
{
    // TODO: Refactor with similar code
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);
    cpu_set_t *mask;
    mask = CPU_ALLOC(num_cpus);
    CPU_ZERO_S(mask_alloc_size, mask);
    CPU_SET_S(cpu, mask_alloc_size, mask);
    if (sched_setaffinity(tid, mask_alloc_size, mask) < 0)
    {
        CPU_FREE(mask);
        // AERON_SET_ERR(errno, "failed to set thread affinity name=%s, cpu_affinity_no=%" PRIu8, name, cpu_affinity_no);
        // return -1;
        // TODO: Raise exception
    }
    CPU_FREE(mask);
}

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinity(JNIEnv *env, jclass clz, jint cpu)
{
    return Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinityFor(env, clz, 0, cpu);
}

JNIEXPORT jint JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinityFor(JNIEnv *env, jclass clz, jint tid)
{
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);

    cpu_set_t *mask;
    mask = CPU_ALLOC(num_cpus);
    CPU_ZERO_S(mask_alloc_size, mask);
    if (sched_getaffinity(tid, mask_alloc_size, mask) < 0)
    {
        // AERON_SET_ERR(errno, "%s", "failed to get thread affinity");
        CPU_FREE(mask);
        return -1;
    }

    for (size_t cpu = 0; cpu < num_cpus; cpu++)
    {
        if (CPU_ISSET_S(cpu, num_cpus, mask))
        {
            return cpu;
            break;
        }
    }
    CPU_FREE(mask);
    return -1;
}


JNIEXPORT jint JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinity(JNIEnv *env, jclass clz)
{
    return Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinityFor(env, clz, 0);
}