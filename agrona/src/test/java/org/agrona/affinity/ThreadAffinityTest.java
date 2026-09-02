package org.agrona.affinity;

import org.agrona.SystemUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ThreadAffinityTest
{

    @Test
    void setAndGetAffinity()
    {
        assumeTrue(SystemUtil.isLinux());
        ThreadAffinity.setAffinity(5);
        assertEquals(5, ThreadAffinity.getAffinity());
    }
}