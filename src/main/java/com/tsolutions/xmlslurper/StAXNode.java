package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Map;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/9/2016.
 */
public final class StAXNode implements XMLNode {
    private final long id;

    private String name;
    private String text;
    private Map<String, String> attributeByName;

    StAXNode(long id, String name, Map<String, String> attributeByName) {
        this.id = id;
        this.name = requireNonNull(name);
        this.attributeByName = requireNonNull(attributeByName);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setAttributes(@NotNull Map<String, String> attributeByName) {
        this.attributeByName = requireNonNull(attributeByName);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributeByName;
    }

    @Override
    public void setAttribute(@NotNull String name, @NotNull String value) {
        this.attributeByName.put(requireNonNull(name), requireNonNull(value));
    }

    @Override
    public String getAttribute(@Nullable String name) {
        return attributeByName.get(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof XMLNode))
            return false;

        return id == ((XMLNode)o).getId();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "StAXNode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", attributeByName=" + attributeByName +
                '}';
    }
}
