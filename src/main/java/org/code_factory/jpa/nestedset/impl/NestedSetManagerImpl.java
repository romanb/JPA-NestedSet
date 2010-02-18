/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import net.jcip.annotations.NotThreadSafe;
import org.code_factory.jpa.nestedset.NestedSetManager;
import org.code_factory.jpa.nestedset.Node;
import org.code_factory.jpa.nestedset.NodeInfo;
import org.code_factory.jpa.nestedset.annotations.LeftColumn;
import org.code_factory.jpa.nestedset.annotations.LevelColumn;
import org.code_factory.jpa.nestedset.annotations.RightColumn;
import org.code_factory.jpa.nestedset.annotations.RootColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A NestedSetManagerImpl is used to read and manipulate the nested set tree structure of
 * a class that implements NodeInfo and where each instance thus has a position in a
 * nested set tree.
 *
 * @author Roman Borschel <roman@code-factory.org>
 */
@NotThreadSafe
public class NestedSetManagerImpl implements NestedSetManager {
    private static Logger log = LoggerFactory.getLogger(NestedSetManagerImpl.class.getName());
    private EntityManager em;
    private Map<Key, Node<?>> nodes;
    private Map<Class<?>, Configuration> configs;

    @Inject
    public NestedSetManagerImpl(EntityManager em) {
        this.em = em;
        this.nodes = new HashMap<Key, Node<?>>();
        this.configs = new HashMap<Class<?>, Configuration>();
    }

    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    @Override
    public void clear() {
        this.nodes.clear();
    }

    /**
     * Gets all currently managed nodes.
     *
     * @return A read-only collection of the currently managed nodes.
     */
    @Override
    public Collection<Node<?>> getNodes() {
        return Collections.unmodifiableCollection(this.nodes.values());
    }

    @Override
    public <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz) {
        return fetchTreeAsList(clazz, 0);
    }

    @Override
    public <T extends NodeInfo> List<Node<T>> fetchTreeAsList(Class<T> clazz, int rootId) {
        Configuration config = getConfig(clazz);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> queryRoot = cq.from(clazz);
        cq.where(cb.ge(queryRoot.get(config.getLeftFieldName()).as(Number.class), 1));
        cq.orderBy(cb.asc(queryRoot.get(config.getLeftFieldName())));
        applyRootId(clazz, cq, rootId);

        List<Node<T>> tree = new ArrayList<Node<T>>();
        for (T n : em.createQuery(cq).getResultList()) {
            tree.add(getNode(n));
        }

        buildTree(tree, 0);

        return tree;
    }

    @Override
    public <T extends NodeInfo> Node<T> fetchTree(Class<T> clazz, int rootId) {
        return fetchTreeAsList(clazz, rootId).get(0);
    }

    /**
     * Establishes all parent/child/ancestor/descendant relationships of all the nodes in
     * the given list. As a result, invocations on the corresponding methods on these node
     * instances will not trigger any database queries.
     *
     * @param <T>
     * @param treeList
     * @param maxLevel
     * @return void
     */
    private <T extends NodeInfo> void buildTree(List<Node<T>> treeList, int maxLevel) {
        Node<T> rootNode = treeList.get(0);

        Stack<NodeImpl<T>> stack = new Stack<NodeImpl<T>>();
        int level = rootNode.getLevel();

        for (Node<T> n : treeList) {
            NodeImpl<T> node = (NodeImpl<T>) n;

            if (node.getLevel() < level) {
                stack.pop();
            }
            level = node.getLevel();

            if (node != rootNode) {
                NodeImpl<T> parent = stack.peek();
                // set parent
                node.internalSetParent(parent);
                // add child to parent
                parent.internalAddChild(node);
                // set ancestors
                node.internalSetAncestors(new ArrayList<Node<T>>(stack));
                // add descendant to all ancestors
                for (NodeImpl<T> anc : stack) {
                    anc.internalAddDescendant(node);
                }
            }

            if (node.hasChildren() && (maxLevel == 0 || maxLevel > level)) {
                stack.push(node);
            }

        }
    }

    /**
     * Creates a root node for the given NodeInfo instance.
     *
     * @param <T>
     * @param root
     * @return The created node instance.
     */
    @Override
    public <T extends NodeInfo> Node<T> createRoot(T root) {
        root.setLeftValue(1);
        root.setRightValue(2);
        root.setLevel(0);
        em.persist(root);
        return getNode(root);
    }

    /**
     * Gets the node that represents the given NodeInfo instance in the tree.
     *
     * @param <T>
     * @param nodeInfo
     * @return The node.
     */
    @Override
    public <T extends NodeInfo> Node<T> getNode(T nodeInfo) {
        Key key = new Key(nodeInfo.getClass(), nodeInfo.getId());
        if (this.nodes.containsKey(key)) {
            return (Node<T>) this.nodes.get(key);
        }
        Node<T> node = new NodeImpl<T>(nodeInfo, this);
        if (!node.isValid()) {
            throw new IllegalArgumentException("The given NodeInfo instance has no position " +
                    "in a tree and is thus not yet a node.");
        }
        this.nodes.put(key, node);

        return node;
    }

    Configuration getConfig(Class<?> clazz) {
        if (!this.configs.containsKey(clazz)) {
            Configuration config = new Configuration();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getAnnotation(LeftColumn.class) != null) {
                    config.setLeftFieldName(field.getName());
                }
                else if (field.getAnnotation(RightColumn.class) != null) {
                    config.setRightFieldName(field.getName());
                }
                else if (field.getAnnotation(LevelColumn.class) != null) {
                    config.setLevelFieldName(field.getName());
                }
                else if (field.getAnnotation(RootColumn.class) != null) {
                    config.setRootIdFieldName(field.getName());
                }
            }

            this.configs.put(clazz, config);
        }

        return this.configs.get(clazz);
    }

    /**
     * INTERNAL:
     * Applies the root ID criteria to the given CriteriaQuery.
     *
     * @param cq
     * @param rootId
     */
    void applyRootId(Class<?> clazz, CriteriaQuery<?> cq, int rootId) {
        Configuration config = getConfig(clazz);
        if (config.getRootIdFieldName() != null) {
            Root<?> root = cq.getRoots().iterator().next();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            Predicate p = cq.getRestriction();
            cq.where(cb.and(p, cb.equal(root.get(config.getRootIdFieldName()), rootId)));
        }
    }

    /**
     * INTERNAL:
     * Updates the left values of all nodes currently known to the manager.
     *
     * @param minLeft The lower bound (inclusive) of the left values to update.
     * @param maxLeft The upper bound (inclusive) of the left values to update.
     * @param delta The delta to apply on the left values within the range.
     */
    void updateLeftValues(int minLeft, int maxLeft, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getLeftValue() >= minLeft && (maxLeft == 0 || node.getLeftValue() <= maxLeft)) {
                    node.setLeftValue(node.getLeftValue() + delta);
                    ((NodeImpl<?>) node).invalidate();
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Updates the right values of all nodes currently known to the manager.
     *
     * @param minRight The lower bound (inclusive) of the right values to update.
     * @param maxRight The upper bound (inclusive) of the right values to update.
     * @param delta The delta to apply on the right values within the range.
     */
    void updateRightValues(int minRight, int maxRight, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getRightValue() >= minRight && (maxRight == 0 || node.getRightValue() <= maxRight)) {
                    node.setRightValue(node.getRightValue() + delta);
                    ((NodeImpl<?>) node).invalidate();
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Updates the level values of all nodes currently known to the manager.
     *
     * @param left The lower bound left value.
     * @param right The upper bound right value.
     * @param delta The delta to apply on the level values of the nodes within the range.
     */
    void updateLevels(int left, int right, int delta, int rootId) {
        for (Node<?> node : this.nodes.values()) {
            if (node.getRootValue() == rootId) {
                if (node.getLeftValue() > left && node.getRightValue() < right) {
                    node.setLevel(node.getLevel() + delta);
                    ((NodeImpl<?>) node).invalidate();
                }
            }
        }
    }
}
