package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Map;

/**
 * Created by mturski on 11/8/2016.
 */
public interface XMLNode {
    long getId();

    @Nullable
    String getNamespace();

    void setNamespace(@Nullable String namespace);

    @Nullable
    String getPrefix();

    void setPrefix(@Nullable String prefix);

    @NotNull
    String getLocalName();

    void setLocalName(@NotNull String name);

    @NotNull
    String getQName();

    @Nullable
    String getText();

    void setText(@Nullable String text);

    @NotNull
    Map<String, String> getAttributes();

    void setAttributes(@NotNull Map<String, String> attributeByName);

    boolean hasAttribute(@NotNull String name);

    @Nullable
    String getAttribute(@Nullable String name);

    void setAttribute(@NotNull String name, @NotNull String value);
}
