package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public interface XMLNode {
    long getId();

    void setLocalName(@NotNull String name);

    @NotNull
    String getLocalName();

    void setText(@Nullable String text);

    @Nullable
    String getText();

    void setAttributes(@NotNull Map<String, String> attributeByName);

    @NotNull
    Map<String, String> getAttributes();

    void setAttribute(@NotNull String name, @NotNull String value);

    @Nullable
    String getAttribute(@Nullable String name);

    boolean hasAttribute(@NotNull String name);
}
