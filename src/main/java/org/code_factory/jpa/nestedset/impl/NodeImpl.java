/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import net.jcip.annotations.NotThreadSafe;
import org.code_factory.jpa.nestedset.Node;
import org.code_factory.jpa.nestedset.NodeInfo;

/**
 * A decorator for a NodeInfo implementation that enriches it with the full API
 * of a node in a nested set tree.
 *
 * @author Roman Borschel <roman@code-factory.org>
 */
@NotThreadSafe
class NodeImpl<T extends NodeInfo> implements Node<T> {
    private static final int PREV_SIBLING = 1;
    private static final int FIRST_CHILD = 2;
    private static final int NEXT_SIBLING = 3;
    private static final int LAST_CHILD = 4;

    /** The wrapped NodeInfo implementor. */
    private T node;
    /** The type of the wrapped instance. */
    private Class<T> type;
    private CriteriaQuery<T> baseQuery;
    private Root<T> queryRoot;

    /** The NestedSetManagerImpl that manages this node. */
    private NestedSetManagerImpl nsm;

    private List<Node<T>> children;
    private Node<T> parent;
    private List<Node<T>> ancestors;
    private List<Node<T>> descendants;
    private int descendantDepth = 0;

    @SuppressWarnings("unchecked")
    public NodeImpl(T node, NestedSetManagerImpl nsm) {
        this.node = node;
        this.nsm = nsm;
        this.type = (Class<T>) node.getClass();
    }

    @Override public int getId() {
        return this.node.getId();
    }

    @Override public int getLeftValue() {
        return this.node.getLeftValue();
    }

    @Override public int getRightValue() {
        return this.node.getRightValue();
    }

    @Override public int getLevel() {
        return this.node.getLevel();
    }

    @Override public int getRootValue() {
        return this.node.getRootValue();
    }

    @Override public void setRootValue(int value) {
        this.node.setRootValue(value);
    }

    @Override public void setLeftValue(int value) {
        this.node.setLeftValue(value);
    }

    @Override public void setRightValue(int value) {
        this.node.setRightValue(value);
    }

    @Override public void setLevel(int level) {
        this.node.setLevel(level);
    }

    @Override public String toString() {
        return "[Left: " + node.getLeftValue() +
                ", Right: " + node.getRightValue() +
                ", Level: " + node.getLevel() +
                ", NodeInfo: " + node.toString() +
                "]";
    }

    /**
     * Tests if the node has any children.
     *
     * @return TRUE if the node has children, FALSE otherwise.
     */
    @Override public boolean hasChildren() {
        return (getRightValue() - getLeftValue()) > 1;
    }

    /**
     * Tests if the node has a parent. If it does not have a parent, it is a root node.
     *
     * @return TRUE if this node has a parent node, FALSE otherwise.
     */
    @Override public boolean hasParent() {
        return !isRoot();
    }

    /**
     * Tests whether the node is a valid node. A valid node is a node with a valid
     * position in the tree, represented by its left, right and level values.
     *
     * @return TRUE if the node is valid, FALSE otherwise.
     */
    @Override public boolean isValid() {
        return isValidNode(this);
    }

    private boolean isValidNode(NodeInfo node) {
        return node != null && node.getRightValue() > node.getLeftValue();
    }

    /*
    public void setBaseQuery(CriteriaQuery<T> cq) {
        // The first root must be of the wrapped node type.
        this.queryRoot = (Root<T>) cq.getRoots().iterator().next();
        this.baseQuery = cq;
    }
    */

    /* public */ private CriteriaQuery<T> getBaseQuery() {
        if (this.baseQuery == null) {
            this.baseQuery = nsm.getEntityManager().getCriteriaBuilder().createQuery(type);
            this.queryRoot = this.baseQuery.from(type);
        }
        return this.baseQuery;
    }

    /**
     * Gets the number of children (direct descendants) of this node.
     *
     * @return The number of children of this node.
     */
    public int getNumberOfChildren() {
        return getChildren().size();
    }

    /**
     * Gets the number of descendants (children and their children etc.) of this node.
     *
     * @return The number of descendants of this node.
     */
    public int getNumberOfDescendants() {
        return (this.getRightValue() - this.getLeftValue() - 1) / 2;
    }

    /**
     * Determines if this node is equal to another node. 
     *
     * @return bool
     */
    /*public boolean isEqualTo(Node<T> node) {
        return ((this.getLeftValue() == node.getLeftValue()) &&
                (this.getRightValue() == node.getRightValue()) &&
                (this.getRootValue() == node.getRootValue()));
    }*/

    /**
     * Tests if this node is a root node.
     *
     * @return TRUE if this node is a root node, FALSE otherwise.
     */
    @Override public boolean isRoot() {
        return getLeftValue() == 1;
    }

    /**
     * Gets the children children of the node (direct descendants).
     *
     * @return The children of the node.
     * @todo Better return an unmodifiable list instead?
     */
    @Override public List<Node<T>> getChildren() {
        if (this.children != null) {
            return this.children;
        }
        return getDescendants(1);
    }

    /**
     * Gets the parent node of this node.
     *
     * @return The parent node or NULL if there is no parent node.
     */
    @Override public Node<T> getParent() {
        if (isRoot()) {
            return null;
        }
        if (this.parent != null) {
            return this.parent;
        }

        CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = getBaseQuery();
        cq.where(cb.lt(
                    queryRoot.get(nsm.getConfig(this.type).getLeftFieldName()).as(Number.class),
                    getLeftValue()
                    ),
                cb.gt(
                    queryRoot.get(nsm.getConfig(this.type).getRightFieldName()).as(Number.class),
                    getRightValue()
                    ));
        cq.orderBy(cb.asc(queryRoot.get(nsm.getConfig(this.type).getRightFieldName())));
        nsm.applyRootId(this.type, cq, getRootValue());

        List<T> result = nsm.getEntityManager().createQuery(cq).getResultList();

        this.parent = nsm.getNode(result.get(0));

        return this.parent;
    }

    /**
     * Gets the descendants of this node.
     *
     * @return The descendants of this node.
     */
    @Override public List<Node<T>> getDescendants() {
        return getDescendants(0);
    }
    
    /**
     * Gets descendants of this node, up to a certain depth.
     *
     * @return The descendants of the node, up to the specified depth.
     */
    @Override public List<Node<T>> getDescendants(int depth) {
        if (this.descendants != null && (depth == 0 && this.descendantDepth == 0 || depth <= this.descendantDepth)) {
            return this.descendants;
        }

        //TODO: Fill this.children here also?
        CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = getBaseQuery();
        Predicate wherePredicate = cb.and(
                cb.gt(
                    queryRoot.get(nsm.getConfig(this.type).getLeftFieldName()).as(Number.class),
                    getLeftValue()
                    ),
                cb.lt(
                    queryRoot.get(nsm.getConfig(this.type).getRightFieldName()).as(Number.class),
                    getRightValue()
                    ));

        if (depth > 0) {
            wherePredicate = cb.and(
                    wherePredicate,
                    cb.le(
                        queryRoot.get(nsm.getConfig(this.type).getLevelFieldName()).as(Number.class),
                        getLevel() + depth)
                        );
        }
        cq.where(wherePredicate);
        cq.orderBy(cb.asc(queryRoot.get(nsm.getConfig(this.type).getLeftFieldName())));

        nsm.applyRootId(this.type, cq, getRootValue());

        List<Node<T>> nodes = new ArrayList<Node<T>>();
        for (T n : nsm.getEntityManager().createQuery(cq).getResultList()) {
            nodes.add(nsm.getNode(n));
        }

        this.descendants = nodes;
        this.descendantDepth = depth;

        /*if (this.descendants.size() > 0) {
            this.nsm.buildTree(this.descendants, depth);
        }*/

        return this.descendants;
    }

    /**
     * Adds a node as the last child of this node.
     *
     * @param child The child to add.
     * @return The newly inserted child node.
     */
    @Override public Node<T> addChild(T child) {
        if (child == this.node) {
            throw new IllegalArgumentException("Cannot add node as child of itself.");
        }

        int newLeft = getRightValue();
        int newRight = getRightValue() + 1;
        int newRoot = getRootValue();

        shiftRLValues(newLeft, 0, 2, newRoot);
        child.setLevel(getLevel() + 1);
        child.setLeftValue(newLeft);
        child.setRightValue(newRight);
        child.setRootValue(newRoot);
        nsm.getEntityManager().persist(child);

        return this.nsm.getNode(child);
    }

    /**
     * Inserts this node as the previous sibling of the given node.
     *
     * @return void
     */
    private void insertAsPrevSiblingOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot add node as child of itself.");
        }

        int newLeft = dest.getLeftValue();
        int newRight = dest.getLeftValue() + 1;
        int newRoot = dest.getRootValue();

        shiftRLValues(newLeft, 0, 2, newRoot);
        setLevel(dest.getLevel());
        setLeftValue(newLeft);
        setRightValue(newRight);
        setRootValue(newRoot);
        nsm.getEntityManager().persist(this.node);
    }

    /**
     * Inserts this node as the next sibling of the given node.
     *
     * @return void
     */
    private void insertAsNextSiblingOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot add node as child of itself.");
        }

        int newLeft = dest.getRightValue() + 1;
        int newRight = dest.getRightValue() + 2;
        int newRoot = dest.getRootValue();

        shiftRLValues(newLeft, 0, 2, newRoot);
        setLevel(dest.getLevel());
        setLeftValue(newLeft);
        setRightValue(newRight);
        setRootValue(newRoot);
        nsm.getEntityManager().persist(this.node);
    }

    /**
     * Inserts this node as the last child of the given node.
     *
     * @return void
     */
    private void insertAsLastChildOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot add node as child of itself.");
        }

        int newLeft = dest.getRightValue();
        int newRight = dest.getRightValue() + 1;
        int newRoot = dest.getRootValue();

        shiftRLValues(newLeft, 0, 2, newRoot);
        setLevel(dest.getLevel() + 1);
        setLeftValue(newLeft);
        setRightValue(newRight);
        setRootValue(newRoot);
        nsm.getEntityManager().persist(this.node);
    }

    /**
     * Inserts this node as the first child of the given node.
     *
     * @return void
     */
    private void insertAsFirstChildOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot add node as child of itself.");
        }

        int newLeft = dest.getLeftValue() + 1;
        int newRight = dest.getLeftValue() + 2;
        int newRoot = dest.getRootValue();

        shiftRLValues(newLeft, 0, 2, newRoot);
        setLevel(dest.getLevel());
        setLeftValue(newLeft);
        setRightValue(newRight);
        setRootValue(newRoot);
        nsm.getEntityManager().persist(this.node);
    }

    /**
     * Deletes the node and all its descendants from the tree.
     *
     * @return void
     */
    @Override public void delete() {
        //TODO: Remove deleted nodes that are in-memory from NestedSetManagerImpl.
        int oldRoot = getRootValue();
        String rootIdFieldName = nsm.getConfig(this.type).getRootIdFieldName();
        String leftFieldName = nsm.getConfig(this.type).getLeftFieldName();
        String rightFieldName = nsm.getConfig(this.type).getRightFieldName();

        StringBuilder sb = new StringBuilder();
        sb.append("delete from " )
                .append(node.getClass().getSimpleName()).append(" n")
                .append(" where n.").append(leftFieldName).append(">= ?1")
                .append(" and n.").append(rightFieldName).append("<= ?2");

        if (rootIdFieldName != null) {
            sb.append(" and n.").append(rootIdFieldName).append("= ?3");
        }

        Query q = nsm.getEntityManager().createQuery(sb.toString());
        q.setParameter(1, this.getLeftValue());
        q.setParameter(2, this.getRightValue());
        if (rootIdFieldName != null) {
            q.setParameter(3, oldRoot);
        }
        q.executeUpdate();

        // Close gap in tree
        int first = getRightValue() + 1;
        int delta = getLeftValue() - getRightValue() - 1;
        shiftRLValues(first, 0, delta, oldRoot);
    }

    /**
     * Adds 'delta' to all Left and right values that are >= 'first' and
     * <= 'last'. 'delta' can also be negative. If 'last' is 0 it is skipped and there is
     * no upper bound.
     *
     * @param first The first left/right value (inclusive) of the nodes to shift.
     * @param last The last left/right value (inclusive) of the nodes to shift.
     * @param delta The offset by which to shift the left/right values (can be negative).
     * @param rootId The root/tree ID of the nodes to shift.
     */
    private void shiftRLValues(int first, int last, int delta, int rootId) {
        String rootIdFieldName = nsm.getConfig(this.type).getRootIdFieldName();
        String leftFieldName = nsm.getConfig(this.type).getLeftFieldName();
        String rightFieldName = nsm.getConfig(this.type).getRightFieldName();

        // Shift left values
        StringBuilder sbLeft = new StringBuilder();
        sbLeft.append("update ").append(node.getClass().getSimpleName()).append(" n")
                .append(" set n.").append(leftFieldName).append(" = n.").append(leftFieldName).append(" + ?1")
                .append(" where n.").append(leftFieldName).append(" >= ?2");

        if (last > 0) {
            sbLeft.append(" and n.").append(leftFieldName).append(" <= ?3");
        }

        if (rootIdFieldName != null) {
            sbLeft.append(" and n.").append(rootIdFieldName).append(" = ?4");
        }

        Query qLeft = nsm.getEntityManager().createQuery(sbLeft.toString());
        qLeft.setParameter(1, delta);
        qLeft.setParameter(2, first);
        if (last > 0) {
            qLeft.setParameter(3, last);
        }
        if (rootIdFieldName != null) {
            qLeft.setParameter(4, rootId);
        }
        qLeft.executeUpdate();
        this.nsm.updateLeftValues(first, last, delta, rootId);

        // Shift right values
        StringBuilder sbRight = new StringBuilder();
        sbRight.append("update ").append(node.getClass().getSimpleName()).append(" n")
                .append(" set n.").append(rightFieldName).append(" = n.").append(rightFieldName).append(" + ?1")
                .append(" where n.").append(rightFieldName).append(" >= ?2");

        if (last > 0) {
            sbRight.append(" and n.").append(rightFieldName).append(" <= ?3");
        }

        if (rootIdFieldName != null) {
            sbRight.append(" and n.").append(rootIdFieldName).append(" = ?4");
        }

        Query qRight = nsm.getEntityManager().createQuery(sbRight.toString());
        qRight.setParameter(1, delta);
        qRight.setParameter(2, first);
        if (last > 0) {
            qRight.setParameter(3, last);
        }
        if (rootIdFieldName != null) {
            qRight.setParameter(4, rootId);
        }
        qRight.executeUpdate();
        this.nsm.updateRightValues(first, last, delta, rootId);
    }

    @Override public T unwrap() {
        return this.node;
    }

    /**
     * Determines if the node is a leaf node.
     *
     * @return TRUE if the node is a leaf, FALSE otherwise.
     */
    public boolean isLeaf() {
        return (getRightValue() - getLeftValue()) == 1;
    }

    /**
     * Gets the first child node of this node.
     *
     * @return The first child node of this node.
     */
    @Override public Node<T> getFirstChild() {
        if (this.children != null) {
            return this.children.get(0);
        }

        CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = getBaseQuery();
        cq.where(cb.equal(queryRoot.get(nsm.getConfig(this.type).getLeftFieldName()), getLeftValue() + 1));

        nsm.applyRootId(this.type, cq, getRootValue());

        return nsm.getNode(nsm.getEntityManager().createQuery(cq).getSingleResult());
    }

    /**
     * Gets the last child node of this node.
     *
     * @return The last child node.
     */
    public Node<T> getLastChild() {
        if (this.children != null) {
            return this.children.get(this.children.size() - 1);
        }

        CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = getBaseQuery();
        cq.where(cb.equal(queryRoot.get(nsm.getConfig(this.type).getRightFieldName()), getRightValue() - 1));

        nsm.applyRootId(this.type, cq, getRootValue());

        return nsm.getNode(nsm.getEntityManager().createQuery(cq).getSingleResult());
    }
    
    /**
     * Gets all ancestors of this node.
     *
     * @param int depth The depth "upstairs".
     * @return The ancestors of the node or FALSE if the node has no ancestors (this
     *                basically means it's a root node).
     */
    @Override public List<Node<T>> getAncestors() {
        if (this.ancestors != null) {
            return this.ancestors;
        }
        
        CriteriaBuilder cb = nsm.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> cq = getBaseQuery();
        Predicate wherePredicate = cb.and(
                cb.lt(queryRoot.get(nsm.getConfig(this.type).getLeftFieldName()).as(Number.class), getLeftValue()),
                cb.gt(queryRoot.get(nsm.getConfig(this.type).getRightFieldName()).as(Number.class), getRightValue())
                );

        cq.where(wherePredicate);
        cq.orderBy(cb.asc(queryRoot.get(nsm.getConfig(this.type).getLeftFieldName())));

        nsm.applyRootId(this.type, cq, getRootValue());

        List<Node<T>> nodes = new ArrayList<Node<T>>();

        for (T n : nsm.getEntityManager().createQuery(cq).getResultList()) {
            nodes.add(nsm.getNode(n));
        }

        this.ancestors = nodes;

        return this.ancestors;
    }

    /**
     * Determines if this node is a descendant of the given node.
     *
     * @return boolean
     */
    @Override public boolean isDescendantOf(Node<T> subj) {
        return ((getLeftValue() > subj.getLeftValue()) &&
                (getRightValue() < subj.getRightValue()) &&
                (getRootValue() == subj.getRootValue()));
    }

    /**
     * gets path to node from root, uses record::toString() method to get node names
     *
     * @param string     $seperator     path seperator
     * @param bool     $includeNode     whether or not to include node at end of path
     * @return string     string representation of path
     */
    public String getPath(String seperator) {
        StringBuilder path = new StringBuilder();
        List<Node<T>> ancestors = getAncestors();
        for (Node<T> ancestor : ancestors) {
            path.append(ancestor.toString()).append(seperator);
        }

        return path.toString();
    }

    /**
     * Moves this node as the previous sibling of the given node.
     *
     * @param dest
     */
    @Override public void moveAsPrevSiblingOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot move node as previous sibling of itself");
        }

        if (dest.getRootValue() != getRootValue()) {
            moveBetweenTrees(dest, dest.getLeftValue(), 1);
        } else {
            // Move within the tree
            int oldLevel = getLevel();
            setLevel(dest.getLevel());
            updateNode(dest.getLeftValue(), getLevel() - oldLevel);
        }
    }

    /**
     * move node's and its children to location 'destLeft' and update rest of tree.
     *
     * @param int destLeft destination left value
     * @param levelDiff
     */
    private void updateNode(int destLeft, int levelDiff) {
        int left = getLeftValue();
        int right = getRightValue();
        int rootId = getRootValue();
        int treeSize = right - left + 1;

        // Make room in the new branch
        shiftRLValues(destLeft, 0, treeSize, rootId);

        if (left >= destLeft) { // src was shifted too?
            left += treeSize;
            right += treeSize;
        }

        String levelFieldName = nsm.getConfig(this.type).getLevelFieldName();
        String leftFieldName = nsm.getConfig(this.type).getLeftFieldName();
        String rightFieldName = nsm.getConfig(this.type).getRightFieldName();
        String rootIdFieldName = nsm.getConfig(this.type).getRootIdFieldName();

        // update level for descendants
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ").append(node.getClass().getSimpleName()).append(" n")
                .append(" set n.").append(levelFieldName).append(" = n.").append(levelFieldName).append(" + ?1")
                .append("where n.").append(leftFieldName).append(" > ?2")
                .append(" and n.").append(rightFieldName).append(" < ?3");

        if (rootIdFieldName != null) {
            updateQuery.append(" and n.").append(rootIdFieldName).append(" = ?4");
        }

        Query q = nsm.getEntityManager().createQuery(updateQuery.toString());
        q.setParameter(1, levelDiff);
        q.setParameter(2, left);
        q.setParameter(3, right);
        if (rootIdFieldName != null) {
            q.setParameter(4, rootId);
        }
        q.executeUpdate();
        this.nsm.updateLevels(left, right, levelDiff, rootId);

        // now there's enough room next to target to move the subtree
        shiftRLValues(left, right, destLeft - left, rootId);

        // correct values after source (close gap in old tree)
        shiftRLValues(right + 1, 0, -treeSize, rootId);
    }

    /**
     * Moves this node in the tree, positioning it as the successive sibling of
     * the given node.
     *
     * @param dest
     */
    @Override public void moveAsNextSiblingOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot move node as next sibling of itself");
        }
        if (dest.getRootValue() != getRootValue()) {
            moveBetweenTrees(dest, dest.getRightValue() + 1, 3);
        } else {
            // Move within tree
            int oldLevel = getLevel();
            setLevel(dest.getLevel());
            updateNode(dest.getRightValue() + 1, getLevel() - oldLevel);
        }
    }

    /**
     * Moves this node in the tree, positioning it as the first child of
     * the given node.
     *
     * @param dest
     */
    @Override public void moveAsFirstChildOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot move node as first child of itself");
        }

        if (dest.getRootValue() != getRootValue()) {
            moveBetweenTrees(dest, dest.getLeftValue() + 1, 2);
        } else {
            // Move within tree
            int oldLevel = getLevel();
            setLevel(dest.getLevel() + 1);
            updateNode(dest.getLeftValue() + 1, getLevel() - oldLevel);
        }
    }

    /**
     * Moves this node in the tree, positioning it as the last child of
     * the given node.
     *
     * @param dest
     */
    @Override public void moveAsLastChildOf(Node<T> dest) {
        if (dest == this.node) {
            throw new IllegalArgumentException("Cannot move node as first child of itself");
        }

        if (dest.getRootValue() != getRootValue()) {
            moveBetweenTrees(dest, dest.getLeftValue() + 1, 4);
        } else {
            // Move within tree
            int oldLevel = getLevel();
            setLevel(dest.getLevel() + 1);
            updateNode(dest.getRightValue(), getLevel() - oldLevel);
        }
    }


    /**
     * Accomplishes moving of nodes between different trees.
     * Used by the move* methods if the root values of the two nodes are different.
     *
     * @param dest
     * @param newLeftValue
     * @param moveType
     */
    private void moveBetweenTrees(Node<T> dest, int newLeftValue, int moveType) {
        String leftFieldName = nsm.getConfig(this.type).getLeftFieldName();
        String rightFieldName = nsm.getConfig(this.type).getRightFieldName();
        String levelFieldName = nsm.getConfig(this.type).getLevelFieldName();
        String rootIdFieldName = nsm.getConfig(this.type).getRootIdFieldName();

        // Move between trees: Detach from old tree & insert into new tree
        int newRoot = dest.getRootValue();
        int oldRoot = getRootValue();
        int oldLft = getLeftValue();
        int oldRgt = getRightValue();
        int oldLevel = getLevel();

        // Prepare target tree for insertion, make room
        shiftRLValues(newLeftValue, 0, oldRgt - oldLft - 1, newRoot);

        // Set new root id for this node
        setRootValue(newRoot);
        //$this ->  _node ->  save();
        // Insert this node as a new node
        setRightValue(0);
        setLeftValue(0);

        switch (moveType) {
            case PREV_SIBLING:
                insertAsPrevSiblingOf(dest);
                break;
            case FIRST_CHILD:
                insertAsFirstChildOf(dest);
                break;
            case NEXT_SIBLING:
                insertAsNextSiblingOf(dest);
                break;
            case LAST_CHILD:
                insertAsLastChildOf(dest);
                break;
            default:
                throw new IllegalArgumentException("Unknown move operation: " + moveType);
        }

        //int diff = oldRgt - oldLft;
        setRightValue(getLeftValue() + (oldRgt - oldLft));
        //$this ->  _node ->  save();

        int newLevel = getLevel();
        int levelDiff = newLevel - oldLevel;

        // Relocate descendants of the node
        int diff = getLeftValue() - oldLft;

        // Update lft/rgt/root/level for all descendants
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ").append(node.getClass().getSimpleName()).append(" n")
                .append(" set n.").append(leftFieldName).append(" = n.").append(leftFieldName).append(" + ?1")
                .append(", n.").append(rightFieldName).append(" = n.").append(rightFieldName).append(" + ?2")
                .append(", n.").append(levelFieldName).append(" = n.").append(levelFieldName).append(" + ?3")
                .append(", n.").append(rootIdFieldName).append(" = ?4")
                .append(" where n.").append(leftFieldName).append(" > ?5")
                .append(" and n.").append(rightFieldName).append(" < ?6")
                .append(" and n.").append(rootIdFieldName).append(" = ?7");

        Query q = nsm.getEntityManager().createQuery(updateQuery.toString());
        q.setParameter(1, diff);
        q.setParameter(2, diff);
        q.setParameter(3, levelDiff);
        q.setParameter(4, newRoot);
        q.setParameter(5, oldLft);
        q.setParameter(6, oldRgt);
        q.setParameter(7, oldRoot);

        q.executeUpdate();

        // Close gap in old tree
        int first = oldRgt + 1;
        int delta = oldLft - oldRgt - 1;
        shiftRLValues(first, 0, delta, oldRoot);
    }

    /**
     * Makes this node a root node. Only used in multiple-root trees.
     * 
     * @param newRootId
     */
    public void makeRoot(int newRootId) {
        if (isRoot()) {
            return;
        }

        String leftFieldName = nsm.getConfig(this.type).getLeftFieldName();
        String rightFieldName = nsm.getConfig(this.type).getRightFieldName();
        String levelFieldName = nsm.getConfig(this.type).getLevelFieldName();
        String rootIdFieldName = nsm.getConfig(this.type).getRootIdFieldName();

        int oldRgt = getRightValue();
        int oldLft = getLeftValue();
        int oldRoot = getRootValue();
        int oldLevel = getLevel();

        // Update descendants lft/rgt/root/level values
        int diff = 1 - oldLft;
        int newRoot = newRootId;

        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("update ").append(node.getClass().getSimpleName()).append(" n")
                .append(" set n.").append(leftFieldName).append(" = n.").append(leftFieldName).append(" + ?1")
                .append(", n.").append(rightFieldName).append(" = n.").append(rightFieldName).append(" + ?2")
                .append(", n.").append(levelFieldName).append(" = n.").append(levelFieldName).append(" - ?3")
                .append(", n.").append(rootIdFieldName).append(" = ?4")
                .append("where n.").append(leftFieldName).append(" > ?5")
                .append(" and n.").append(rightFieldName).append(" < ?6")
                .append(" and n.").append(rootIdFieldName).append(" = ?7");

        Query q = nsm.getEntityManager().createQuery(updateQuery.toString());
        q.setParameter(1, diff);
        q.setParameter(2, diff);
        q.setParameter(3, oldLevel);
        q.setParameter(4, newRoot);
        q.setParameter(5, oldLft);
        q.setParameter(6, oldRgt);
        q.setParameter(7, oldRoot);

        q.executeUpdate();

        // Detach from old tree (close gap in old tree)
        int first = oldRgt + 1;
        int delta = oldLft - oldRgt - 1;
        shiftRLValues(first, 0, delta, getRootValue());

        // Set new lft/rgt/root/level values for root node
        setLeftValue(1);
        setRightValue(oldRgt - oldLft + 1);
        setRootValue(newRootId);
        setLevel(0);
    }

    void invalidate() {
        // Clear all local caches of other nodes, so that they're re-evaluated.
        this.children = null;
        this.parent = null;
        this.ancestors = null;
        this.descendants = null;
        this.descendantDepth = 0;
    }

    void internalAddChild(Node<T> child) {
        if (this.children == null) {
            this.children = new ArrayList<Node<T>>();
        }
        this.children.add(child);
    }

    void internalSetParent(Node<T> parent) {
        this.parent = parent;
    }

    void internalAddDescendant(Node<T> descendant) {
        if (this.descendants == null) {
            this.descendants = new ArrayList<Node<T>>();
        }
        this.descendants.add(descendant);
    }

    void internalSetAncestors(List<Node<T>> ancestors) {
        this.ancestors = ancestors;
    }
}
