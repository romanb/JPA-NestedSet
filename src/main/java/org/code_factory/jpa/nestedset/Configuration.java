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
 * A configuration for a class managed by a NestedSetManager.
 *
 * @author robo
 */
class Configuration {
    private String leftFieldName;
    private String rightFieldName;
    private String levelFieldName;
    private String rootIdFieldName;
    private String entityName;
    
    private boolean hasManyRoots = false;

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
        this.hasManyRoots = true;
    }

    public boolean hasManyRoots() {
        return this.hasManyRoots;
    }

    @Override public String toString() {
        return "[leftFieldName: " + this.leftFieldName + ", rightFieldName:" + this.rightFieldName
                + ", levelFieldName: " + this.levelFieldName + ", rootIdFieldName:" + this.rootIdFieldName + "]";
    }

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String tableName) {
		this.entityName = tableName;
	}
}
