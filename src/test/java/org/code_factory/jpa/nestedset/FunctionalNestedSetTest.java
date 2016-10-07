/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author robo
 */
public class FunctionalNestedSetTest {
    private static EntityManagerFactory emFactory;
    protected EntityManager em;
    protected NestedSetManager nsm;

    @BeforeClass
    public static void createEntityManagerFactory() {
        try {
            emFactory = Persistence.createEntityManagerFactory("TestPU");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Before
    public void createEntityManager() {
        em = emFactory.createEntityManager();
        this.nsm = new JpaNestedSetManager(this.em);
    }

    @After
    public void closeEntityManager() {
        if (em != null) {
            em.getTransaction().begin();
            em.createQuery("delete from Category").executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
        this.nsm = null;
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        if (emFactory != null) {
            emFactory.close();
        }
    }

    protected void printTree(Node<?> node) {
        printNode(node);
        if (node.hasChildren()) {
            for (Node<?> child : node.getChildren()) {
                printTree(child);
            }
        }
    }

    protected void printNode(Node<?> node) {
        for (int i=0; i<node.getLevel(); i++) {
            System.out.print("--");
        }
        System.out.println(node.toString());
    }
}
