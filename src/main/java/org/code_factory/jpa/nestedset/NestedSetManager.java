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
 * Contract for a nestedset manager.
 *
 * @author Roman Borschel <roman@code-factory.org>
 */
public interface NestedSetManager {

    void clear();

    /**
     * Creates a root node for the given NodeInfo instance.
     *
     * @param <T>
     * @param root
     * @return The created node instance.
     */
    <T extends NodeInfo> Node<T> createRoot(T root);

    <T extends NodeInfo> Node<T> fetchTree(Class<T> clazz, int rootId);

    <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz);

    <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz, int rootId);

    EntityManager getEntityManager();

    /**
     * Gets the node that represents the given NodeInfo instance in the tree.
     *
     * @param <T>
     * @param nodeInfo
     * @return The node.
     */
    <T extends NodeInfo> Node<T> getNode(T nodeInfo);

    Collection<Node<?>> getNodes();

}
