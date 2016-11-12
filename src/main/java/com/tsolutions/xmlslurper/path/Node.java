package com.tsolutions.xmlslurper.path;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public interface Node {
    long getId();

    void setName(@NotNull String name);

    @NotNull
    String getName();

    void setText(@Nullable String text);

    @Nullable
    String getText();

    void setAttributes(@NotNull Map<String, String> attributeByName);

    @NotNull
    Map<String, String> getAttributes();

    void setAttribute(@NotNull String name, @NotNull String value);

    @Nullable
    String getAttribute(@Nullable String name);
}
