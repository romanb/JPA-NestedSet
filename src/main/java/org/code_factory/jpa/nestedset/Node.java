/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.util.List;

/**
 * A node in a nested set tree.
 *
 * @param <T extends NodeInfo> The wrapped entity type.
 * @author Roman Borschel <roman@code-factory.org>
 */
public interface Node<T extends NodeInfo> extends NodeInfo {
    /**
     * Moves this node in the tree, positioning it as the last child of
     * the given node.
     *
     * @param dest
     */
    void moveAsLastChildOf(Node<T> dest);
    /**
     * Moves this node in the tree, positioning it as the first child of
     * the given node.
     *
     * @param dest
     */
    void moveAsFirstChildOf(Node<T> dest);
    /**
     * Moves this node in the tree, positioning it as the successive sibling of
     * the given node.
     *
     * @param dest
     */
    void moveAsNextSiblingOf(Node<T> dest);
    /**
     * Moves this node as the previous sibling of the given node.
     *
     * @param dest
     */
    void moveAsPrevSiblingOf(Node<T> dest);
    /**
     * Gets the children children of the node (direct descendants).
     *
     * @return The children of the node.
     */
    List<Node<T>> getChildren();
    /**
     * Gets descendants of this node, up to a certain depth.
     *
     * @param depth
     * @return The descendants of the node, up to the specified depth.
     */
    List<Node<T>> getDescendants(int depth);
    /**
     * Gets the descendants of this node.
     *
     * @return The descendants of this node.
     */
    List<Node<T>> getDescendants();
    /**
     * Gets all ancestors of this node.
     *
     * @param int depth The depth "upstairs".
     * @return The ancestors of the node.
     */
    List<Node<T>> getAncestors();
    /**
     * Adds a node as the last child of this node.
     *
     * @param child The child to add.
     * @return The newly inserted child node.
     */
    Node<T> addChild(T child);
    /**
     * Gets the parent node of this node.
     *
     * @return The parent node or NULL if there is no parent node.
     */
    Node<T> getParent();
    /**
     * Gets the first child node of this node.
     *
     * @return The first child node of this node.
     */
    Node<T> getFirstChild();
    /**
     * Gets the last child node of this node.
     *
     * @return The last child node.
     */
    Node<T> getLastChild();
    /**
     * Unwraps the node, returning the wrapped object.
     *
     * @return The wrapped object.
     */
    T unwrap();
    /**
     * Deletes the node and all its descendants from the tree.
     *
     * @return void
     */
    void delete();
    /**
     * Tests if this node is a root node.
     *
     * @return TRUE if this node is a root node, FALSE otherwise.
     */
    boolean isRoot();
    /**
     * Tests if the node has a parent. If it does not have a parent, it is a root node.
     *
     * @return TRUE if this node has a parent node, FALSE otherwise.
     */
    boolean hasParent();
    /**
     * Tests if the node has any children.
     *
     * @return TRUE if the node has children, FALSE otherwise.
     */
    boolean hasChildren();
    /**
     * Tests whether the node is a valid node. A valid node is a node with a valid
     * position in the tree, represented by its left, right and level values.
     *
     * @return TRUE if the node is valid, FALSE otherwise.
     */
    boolean isValid();
    /**
     * Determines if this node is a descendant of the given node.
     *
     * @return TRUE if this node is a descendant of the given node, FALSE otherwise.
     */
    boolean isDescendantOf(Node<T> other);
}
