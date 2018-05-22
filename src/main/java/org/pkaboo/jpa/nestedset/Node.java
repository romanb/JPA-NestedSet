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
    void moveAsLastChildOf(Node<T> dest);
    void moveAsFirstChildOf(Node<T> dest);
    void moveAsNextSiblingOf(Node<T> dest);
    void moveAsPrevSiblingOf(Node<T> dest);
    List<Node<T>> getChildren();
    List<Node<T>> getDescendants(int depth);
    List<Node<T>> getDescendants();
    List<Node<T>> getAncestors();
    Node<T> addChild(T child);
    Node<T> getParent();
    Node<T> getFirstChild();
    Node<T> getLastChild();
    T unwrap();
    void delete();
    boolean isRoot();
    void makeRoot(int newRootId);
    boolean hasParent();
    boolean hasChildren();
    boolean isValid();
    boolean isDescendantOf(Node<T> other);
}
