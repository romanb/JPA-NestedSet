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
 * @author Roman Borschel <roman@code-factory.org>
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
    T unwrap();
    void delete();
    boolean isRoot();
    boolean hasParent();
    boolean hasChildren();
    boolean isValid();
    boolean isDescendantOf(Node<T> other);
}
