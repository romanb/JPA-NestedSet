/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 *
 * @author robo
 */
public class FunctionalNestedSetTest {
    protected EntityManagerFactory emFactory;
    protected EntityManager em;
    protected NestedSetManager nsm;

    @BeforeClass(alwaysRun=true)
    protected void createEntityManagerFactory() {
        try {
            emFactory = Persistence.createEntityManagerFactory("TestPU");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @BeforeMethod(alwaysRun=true)
    protected void createEntityManager() {
        em = emFactory.createEntityManager();
        this.nsm = new JpaNestedSetManager(this.em);
    }

    @AfterMethod
    protected void closeEntityManager() {
        if (em != null) {
            em.getTransaction().begin();
            em.createQuery("delete from Category").executeUpdate();
            em.getTransaction().commit();
            em.close();
        }
        this.nsm = null;
    }

    @AfterClass
    protected void closeEntityManagerFactory() {
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
