package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import java.util.Map;

/**
 * A data structure holding information about a XML element.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface XMLNode {
    long getId();

    long getPosition();

    @Nullable
    XMLNode getParent();

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

    void setAttributes(@NotNull Map<String, String> attributeByQName);

    boolean hasAttribute(@NotNull String name);

    @Nullable
    String getAttribute(@Nullable String name);

    void setAttribute(@NotNull String name, @NotNull String value);
}
