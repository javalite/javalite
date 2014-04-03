package org.javalite.activejdbc.test_models;


/*
 * Copyright (C) 2014 Sebastian Grunow <s.grunow at grunow-it.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.List;
        import java.util.TreeMap;
        import org.javalite.activejdbc.Model;
        import org.javalite.activejdbc.annotations.Table;

/**
 *
 * @author Sebastian Grunow <s.grunow at grunow-it.de>
 * @version 20140326
 * @since 2014-03-26
 */
@Table("gperms_memberships")
public class Membership extends Model {

    private static final TreeMap<PermissionOwner, List<Membership>> playerGroupMap = new TreeMap<PermissionOwner, List<Membership>>();

    /**
     * Clears the cache. Used in reload.
     */
    public static void clearCache() {
        playerGroupMap.clear();
    }

    /**
     *
     * @param member
     * @param parent
     * @return
     */
    public static Membership getNewMembership(PermissionOwner member, PermissionOwner parent) {
        System.out.println("New Membership: " + member.getName() + " -> " + parent.getName());
        if (playerGroupMap.containsKey(member)) {
            for (Membership m : playerGroupMap.get(member)) {
                if (m.getParentGroup().equals(parent)) {
                    return m;
                }
            }
        }
        Membership membership = new Membership();
        membership.setInteger("type", PermissionType.PLAYER);
        membership.setInteger("member", member.getId());
        membership.setInteger("parent", parent.getId());
        if (membership.save()) {
            if (playerGroupMap.containsKey(member)) {
                playerGroupMap.get(member).add(membership);
            } else {
                List<Membership> msList = new ArrayList<Membership>();
                msList.add(membership);
                playerGroupMap.put(member, msList);
            }
            return membership;
        }
        return null;
    }

    /**
     *
     * @param owner
     * @return
     */
    public static List<Membership> getMembershipsForMember(PermissionOwner owner) {
        if (playerGroupMap.containsKey(owner)) {
            return playerGroupMap.get(owner);
        }
        List<Membership> list = new ArrayList<Membership>();
        List<Membership> memberships = Membership.find("type = ? AND member = ?", owner.getType().getType(), owner.getId());
        if (memberships.isEmpty()) {
            List<Group> defaultGroups = Group.getDefaultGroups();
            if (!defaultGroups.isEmpty()) {
                for (Group group : defaultGroups) {
                    Membership m = new Membership();
                    m.setInteger("member", owner.getId());
                    m.setInteger("parent", group.getId());
                    m.save();
                    list.add(m);
                }
            }
        } else {
            for (Membership m : memberships) {
                Calendar expiration = Calendar.getInstance();
                try {
                    expiration.setTime(m.getDate("expiration"));
                } catch (NullPointerException ex) {
                    expiration.setTimeInMillis(0);
                }
                if (expiration.getTimeInMillis() > 0) {
                    if (Calendar.getInstance().compareTo(expiration) > 0) {
                        removeMembership(m.getMemberAsOwner(), m.getParentGroup());
                        continue;
                    }
                }
                list.add(m);
            }
        }
        playerGroupMap.put(owner, list);
        return list;
    }

    /**
     * Returns all members of given parent with the membership type of type. A
     * membership type PLAYER would return all players, while a membership type
     * GROUP would list all sub-groups of given parent.
     *
     * @param type
     * @param parent
     * @return
     */
    public static List<Membership> getMembershipsForParent(PermissionType type, PermissionOwner parent) {
        List<Membership> memberships = Membership.find("type = ? AND parent = ?", type.getType(), parent.getId());
        if (memberships == null || memberships.isEmpty()) {
            return new ArrayList<Membership>();
        }
        List<Membership> mList = new ArrayList<Membership>();
        for (Membership membership : memberships) {
            if (playerGroupMap.containsKey(membership.getMemberAsOwner())) {
                if (!playerGroupMap.get(membership.getMemberAsOwner()).contains(membership)) {
                    playerGroupMap.get(membership.getMemberAsOwner()).add(membership);
                }
            } else {
                List<Membership> tempList = new ArrayList<Membership>();
                tempList.add(membership);
                playerGroupMap.put(membership.getMemberAsOwner(), tempList);
            }
            mList.add(membership);
        }
        return mList;
    }

    /**
     *
     * @param member
     * @param group
     * @return
     */
    public static boolean removeMembership(PermissionOwner member, Group group) {
        if (playerGroupMap.containsKey(member)) {
            List<Membership> msList = playerGroupMap.get(member);
            for (Membership membership : msList) {
                if (membership.getParentGroup().getName().equals(group.getName())) {
                    msList.remove(membership);
                    playerGroupMap.put(member, msList);
                    membership.delete();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public Integer getMember() {
        return this.getInteger("member");
    }

    /**
     *
     * @return
     */
    public Integer getParent() {
        return this.getInteger("parent");
    }

    /**
     *
     * @return
     */
    public Integer getType() {
        return this.getInteger("type");
    }

    /**
     * Gets the member part of this membership. Returns a player if type is 1 or a
     * group if type is 2. Otherwise returns null as there are no memberships for
     * the other types (yet).
     *
     * ActiveJDBC instrumentation doesn't seem to like it when using
     * getInteger("member") in this method. When running it instantly crashes Java
     * if you do. I used a workaround using getMember() and getType() to fix this
     * bug. Though I still don't understand why it doesn't work.
     *
     * @return
     */
    public PermissionOwner getMemberAsOwner() {
        PermissionType type = PermissionType.getPermissionTypeForId(getType());
        switch (type) {
            case PLAYER:
                return Player.getPlayerForId(getMember());
            case GROUP:
                return Group.getGroupForId(getMember());
            default:
                return null;
        }
    }

    /**
     * Returns the parent group as a Group object.
     *
     * @return
     */
    public Group getParentGroup() {
        return Group.getGroupForId(getParent());
    }
}