package org.xs4j;

import com.sun.istack.NotNull;
import org.xs4j.path.SlurpNode;

import java.io.InputStream;

/**
 * Created by mturski on 11/8/2016.
 */
public interface XMLSlurper {
    @NotNull
    SlurpNode getNodes();

    void parse(@NotNull InputStream inputStream) throws Exception;
}
