#include <jni.h>

#ifndef _Included_org_agrona_affinity_ThreadAffinity
#define _Included_org_agrona_affinity_ThreadAffinity
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinity
  (JNIEnv *, jclass, jint);

JNIEXPORT jint JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinity
  (JNIEnv *, jclass);

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinityFor
  (JNIEnv *, jclass, jint, jint);

JNIEXPORT void JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeSetAffinitiesFor
  (JNIEnv *, jclass, jint, jintArray);

JNIEXPORT jint JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinityFor
  (JNIEnv *, jclass, jint);

JNIEXPORT jintArray JNICALL Java_org_agrona_concurrent_affinity_ThreadAffinity_nativeGetAffinitiesFor
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
