package com.github.timurstrekalov.saga.core;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileStatsTest {

    @Test
    public void getParentName() {
        assertEquals("test/path/bla", new FileStats(System.getProperty("user.dir") + "\\test\\path\\bla/file.js",
                Lists.<LineCoverageRecord>newLinkedList(), true).getParentName());
    }

}
