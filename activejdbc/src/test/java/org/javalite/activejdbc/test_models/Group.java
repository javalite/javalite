package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Table("gperms_groups")
public class Group extends Model implements PermissionOwner {

    public static TreeMap<Integer, Group> cachedGroups = new TreeMap<Integer, Group>();

    public static void clearCache() {
        cachedGroups.clear();
    }

    public static Group getGroupForId(Integer id) {
        if (cachedGroups.containsKey(id)) {
            return cachedGroups.get(id);
        }
        Group g = Group.findFirst("id = ?", id);
        cachedGroups.put(id, g);
        return g;
    }

    public static Group getGroupForName(String name) {
        for (Group s : cachedGroups.values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        Group s = Group.findFirst("name = ?", name);
        if (s != null) {
            cachedGroups.put(s.getId(), s);
            return s;
        }
        return null;
    }

    public static Group getNewGroup(String name, Integer rank) {
        Group g = Group.create("name", name, "displayname", "", "rank", rank, "prefix", "", "suffix", "");
        if(g.save()) {
            cachedGroups.put(g.getId(), g);
            return g;
        }
        return null;
    }

    public static List<Group> getDefaultGroups() {
        List<Group> gList = new ArrayList<Group>();
        for (Group g : cachedGroups.values()) {
            if(g.isDefault()) {
                gList.add(g);
            }
        }
        return gList;
    }

    public static boolean deleteGroup(Integer id) {
        if(cachedGroups.containsKey(id)) {
            cachedGroups.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public Integer getId() {
        return this.getInteger("id");
    }

    @Override
    public String getName() {
        return this.getString("name");
    }

    public String getDisplayName() {
        if (this.getString("displayname").isEmpty()) {
            return getName();
        }
        return this.getString("displayname");
    }

    @Override
    public String getDisplayString() {
        return getPrefix() + getDisplayName() + getSuffix();
    }


    public Boolean isDefault() {
        return this.getBoolean("isDefault");
    }

    @Override
    public String getPrefix() {
        return this.getString("prefix");
    }

    @Override
    public String getSuffix() {
        return this.getString("suffix");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public PermissionType getType() {
        return PermissionType.GROUP;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Group)) return false;
        Group group = (Group) other;
        return group.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash * this.getId();
        hash = 13 * hash * this.getDisplayString().hashCode();
        hash = 13 * hash * this.getType().getType();
        return hash;
    }

    @Override
    public int compareTo(PermissionOwner o) {
        if(this.getType() == o.getType()) {
            if(this.getId() > o.getId()) {
                return 1;
            }
            if(this.getId() == o.getId()) {
                return 0;
            }
            if(this.getId() < o.getId()) {
                return -1;
            }
        } else {
            if(this.getType().getType() > o.getType().getType()) {
                return 1;
            }
            if(this.getType().getType() < o.getType().getType()) {
                return -1;
            }
        }
        return 0;
    }
}