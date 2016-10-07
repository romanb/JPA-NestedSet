/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.code_factory.jpa.nestedset.model;

import javax.persistence.*;

import org.code_factory.jpa.nestedset.NodeInfo;
import org.code_factory.jpa.nestedset.annotations.LeftColumn;
import org.code_factory.jpa.nestedset.annotations.LevelColumn;
import org.code_factory.jpa.nestedset.annotations.RightColumn;
import org.code_factory.jpa.nestedset.annotations.RootColumn;

/**
 * @author robo
 */
@Entity
public class Category implements NodeInfo {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Column(updatable=false)
    @LeftColumn
    private Integer lft;
    @RightColumn
    @Column(updatable=false)
    private Integer rgt;
    @LevelColumn
    @Column(updatable=false)
    private Integer level;
    @RootColumn
    private Long rootId;

    @Override public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Integer getLeftValue() {
        return this.lft;
    }

    @Override
    public Integer getRightValue() {
        return this.rgt;
    }

    @Override
    public Integer getLevel() {
        return this.level;
    }

    @Override
    public void setLeftValue(Integer value) {
        this.lft = value;
    }

    @Override
    public void setRightValue(Integer value) {
        this.rgt = value;
    }

    @Override
    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public Long getRootValue() {
        return this.rootId;
    }

    @Override
    public void setRootValue(Long value) {
        this.rootId = value;
    }

    @Override public String toString() {
        return "[Category: id=" + this.id + ", name=" + this.name + "-" + super.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (!id.equals(category.id)) return false;
        if (!name.equals(category.name)) return false;
        if (!lft.equals(category.lft)) return false;
        if (!rgt.equals(category.rgt)) return false;
        if (!level.equals(category.level)) return false;
        return rootId != null ? rootId.equals(category.rootId) : category.rootId == null;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + lft.hashCode();
        result = 31 * result + rgt.hashCode();
        result = 31 * result + level.hashCode();
        result = 31 * result + (rootId != null ? rootId.hashCode() : 0);
        return result;
    }
}
