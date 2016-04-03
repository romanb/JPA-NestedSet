/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.pkaboo.jpa.nestedset;

import java.util.List;

/**
 * A node in a nested set tree.
 *
 * @param <T extends NodeInfo> The wrapped entity type.
 */
public interface Node<T extends NodeInfo> extends NodeInfo {
    /**
     * Move this node in the tree, positioning it as the last child of
     * the given node.
     *
     * @param dest
     */
    void moveAsLastChildOf(Node<T> dest);
    /**
     * Move this node in the tree, positioning it as the first child of
     * the given node.
     *
     * @param dest
     */
    void moveAsFirstChildOf(Node<T> dest);
    /**
     * Move this node in the tree, positioning it as the successive sibling of
     * the given node.
     *
     * @param dest
     */
    void moveAsNextSiblingOf(Node<T> dest);
    /**
     * Move this node as the previous sibling of the given node.
     *
     * @param dest
     */
    void moveAsPrevSiblingOf(Node<T> dest);
    /**
     * Get the children children of the node (direct descendants).
     *
     * @return The children of the node.
     */
    List<Node<T>> getChildren();
    /**
     * Get descendants of this node, up to a certain depth.
     *
     * @param depth
     * @return The descendants of the node, up to the specified depth.
     */
    List<Node<T>> getDescendants(int depth);
    /**
     * Get the descendants of this node.
     *
     * @return The descendants of this node.
     */
    List<Node<T>> getDescendants();
    /**
     * Get all ancestors of this node.
     *
     * @return The ancestors of the node.
     */
    List<Node<T>> getAncestors();
    /**
     * Add a node as the last child of this node.
     *
     * @param child The child to add.
     * @return The newly inserted child node.
     */
    Node<T> addChild(T child);
    /**
     * Get the parent node of this node.
     *
     * @return The parent node or NULL if there is no parent node.
     */
    Node<T> getParent();
    /**
     * Get the first child node of this node.
     *
     * @return The first child node of this node.
     */
    Node<T> getFirstChild();
    /**
     * Get the last child node of this node.
     *
     * @return The last child node.
     */
    Node<T> getLastChild();
    /**
     * Unwrap the underlying entity.
     *
     * @return The wrapped entity.
     */
    T unwrap();
    /**
     * Delete the node (and thus the underlying entity) and all its
     * descendants from the tree.
     */
    void delete();
    /**
     * Test if this node is a root node.
     *
     * @return TRUE if this node is a root node, FALSE otherwise.
     */
    boolean isRoot();
    /**
     * Turn this node into a root node. Only used in multiple-root trees.
     *
     * @param newRootId
     */
    void makeRoot(int newRootId);
    /**
     * Test if the node has a parent. If it does not have a parent, it is a root node.
     *
     * @return TRUE if this node has a parent node, FALSE otherwise.
     */
    boolean hasParent();
    /**
     * Test if the node has any children.
     *
     * @return TRUE if the node has children, FALSE otherwise.
     */
    boolean hasChildren();
    /**
     * Test whether the node is a valid node. A valid node is a node with a valid
     * position in the tree, represented by its left, right and level values.
     *
     * @return TRUE if the node is valid, FALSE otherwise.
     */
    boolean isValid();
    /**
     * Determine if this node is a descendant of the given node.
     *
     * @return TRUE if this node is a descendant of the given node, FALSE otherwise.
     */
    boolean isDescendantOf(Node<T> other);
}
