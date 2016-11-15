package com.tsolutions.xmlslurper;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.tsolutions.xmlslurper.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/15/2016.
 */
@SuppressWarnings("unused")
public class StAXNodeLite implements XMLNode {
    private static final int ATTRIBUTE_TABLE_EXTENSION_CAPACITY = 10;

    private final long id;

    private String name;
    private String text;
    private String[][] attributeByName;

    private int attributeByNameSize;

    StAXNodeLite(long id, String name, String[][] attributeByName) {
        this.id = id;
        this.name = requireNonNull(name);
        this.attributeByName = requireNonNull(attributeByName);
        this.attributeByNameSize = this.attributeByName.length;
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
        requireNonNull(attributeByName);

        this.attributeByName = new String[attributeByName.size()][];

        int index = 0;
        for (Map.Entry<String, String> entry : attributeByName.entrySet()) {
            String attrName = requireNonNull(entry.getKey());
            String attrValue = requireNonNull(entry.getValue());

            this.attributeByName[index++] = new String[] {attrName, attrValue};
        }

        attributeByNameSize = this.attributeByName.length;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> result = new HashMap<String, String>(attributeByName.length);

        for (String[] entry : attributeByName)
            result.put(entry[0], entry[1]);

        return result;
    }

    @Override
    public void setAttribute(@NotNull String name, @NotNull String value) {
        requireNonNull(name);
        requireNonNull(value);

        for(String[] entry : attributeByName)
            if (entry[0].equals(name)) {
                entry[1] = value;
                return;
            }

        if (attributeByName.length <= attributeByNameSize) {
            String[][] tmpAttributeByName = attributeByName;
            attributeByName = new String[tmpAttributeByName.length + ATTRIBUTE_TABLE_EXTENSION_CAPACITY][];

            int index = 0;
            for(String[] entry : tmpAttributeByName) {
                attributeByName[index++] = entry;
            }
        }

        attributeByName[attributeByNameSize++] = new String[] {name, value};
    }

    @Override
    public String getAttribute(@Nullable String name) {
        requireNonNull(name);

        for(String[] entry : attributeByName)
            if (entry[0].equals(name))
                return entry[1];

        return null;
    }

    @Override
    public boolean hasAttribute(@NotNull String name) {
        requireNonNull(name);

        for(String[] entry : attributeByName)
            if (entry[0].equals(name))
                return true;

        return false;
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
        return "StAXNodeLite{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", attributeByName=" + attributeByName +
                '}';
    }
}
