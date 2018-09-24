package org.javalite.common;

import org.junit.Test;

import static org.javalite.common.RuntimeUtil.execute;
import static org.javalite.test.jspec.JSpec.the;

/**
 * @author igor on 9/23/18.
 */
public class RuntimeUtilSpec {

    @Test
    public void shouldUseDefaultBufferSize(){
        the(execute("ls -ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldUseDefaultBufferSizeSplitArguments(){
        the(execute("ls", "-ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldOverrideBufferSize(){
        the(execute(4096, "ls", "-ls").out).shouldContain("pom.xml");
    }

    @Test
    public void shouldOverrideBufferSizeSplitArguments(){
        the(execute(4096, "ls -ls").out).shouldContain("pom.xml");
    }
}
