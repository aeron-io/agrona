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

#ifdef __cplusplus
}
#endif
#endif
