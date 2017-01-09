package org.xs4j;

import org.xs4j.util.ArraysUtil;
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
    private static final String TOO_LARGE_TEXT_LENGTH = "Requested array size exceeds VM limit";

    private static final int DEFAULT_SIZE = 16;

    private final long id;

    private long position;
    private int depth;
    private XMLNode parent;

    private String namespace;
    private String prefix;
    private String localName;

    private int lastAppendIndex = 0;
    private int lastAppendLength = 0;
    private char[] characters = new char[DEFAULT_SIZE];
    private int charactersSize = 0;

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
    public long getPosition() {
        return position;
    }

    @Override
    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public XMLNode getParent() {
        return parent;
    }

    @Override
    public void setParent(XMLNode parent) {
        this.parent = parent;
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
        lastAppendIndex = charactersSize;
        lastAppendLength = text.length();

        characters = text.toCharArray();
        charactersSize = characters.length;
    }

    @Override
    public String getAppendedText() {
        return new String(characters, lastAppendIndex, lastAppendLength);
    }

    @Override
    public void appendText(@Nullable String text) {
        appendText(text.toCharArray(), 0, text.length());
    }

    @Override
    public void appendText(char[] text, int startPosition, int length) {
        requireNonNull((Object)text);

        lastAppendIndex = charactersSize;
        lastAppendLength = length;

        int lenAfterConcat = charactersSize + length;
        if (lenAfterConcat < charactersSize)
            throw new java.lang.OutOfMemoryError(TOO_LARGE_TEXT_LENGTH);

        if (lenAfterConcat > characters.length) {
            int extLength = ArraysUtil.safelyDoubleLengthValue(characters.length);

            characters = Arrays.copyOf(
                    characters,
                    lenAfterConcat < extLength ? extLength : ArraysUtil.safelyDoubleLengthValue(lenAfterConcat));
        }

        System.arraycopy(text, startPosition, characters, charactersSize, length);
        charactersSize += length;
    }

    @Override
    public Map<String, String> getAttributes() {
        return new HashMap<String, String>(attributeByQName);
    }

    Map<String, String> getAttributeByQName() {
        return attributeByQName;
    }

    @Override
    public void setAttributes(@NotNull Map<String, String> attributeByQName) {
        requireNonNull((Object)attributeByQName);

        this.attributeByQName = new HashMap<String, String>(attributeByQName.size());
        for(String attrName : attributeByQName.keySet())
            this.attributeByQName.put(
                    requireNonNull(attrName),
                    requireNonNull(attributeByQName.get(attrName)));
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
                ", depth=" + depth +
                ", namespace='" + namespace + '\'' +
                ", prefix='" + prefix + '\'' +
                ", localName='" + localName + '\'' +
                ", text='" + getText() + '\'' +
                ", appendedText='" + getAppendedText() + '\'' +
                ", attributeByQName=" + attributeByQName +
                '}';
    }
}
