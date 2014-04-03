package org.javalite.activejdbc.test_models;



import java.util.List;
import java.util.TreeMap;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;


@Table("gperms_players")
public class Player extends Model implements PermissionOwner {

    private List<Membership> memberships;

    public static TreeMap<Integer, Player> cachedPlayers = new TreeMap<Integer, Player>();

    public static void clearCache() {
        cachedPlayers.clear();
    }

    public static Player getPlayerForId(Integer id) {
        if (cachedPlayers.containsKey(id)) {
            return cachedPlayers.get(id);
        }
        Player p = Player.findFirst("id = ?", id);
        p.getMemberships();
        cachedPlayers.put(id, p);
        return p;
    }

    public static Player getPlayerForName(String name) {
        for (Player p : cachedPlayers.values()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        Player p = Player.findFirst("name = ?", name);
        if (p != null) {
            cachedPlayers.put(p.getId(), p);
            p.getMemberships();
            return p;
        }
        return null;
    }

    public static Player getNewPlayer(String name) {
        Player p = Player.create("name", name, "displayname", "", "prefix", "", "suffix", "");
        if (p.save()) {
            cachedPlayers.put(p.getId(), p);
            p.getMemberships();
            return p;
        }
        return null;
    }

    public static void removeCachedPlayer(Player p) {
        cachedPlayers.remove(p.getId());
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

    public String getDisplayString() {
        return getPrefix() + getDisplayName() + getSuffix();
    }

    public String getPrefix() {
        return this.getString("prefix");
    }

    public String getSuffix() {
        return this.getString("suffix");
    }

    public List<Membership> getMemberships() {
        if(memberships == null) {
            memberships = Membership.getMembershipsForMember(this);
        }
        return memberships;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public PermissionType getType() {
        return PermissionType.PLAYER;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Player)) return false;
        Player player = (Player) other;
        return player.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 9;
        hash = 19 * hash * this.getId();
        hash = 19 * hash * this.getDisplayString().hashCode();
        hash = 19 * hash * this.getType().getType();
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