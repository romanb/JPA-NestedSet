/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * A <tt>NestedSetManager</tt> is used to read and manipulate the nested set tree structure of
 * classes that implement {@link NodeInfo} using and where each instance thus has a position in a
 * nested set tree.
 *
 * @author Roman Borschel <roman@code-factory.org>
 */
public interface NestedSetManager {
    /**
     * Clears the NestedSetManager, removing all managed nodes from the <tt>NestedSetManager</tt>.
     * Any entities wrapped by such nodes are not detached from the underlying <tt>EntityManager</tt>.
     *
     * @return void
     */
    void clear();

    /**
     * Creates a root node for the given NodeInfo instance.
     *
     * @param <T>
     * @param root
     * @return The created node instance.
     */
    <T extends NodeInfo> Node<T> createRoot(T root);

    /**
     * Fetches a complete tree, returning the root node of the tree.
     *
     * @param <T>
     * @param clazz
     * @param rootId
     * @return The root node of the tree.
     */
    <T extends NodeInfo> Node<T> fetchTree(Class<T> clazz, int rootId);

    /**
     * Fetches the complete tree, returning the root node of the tree.
     *
     * @param <T>
     * @param clazz
     * @param rootId
     * @return The root node of the tree.
     */
    <T extends NodeInfo> Node<T> fetchTree(Class<T> clazz);

    /**
     * Fetches a complete tree and returns the tree as a list.
     *
     * @param <T>
     * @param clazz
     * @return The tree in form of a list, starting with the root node.
     */
    <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz);

    /**
     * Fetches a complete tree and returns the tree as a list.
     *
     * @param <T>
     * @param clazz
     * @param rootId
     * @return The tree in form of a list, starting with the root node.
     */
    <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz, int rootId);

    /**
     * Gets the EntityManager used by this NestedSetManager.
     *
     * @return The EntityManager.
     */
    EntityManager getEntityManager();

    /**
     * Gets the node that represents the given NodeInfo instance in the tree.
     *
     * @param <T>
     * @param nodeInfo
     * @return The node.
     */
    <T extends NodeInfo> Node<T> getNode(T nodeInfo);

    /**
     * Gets a collection of all nodes currently managed by the NestedSetManager.
     *
     * @return The collection of nodes.
     */
    Collection<Node<?>> getNodes();
}
