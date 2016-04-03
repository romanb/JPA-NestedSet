/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.pkaboo.jpa.nestedset;

import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * A <tt>NestedSetManager</tt> is used to read and manipulate the nested set
 * tree structure intrinsic to classes that implement {@link NodeInfo}.
 */
public interface NestedSetManager {
    /**
     * Clears the NestedSetManager, removing all managed nodes from the
     * <tt>NestedSetManager</tt>. Any entities wrapped by such nodes are
     * not detached from the underlying {@link EntityManager}.
     */
    // void clear();

    /**
     * Create a root node for the given NodeInfo instance.
     *
     * @param <T>
     * @param root
     * @return The created node instance.
     */
    <T extends NodeInfo> Node<T> createRoot(T root);

    /**
     * List all nodes of a tree, in ascending order of {@link NodeInfo#getLeftValue}.
     *
     * @param <T>
     * @param clazz
     * @return The tree in form of a list, starting with the root node.
     */
    <T extends NodeInfo> List<Node<T>> listNodes(Class<T> clazz);

    /**
     * List all nodes of a tree, in ascending order of {@link NodeInfo#getLeftValue}.
     *
     * @param <T>
     * @param clazz
     * @param rootId The tree ID.
     * @return The tree in form of a list, starting with the root node.
     */
    <T extends NodeInfo> List<Node<T>> listNodes(Class<T> clazz, int rootId);

    /**
     * Get the EntityManager used by this NestedSetManager.
     *
     * @return The EntityManager.
     */
    EntityManager getEntityManager();

    /**
     * Get the node that represents the given NodeInfo instance in the tree.
     *
     * @param <T>
     * @param nodeInfo
     * @return The node.
     */
    <T extends NodeInfo> Node<T> getNode(T nodeInfo);

    /**
     * Gets a collection of all nodes currently managed by the NestedSetManager.
     *
     * @return The collection of managed nodes.
     */
    // Collection<Node<?>> getManagedNodes();
}
