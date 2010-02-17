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
 * A NodeInfo implementor carries information about its identity and position in
 * a nested set.
 *
 * @author Roman Borschel <roman@code-factory.org>
 */
public interface NodeInfo {
    int getId();
    int getLeftValue();
    int getRightValue();
    int getLevel();
    int getRootValue();
    void setLeftValue(int value);
    void setRightValue(int value);
    void setLevel(int level);
    void setRootValue(int value);
}
