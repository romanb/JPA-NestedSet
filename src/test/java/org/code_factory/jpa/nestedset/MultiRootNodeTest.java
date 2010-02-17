/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import org.code_factory.jpa.nestedset.model.Category;
import org.testng.annotations.Test;

/**
 * @author Roman Borschel <roman@code-factory.org>
 */
public class MultiRootNodeTest extends FunctionalNestedSetTest {

    @Test
    public void testCreateTrees() {
        Category javaCat = new Category();
        javaCat.setName("Java");
        javaCat.setRootValue(1);

        Category netCat = new Category();
        netCat.setName(".NET");
        netCat.setRootValue(2);

        Category phpCat = new Category();
        phpCat.setName("PHP");
        phpCat.setRootValue(3);

        em.getTransaction().begin();
        nsm.createRoot(javaCat);
        nsm.createRoot(netCat);
        nsm.createRoot(phpCat);
        em.getTransaction().commit();

        assert 1 == javaCat.getLeftValue();
        assert 2 == javaCat.getRightValue();
        assert 1 == netCat.getLeftValue();
        assert 2 == netCat.getRightValue();
        assert 1 == phpCat.getLeftValue();
        assert 2 == phpCat.getRightValue();

        em.getTransaction().begin();
        Node<Category> javaNode = nsm.getNode(javaCat);
        Category ejbCat = new Category();
        ejbCat.setName("EJB");
        Node<Category> ejbNode = javaNode.addChild(ejbCat);
        em.getTransaction().commit();

        assert 1 == javaCat.getLeftValue();
        assert 2 == ejbCat.getLeftValue();
        assert 3 == ejbCat.getRightValue();
        assert 1 == ejbCat.getLevel();
        assert 1 == ejbCat.getRootValue();
        assert 4 == javaCat.getRightValue();
        assert 1 == netCat.getLeftValue();
        assert 2 == netCat.getRightValue();
        assert 1 == phpCat.getLeftValue();
        assert 2 == phpCat.getRightValue();

        // move between trees

        em.getTransaction().begin();
        Node<Category> netNode = nsm.getNode(netCat);
        ejbNode.moveAsLastChildOf(netNode);
        // Refresh to make sure that we check the database state, not just the in-memory state.
        em.refresh(javaCat);
        em.refresh(netCat);
        em.refresh(phpCat);
        em.getTransaction().commit();

        assert 1 == javaCat.getLeftValue();
        assert 2 == javaCat.getRightValue();
        assert 1 == netNode.getLeftValue();
        assert 2 == ejbNode.getLeftValue();
        assert 3 == ejbNode.getRightValue();
        assert 2 == ejbNode.getRootValue();
        assert 4 == netNode.getRightValue();
        assert 1 == phpCat.getLeftValue();
        assert 2 == phpCat.getRightValue();

    }

    
}
