/**
 * LICENSE
 *
 * This source file is subject to the MIT license that is bundled
 * with this package in the file MIT.txt.
 * It is also available through the world-wide-web at this URL:
 * http://www.opensource.org/licenses/mit-license.html
 */

package org.code_factory.jpa.nestedset;

import net.jcip.annotations.Immutable;

/**
 * @author Roman Borschel <roman@code-factory.org>
 */
@Immutable
class Key {
    private final Class<?> clazz;
    private final int id;

    public Key(Class<?> clazz, int id) {
        this.clazz = clazz;
        this.id = id;
    }

    @Override public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
        hash = 23 * hash + this.id;
        return hash;
    }

    @Override public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Key)) {
            return false;
        }

        Key otherKey = (Key) other;

        return this.clazz.equals(otherKey.clazz)
                && this.id == otherKey.id;
    }

    @Override public String toString() {
        return "[Class: " + this.clazz.getName() + ", Id: " + this.id + "]";
    }
}
