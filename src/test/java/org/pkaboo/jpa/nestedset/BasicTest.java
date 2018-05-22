/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.pkaboo.jpa.nestedset;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.pkaboo.jpa.nestedset.model.Category;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class BasicTest extends FunctionalNestedSetTest {
    private Category progCat;
    private Category javaCat;
    private Category netCat;

    @AfterMethod
    @Override protected void closeEntityManager() {
        super.closeEntityManager();
        this.progCat = null;
        this.javaCat = null;
        this.netCat = null;
    }

    /**
     * Helper method that creates a basic tree that looks as follows:
     *
     *           Programming
     *            /       \
     *         Java       .NET
     *
     */
    private void createBasicTree() {
        this.progCat = new Category();
        this.progCat.setName("Programming");

        this.javaCat = new Category();
        this.javaCat.setName("Java");

        this.netCat = new Category();
        this.netCat.setName(".NET");

        em.getTransaction().begin();
        Node<Category> root = nsm.createRoot(this.progCat);
        root.addChild(this.javaCat);
        root.addChild(this.netCat);
        em.getTransaction().commit();
        nsm.clear();
        em.clear();
    }

    @Test
    public void testCreateRoot() {
        Category cat = new Category();
        cat.setName("Java");

        em.getTransaction().begin();
        nsm.createRoot(cat);
        em.getTransaction().commit();
        em.clear();

        Category cat2 = em.find(Category.class, cat.getId());
        assert 1 == cat2.getLeftValue();
        assert 2 == cat2.getRightValue();
        assert 0 == cat2.getLevel();
        assert cat != cat2;
        assert true == nsm.getNode(cat2).isRoot();
    }

    @Test
    public void testlistNodes() {
        this.createBasicTree();

        List<Node<Category>> nodes = nsm.listNodes(Category.class);
        assert nodes.size() == 3;
        Iterator<Node<Category>> iter = nodes.iterator();
        for (int i = 0; i < 3; i++) {
            Node node = iter.next();
            if (i == 0) {
                assert 1 == node.getLeftValue();
                assert 6 == node.getRightValue();
                assert 0 == node.getLevel();
            } else if (i == 1) {
                assert 2 == node.getLeftValue();
                assert 3 == node.getRightValue();
                assert 1 == node.getLevel();
            } else {
                assert 4 == node.getLeftValue();
                assert 5 == node.getRightValue();
                assert 1 == node.getLevel();
            }
        }
    }

    @Test
    public void testBasicTreeNavigation() {
        this.createBasicTree();

        Category progCat2 = em.find(Category.class, this.progCat.getId());

        Node<Category> progCatNode = nsm.getNode(progCat2);

        assert 1 == progCatNode.getLeftValue();
        assert 6 == progCatNode.getRightValue();
        assert 0 == progCatNode.getLevel();
        assert null == progCatNode.getParent();

        List<Node<Category>> children = progCatNode.getChildren();
        assert 2 == children.size();
        Iterator<Node<Category>> childIter = children.iterator();
        Node child1 = childIter.next();
        Node child2 = childIter.next();
        assert 2 == child1.getLeftValue();
        assert 3 == child1.getRightValue();
        assert false == child1.hasChildren();
        assert false == child2.hasChildren();
        assert 0 == child1.getChildren().size();
        assert 0 == child2.getChildren().size();

        assert progCat2 == child1.getParent().unwrap();
        assert progCat2 == child2.getParent().unwrap();
    }

    @Test
    public void testAddingNodesToTree() {
        this.createBasicTree();

        assert 0 == this.nsm.getManagedNodes().size();
        Node<Category> root = this.nsm.getNode(em.find(Category.class, this.progCat.getId()));

        // Assert basic tree state, a Programming category with 2 child categories.
        assert 1 == root.getLeftValue();
        assert 6 == root.getRightValue();
        assert 2 == root.getChildren().size();
        assert 2 == root.getChildren().size();

        assert 3 == this.nsm.getManagedNodes().size();

        // Add PHP category under Programming
        em.getTransaction().begin();
        Category phpCat = new Category();
        phpCat.setName("PHP");
        root.addChild(phpCat);
        em.getTransaction().commit();

        assert 6 == phpCat.getLeftValue();
        assert 7 == phpCat.getRightValue();
        assert 8 == root.getRightValue();
        assert 3 == root.getChildren().size();

        // Add Java EE category under Java
        em.getTransaction().begin();
        Category jeeCat = new Category();
        jeeCat.setName("Java EE");
        Node<Category> javaNode = this.nsm.getNode(em.find(Category.class, this.javaCat.getId()));
        javaNode.addChild(jeeCat);
        em.getTransaction().commit();

        assert 3 == root.getChildren().size();
        assert 3 == jeeCat.getLeftValue();
        assert 4 == jeeCat.getRightValue();
        assert 2 == jeeCat.getLevel();
        assert 2 == javaNode.getLeftValue();
        assert 5 == javaNode.getRightValue();
        assert 10 == root.getRightValue();

        assert 5 == this.nsm.getManagedNodes().size();
    }

    @Test
    public void testMovingNodes() {
        this.createBasicTree();

        em.getTransaction().begin();

        Node<Category> progNode = this.nsm.getNode(em.find(Category.class, this.progCat.getId()));

        // Create a new WPF category, placing it under "Programming" first.
        /*
                 Programming
                  /   |   \
               Java  .NET  WPF
        */
        Category wpfCat = new Category();
        wpfCat.setName("WPF");
        Node<Category> wpfNode = progNode.addChild(wpfCat);
        assert 6 == wpfNode.getLeftValue();
        assert 7 == wpfNode.getRightValue();
        assert 1 == wpfNode.getLevel();
        assert 8 == progNode.getRightValue();

        // Now move it under the .NET category where it really belongs
        /*
                 Programming
                   /   \
                Java    .NET
                          |
                         WPF
        */
        Node<Category> netNode = this.nsm.getNode(em.find(Category.class, this.netCat.getId()));
        wpfNode.moveAsFirstChildOf(netNode);
        assert 4 == netNode.getLeftValue();
        assert 7 == netNode.getRightValue();
        assert 5 == wpfNode.getLeftValue();
        assert 6 == wpfNode.getRightValue();
        assert 2 == wpfNode.getLevel();
        assert 8 == progNode.getRightValue();

        // Create another category "EJB" under "Programming" that doesnt really belong there
        /*
                 Programming
                  /   |   \
               Java  .NET  EJB
                      |
                      WPF
        */
        Category ejbCat = new Category();
        ejbCat.setName("EJB");
        Node<Category> ejbNode = progNode.addChild(ejbCat);
        assert 8 == ejbNode.getLeftValue();
        assert 9 == ejbNode.getRightValue();
        assert 10 == progNode.getRightValue();

        // Move it under "Java" where it belongs
        /*
                 Programming
                   /   \
                Java    .NET
                  |       |
                 EJB     WPF
        */
        Node<Category> javaNode = this.nsm.getNode(em.find(Category.class, this.javaCat.getId()));
        ejbNode.moveAsLastChildOf(javaNode);
        assert 3 == ejbNode.getLeftValue();
        assert 4 == ejbNode.getRightValue();
        assert 2 == ejbNode.getLevel();
        assert 5 == javaNode.getRightValue();
        assert 6 == netNode.getLeftValue();
        assert 9 == netNode.getRightValue();
        assert 10 == progNode.getRightValue();

        em.getTransaction().commit();

        em.refresh(wpfNode.unwrap());
        assert 2 == wpfNode.getLevel();
    }

    @Test
    public void testDeleteNode() {
        this.createBasicTree();

        em.getTransaction().begin();
        // fetch the tree
        Node<Category> progNode = nsm.listNodes(Category.class, this.progCat.getRootValue()).get(0);
        assertEquals(progNode.getChildren().size(), 2);
        assert 1 == progNode.getLeftValue();
        assert 6 == progNode.getRightValue();

        // delete the .NET node
        Category netCat2 = em.find(Category.class, this.netCat.getId());
        Node<Category> netNode = nsm.getNode(netCat2);
        netNode.delete();

        // check in-memory state of tree
        assert 1 == progNode.getLeftValue();
        assert 4 == progNode.getRightValue();
        assertFalse(em.contains(netCat2));
        assertTrue(em.contains(progNode.unwrap()));
        assertEquals(progNode.getChildren().size(), 1);
        try {
            nsm.getNode(netCat2);
            fail("Retrieving node for deleted category should fail.");
        } catch (IllegalArgumentException expected) {}

        em.getTransaction().commit();
    }

    @Test
    public void testTreeView() {
        // Create tree
        /*
                   A
                  / \
                 B   C
                /  / | \
               D  E  F  G
               |     |
               H     I
                    / \
                   J   K
        */

        Category catA = new Category();
        catA.setName("A");

        Category catB = new Category();
        catB.setName("B");

        Category catC = new Category();
        catC.setName("C");

        Category catD = new Category();
        catD.setName("D");

        Category catE = new Category();
        catE.setName("E");

        Category catF = new Category();
        catF.setName("F");

        Category catG = new Category();
        catG.setName("G");

        Category catH = new Category();
        catH.setName("H");

        Category catI = new Category();
        catI.setName("I");

        Category catJ = new Category();
        catJ.setName("J");

        Category catK = new Category();
        catK.setName("K");

        em.getTransaction().begin();
        Node<Category> root = nsm.createRoot(catA);
        // Level 1
        root.addChild(catB);
        root.addChild(catC);
        // Level 2
        nsm.getNode(catB).addChild(catD);
        nsm.getNode(catC).addChild(catE);
        nsm.getNode(catC).addChild(catF);
        nsm.getNode(catC).addChild(catG);
        // Level 3
        nsm.getNode(catD).addChild(catH);
        nsm.getNode(catF).addChild(catI);
        // Level 4
        nsm.getNode(catI).addChild(catJ);
        nsm.getNode(catI).addChild(catK);

        em.getTransaction().commit();

        // Test tree
        em.getTransaction().begin();
        List<Category> nodes = nsm.listNodes(Category.class, 0).stream()
                .map(n -> n.unwrap())
                .collect(Collectors.toList());
        em.getTransaction().commit();
        // No database operations from here on.
        closeEntityManager();

        TreeView<Category> rootView = TreeView.build(nodes);

        // Check root
        assert catA == rootView.node;

        // Check level 1
        List<TreeView<Category>> childrenA = rootView.getChildren();
        assert 2 == childrenA.size();
        TreeView<Category> viewB = childrenA.get(0);
        TreeView<Category> viewC = childrenA.get(1);
        assert catB == viewB.node;
        assert catC == viewC.node;
        assert rootView == viewB.getParent();
        assert rootView == viewC.getParent();

        // Check level 2
        List<TreeView<Category>> childrenB = viewB.getChildren();
        List<TreeView<Category>> childrenC = viewC.getChildren();
        assert 1 == childrenB.size();
        assert 3 == childrenC.size();
        TreeView<Category> viewD = childrenB.get(0);
        TreeView<Category> viewE = childrenC.get(0);
        TreeView<Category> viewF = childrenC.get(1);
        TreeView<Category> viewG = childrenC.get(2);
        assert catD == viewD.node;
        assert catE == viewE.node;
        assert catF == viewF.node;
        assert catG == viewG.node;

        // Check level 3
        List<TreeView<Category>> childrenD = viewD.getChildren();
        List<TreeView<Category>> childrenE = viewE.getChildren();
        List<TreeView<Category>> childrenF = viewF.getChildren();
        List<TreeView<Category>> childrenG = viewG.getChildren();
        assert 1 == childrenD.size();
        assert 0 == childrenE.size();
        assert 1 == childrenF.size();
        assert 0 == childrenG.size();
        TreeView<Category> viewH = childrenD.get(0);
        TreeView<Category> viewI = childrenF.get(0);
        assert catH == viewH.node;
        assert catI == viewI.node;

        // Check level 4
        List<TreeView<Category>> childrenH = viewH.getChildren();
        List<TreeView<Category>> childrenI = viewI.getChildren();
        assert 0 == childrenH.size();
        assert 2 == childrenI.size();
        TreeView<Category> viewJ = childrenI.get(0);
        TreeView<Category> viewK = childrenI.get(1);
        assert catJ == viewJ.node;
        assert catK == viewK.node;
    }
}
