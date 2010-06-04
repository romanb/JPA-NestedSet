/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import java.util.Iterator;
import java.util.List;
import org.code_factory.jpa.nestedset.model.Category;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author Roman Borschel <roman@code-factory.org>
 */
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
        Node<Category> rootNode = nsm.createRoot(this.progCat);
        rootNode.addChild(this.javaCat);
        rootNode.addChild(this.netCat);
        em.getTransaction().commit();
        em.clear();
        nsm.clear();
    }

    @Test public void testCreateRoot() {
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

    @Test public void testFetchTree() {
        this.createBasicTree();

        List<Node<Category>> tree = nsm.fetchTreeAsList(Category.class);
        assert tree.size() == 3;
        Iterator<Node<Category>> iter = tree.iterator();
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

    @Test public void testBasicTreeNavigation() {
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

    @Test public void testAddingNodesToTree() {
        this.createBasicTree();

        assert 0 == this.nsm.getNodes().size();
        Node<Category> root = this.nsm.getNode(em.find(Category.class, this.progCat.getId()));

        // Assert basic tree state, a Programming category with 2 child categories.
        assert 1 == root.getLeftValue();
        assert 6 == root.getRightValue();
        assert 2 == root.getChildren().size();
        assert 2 == root.getChildren().size();

        assert 3 == this.nsm.getNodes().size();

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

        assert 5 == this.nsm.getNodes().size();
    }

    /**
     * Tests creating new nodes and moving them around in a tree.
     */
    @Test public void testMovingNodes() {
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
        wpfNode.moveAsLastChildOf(netNode);
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
    }

    @Test
    public void testDeleteNode() {
        this.createBasicTree();
        
        em.getTransaction().begin();
        // fetch the tree
        Node<Category> progNode = nsm.fetchTree(Category.class, this.progCat.getRootValue());
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
}
