package org.xs4j.path;

import org.xs4j.XMLSlurper;

import com.sun.istack.NotNull;

/**
 * A search node API that narrows the search to particular XPath/GPath similar pattern.
 *
 * @author <a href="mailto:turski.marek@gmail.com">Marek Turski</a>
 */
public interface SlurpNode extends Slurp {
    /**
     * Narrows the search further to a child node with given qualified name (local name with/without prefix). When used
     * with <code>*</code> wildcard characters, all children of the given nodes will be search for. With <code>**</code>
     * double wildcard characters, not only immediate children will be searched for but also their children
     * (descendants).
     *
     * @param qName of the child node to be searched for
     * @return a new instance of <code>SlurpNode</code> search API that may be used to further fine tune search pattern
     */
    @NotNull
    SlurpNode node(@NotNull String qName);

    /**
     * Limits the search to n-th current element specified by the <code>nodeIndex</code> parameter. When used
     * immediately after no parameter call of a method {@link XMLSlurper#getNodes}, then each n-th element at any depth
     * level will be search for. For example, searching for a 2nd element will detect the following nodes (bolded):
     * <br />
     * <br />
     * &lt;Foo&gt;<br />
     * &emsp;&lt;Bar /&gt;<br />
     * &emsp;<b>&lt;Bar&gt;</b><br />
     * &emsp;&emsp;&lt;Baz /&gt;<br />
     * &emsp;&emsp;<b>&lt;Baz /&gt;</b><br />
     * &emsp;<b>&lt;/Bar&gt;</b><br />
     * &emsp;&lt;Bar&gt;<br />
     * &emsp;&emsp;&lt;Baz&gt;<br />
     * &emsp;&lt;/Bar&gt;<br />
     * &lt;/Foo&gt;<br />
     * <br />
     * Please note, both start-tag and end-tag of the <code>Bar</code> node have been marked, and yet it is up to the
     * type of {@link Slurp#find}/{@link Slurp#findAll} methods being chosen. Both have the possibility to listen only
     * to the start-tag, end-tag or both events.
     *
     * @param nodeIndex specifying n-th current element to be search for
     * @return a new instance of <code>SlurpNode</code> search API that may be used to further fine tune search pattern
     */
    @NotNull
    SlurpNode get(long nodeIndex);

    /**
     * Limits the search to an existence of the attribute with given qualified name (local name with/without prefix) on
     * the current element.
     *
     * @param qName of the attribute on the current element to be search for
     * @return a new instance of <code>SlurpAttribute</code> search API that may be used to further fine tune attribute
     * search pattern
     */
    @NotNull
    SlurpAttribute attr(@NotNull String qName);
}
