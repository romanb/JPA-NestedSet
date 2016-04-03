/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.pkaboo.jpa.nestedset;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/** An in-memory tree view of a list of {@link Node}s. */
public class TreeView<T extends NodeInfo> {
    public final T node;
    private TreeView<T> parent;
    private final List<TreeView<T>> children;

    private TreeView(T node) {
        this.node = node;
        this.children = new ArrayList<>();
    }

    public TreeView<T> getParent() {
        return this.parent;
    }

    public List<TreeView<T>> getChildren() {
        return this.children;
    }

    public static <T extends NodeInfo> TreeView<T> build(List<T> nodes) {
        return build(nodes, -1);
    }

    // nodes must be non-empty
    // nodes must be sorted by {@link NodeInfo#getLeftValue}
    public static <T extends NodeInfo> TreeView<T> build(List<T> nodes, int maxLevel) {
        TreeView<T> root = new TreeView<>(nodes.get(0));
        Stack<TreeView<T>> ancestors = new Stack<>();
        ancestors.push(root);

        int level = root.node.getLevel();
        int total = nodes.size();
        for (int i = 1; i < total; ++i) {
            T node = nodes.get(i);
            TreeView<T> tree = new TreeView<>(node);
            while (ancestors.peek().node.getLevel() >= node.getLevel()) {
                ancestors.pop();
            }
            level = node.getLevel();

            TreeView<T> parent = ancestors.peek();
            tree.parent = parent;
            parent.children.add(tree);

            boolean hasChildren = node.getRightValue() - node.getLeftValue() > 1;
            if (hasChildren && (maxLevel == -1 || maxLevel > level)) {
                ancestors.push(tree);
            }
        }

        return root;
    }
}

