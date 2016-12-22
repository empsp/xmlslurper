package org.xs4j;

import org.xs4j.util.NotNull;
import org.xs4j.util.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.xs4j.util.NonNullValidator.requireNonNull;

/**
 * Created by mturski on 11/9/2016.
 */
public final class XMLNodeImpl implements XMLNode {
    public static final int DEFAULT_SIZE = 16;

    private final long id;
    private long position;
    private XMLNode parent;

    private String namespace;
    private String prefix;
    private String localName;

    private int charactersSize = 0;
    private char[] characters = new char[DEFAULT_SIZE];

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

    void setPosition(long position) {
        this.position = position;
    }

    @Override
    public long getPosition() {
        return position;
    }

    void setParent(XMLNode parent) {
        this.parent = parent;
    }

    @Override
    public XMLNode getParent() {
        return parent;
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

    public String getText() {
        if (charactersSize > 0)
            return new String(characters, 0, charactersSize);
        else
            return null;
    }

    public void setText(@Nullable String text) {
        this.characters = text.toCharArray();
        this.charactersSize = characters.length;
    }

    @Override
    public Map<String, String> getAttributes() {
        return new HashMap<String, String>(attributeByQName);
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
        if (!(o instanceof XMLNode)) return false;

        XMLNode that = (XMLNode) o;

        return id == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "XMLNodeImpl{" +
                "id=" + id +
                ", position=" + position +
                ", namespace='" + namespace + '\'' +
                ", prefix='" + prefix + '\'' +
                ", localName='" + localName + '\'' +
                ", text='" + getText() + '\'' +
                ", attributeByQName=" + attributeByQName +
                '}';
    }


    void appendText(char[] ch, int start, int length) {
        int lenAfterConcat = charactersSize + length;
        int extLength = characters.length << 1;

        if (lenAfterConcat > characters.length - 1)
            characters = Arrays.copyOf(characters, extLength < lenAfterConcat ? lenAfterConcat << 1 : extLength );

        System.arraycopy(ch, start, characters, charactersSize, length);
        charactersSize += length;
    }
}
