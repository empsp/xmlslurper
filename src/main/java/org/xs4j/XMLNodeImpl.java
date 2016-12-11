package org.xs4j;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NotNullValidator.requireNonNull;

/**
 * Created by mturski on 11/9/2016.
 */
public final class XMLNodeImpl implements XMLNode {
    private final long id;

    private String namespace;
    private String prefix;
    private String localName;

    private String text;

    private Map<String, String> attributeByQName;

    XMLNodeImpl(long id, String namespace, String prefix, String localName, Map<String, String> attributeByQName) {
        this.id = id;
        this.localName = requireNonNull(localName);
        this.attributeByQName = requireNonNull(attributeByQName);
        this.namespace = namespace;
        this.prefix = prefix;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(@Nullable String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public void setLocalName(@NotNull String name) {
        this.localName = requireNonNull(name);
    }

    @Override
    public String getQName() {
        return prefix != null ? prefix + XMLNodeFactory.QNAME_SEPARATOR + localName : localName;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(@Nullable String text) {
        this.text = text;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributeByQName;
    }

    @Override
    public void setAttributes(@NotNull Map<String, String> attributeByQName) {
        this.attributeByQName = new HashMap<String, String>();

        for(Map.Entry<String, String> entry : attributeByQName.entrySet()) {
            String attrName = requireNonNull(entry.getKey());
            String attrValue = requireNonNull(entry.getValue());

            this.attributeByQName.put(attrName, attrValue);
        }
    }

    @Override
    public boolean hasAttribute(@NotNull String qName) {
        requireNonNull(qName);

        return attributeByQName.containsKey(qName);
    }

    @Override
    public void setAttribute(@NotNull String qName, @NotNull String value) {
        this.attributeByQName.put(requireNonNull(qName), requireNonNull(value));
    }

    @Override
    public String getAttribute(@Nullable String qName) {
        return attributeByQName.get(qName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XMLNodeImpl xmlNode = (XMLNodeImpl) o;

        return id == xmlNode.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "XMLNodeImpl{" +
                "id=" + id +
                ", namespace='" + namespace + '\'' +
                ", prefix='" + prefix + '\'' +
                ", localName='" + localName + '\'' +
                ", text='" + text + '\'' +
                ", attributeByQName=" + attributeByQName +
                '}';
    }
}
