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

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinitiesFor
  (JNIEnv *env, jclass clz, jint tid, jintArray cpus)
{
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);
    cpu_set_t *mask = CPU_ALLOC(num_cpus);
    CPU_ZERO_S(mask_alloc_size, mask);

    jsize cpu_len = (*env)->GetArrayLength(env, cpus);
    jint *cpus_arr = (*env)->GetIntArrayElements(env, cpus, NULL);

    for (jsize i = 0; i < cpu_len; i++)
    {
        CPU_SET_S(cpus_arr[i], mask_alloc_size, mask);
    }

    if (sched_setaffinity(tid, mask_alloc_size, mask) < 0)
    {
        CPU_FREE(mask);
        // AERON_SET_ERR(errno, "failed to set thread affinity name=%s, cpu_affinity_no=%" PRIu8, name, cpu_affinity_no);
        // return -1;
        // TODO: Raise exception
    }
    CPU_FREE(mask);
    (*env)->ReleaseIntArrayElements(env, cpus, cpus_arr, JNI_ABORT);
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

JNIEXPORT jintArray JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinitiesFor
  (JNIEnv *env, jclass clz, jint tid)
{
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);

    cpu_set_t *mask = CPU_ALLOC(num_cpus);
    CPU_ZERO_S(mask_alloc_size, mask);
    if (sched_getaffinity(tid, mask_alloc_size, mask) < 0)
    {
        // AERON_SET_ERR(errno, "%s", "failed to get thread affinity");
        CPU_FREE(mask);
        return NULL;
    }

    const size_t count = CPU_COUNT_S(num_cpus, mask);
    jint *cpus_arr = (jint *)malloc(count * sizeof(jint));
    if (cpus_arr == NULL)
    {
        CPU_FREE(mask);
        return NULL;
    }

    size_t tracked_count = 0;
    for (size_t cpu = 0; cpu < num_cpus; cpu++)
    {
        if (CPU_ISSET_S(cpu, num_cpus, mask))
        {
            cpus_arr[tracked_count++] = cpu;
        }
    }
    CPU_FREE(mask);
    jintArray cpu_result = (*env)->NewIntArray(env, count);
    if (cpu_result == NULL)
    {
        free(cpus_arr);
        return NULL;
    }

    (*env)->SetIntArrayRegion(env, cpu_result, 0, count, cpus_arr);
    free(cpus_arr);
    return cpu_result;
}

JNIEXPORT jint JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinity(JNIEnv *env, jclass clz)
{
    return Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinityFor(env, clz, 0);
}