/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

/**
 *
 * @author robo
 */
public class Configuration {
    private String leftFieldName = "lft";
    private String rightFieldName = "rgt";
    private String levelFieldName = "level";
    private String rootIdFieldName = "rootId";

    /**
     * @return the leftFieldName
     */
    public String getLeftFieldName() {
        return leftFieldName;
    }

    /**
     * @param leftFieldName the leftFieldName to set
     */
    public void setLeftFieldName(String leftFieldName) {
        this.leftFieldName = leftFieldName;
    }

    /**
     * @return the rightFieldName
     */
    public String getRightFieldName() {
        return rightFieldName;
    }

    /**
     * @param rightFieldName the rightFieldName to set
     */
    public void setRightFieldName(String rightFieldName) {
        this.rightFieldName = rightFieldName;
    }

    /**
     * @return the levelFieldName
     */
    public String getLevelFieldName() {
        return levelFieldName;
    }

    /**
     * @param levelFieldName the levelFieldName to set
     */
    public void setLevelFieldName(String levelFieldName) {
        this.levelFieldName = levelFieldName;
    }

    /**
     * @return the rootIdFieldName
     */
    public String getRootIdFieldName() {
        return rootIdFieldName;
    }

    /**
     * @param rootIdFieldName the rootIdFieldName to set
     */
    public void setRootIdFieldName(String rootIdFieldName) {
        this.rootIdFieldName = rootIdFieldName;
    }

}
