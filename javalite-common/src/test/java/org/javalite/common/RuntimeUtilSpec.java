package org.javalite.common;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import static org.javalite.common.RuntimeUtil.execute;
import static org.javalite.test.jspec.JSpec.the;
import static org.junit.Assume.assumeFalse;

/**
 * @author igor on 9/23/18.
 */
public class RuntimeUtilSpec {

    @Test
    public void shouldUseDefaultBufferSize(){
        assumeFalse(SystemUtils.IS_OS_WINDOWS); // will skip these tests on Windows,

        the(execute("ls -ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldUseDefaultBufferSizeSplitArguments(){
        assumeFalse(SystemUtils.IS_OS_WINDOWS);

        the(execute("ls", "-ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldOverrideBufferSize(){
        assumeFalse(SystemUtils.IS_OS_WINDOWS);

        the(execute(4096, "ls", "-ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldOverrideBufferSizeSplitArguments(){
        assumeFalse(SystemUtils.IS_OS_WINDOWS);

        the(execute(4096, "ls -ls").out).shouldContain("pom.xml");
    }
}
