#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <sched.h>
#include <sys/sysinfo.h>
#include <jni.h>
#include <errno.h>
#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "org_agrona_concurrent_affinity_ThreadAffinity.h"

#define OOM_EXCEPTION "java/lang/OutOfMemoryError"
#define THREAD_AFFINITY_EXCEPTION "org/agrona/concurrent/affinity/ThreadAffinityException"

static void handle_thread_affinity_error(JNIEnv *env, jint tid, cpu_set_t *mask, const char *action) {
    const int err = errno;
    CPU_FREE(mask);
    char msg[256];
    snprintf(msg, sizeof(msg), "failed to %s thread affinity tid=%d: %s", action, tid, strerror(err));
    jclass ex = (*env)->FindClass(env, THREAD_AFFINITY_EXCEPTION);
    if (ex != NULL)
    {
        (*env)->ThrowNew(env, ex, msg);
    }
}

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinity(
    JNIEnv *env, jclass clz, jint tid, jintArray cpus)
{
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);
    cpu_set_t *mask = CPU_ALLOC(num_cpus);
    if (mask == NULL)
    {
        jclass ex = (*env)->FindClass(env, OOM_EXCEPTION);
        if (ex != NULL)
        {
            (*env)->ThrowNew(env, ex, "failed to allocate CPU mask");
        }
        return;
    }
    CPU_ZERO_S(mask_alloc_size, mask);

    jsize cpu_len = (*env)->GetArrayLength(env, cpus);
    jint *cpus_arr = (*env)->GetIntArrayElements(env, cpus, NULL);
    if (cpus_arr == NULL)
    {
        CPU_FREE(mask);
        return;
    }

    for (jsize i = 0; i < cpu_len; i++)
    {
        CPU_SET_S(cpus_arr[i], mask_alloc_size, mask);
    }
    (*env)->ReleaseIntArrayElements(env, cpus, cpus_arr, JNI_ABORT);

    if (sched_setaffinity(tid, mask_alloc_size, mask) < 0)
    {
        handle_thread_affinity_error(env, tid, mask, "set");
        return;
    }
    CPU_FREE(mask);
}

JNIEXPORT jintArray JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinity(
    JNIEnv *env, jclass clz, jint tid)
{
    const size_t num_cpus = sysconf(_SC_NPROCESSORS_CONF);
    const size_t mask_alloc_size = CPU_ALLOC_SIZE(num_cpus);
    cpu_set_t *mask = CPU_ALLOC(num_cpus);
    if (mask == NULL)
    {
        jclass ex = (*env)->FindClass(env, OOM_EXCEPTION);
        if (ex != NULL)
        {
            (*env)->ThrowNew(env, ex, "failed to allocate CPU mask");
        }
        return NULL;
    }

    CPU_ZERO_S(mask_alloc_size, mask);
    if (sched_getaffinity(tid, mask_alloc_size, mask) < 0)
    {
        handle_thread_affinity_error(env, tid, mask, "get");
        return NULL;
    }

    const size_t count = CPU_COUNT_S(mask_alloc_size, mask);
    jint *cpus_arr = (jint *)malloc(count * sizeof(jint));
    if (cpus_arr == NULL)
    {
        CPU_FREE(mask);
        jclass ex = (*env)->FindClass(env, OOM_EXCEPTION);
        if (ex != NULL)
        {
            (*env)->ThrowNew(env, ex, "failed to allocate CPU array");
        }
        return NULL;
    }

    size_t tracked_count = 0;
    for (size_t cpu = 0; cpu < num_cpus; cpu++)
    {
        if (CPU_ISSET_S(cpu, mask_alloc_size, mask))
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
