package net.neferett.LinarisKits.Managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.neferett.LinarisKits.api.Kit;
import net.neferett.LinarisKits.api.User;

import org.bukkit.entity.Player;

public class UsersManager {
    private List<User> m_users = new LinkedList<User>();
    private static UsersManager instance;

    public static UsersManager getInstance() {
        if (instance == null) {
            instance = new UsersManager();
        }
        return instance;
    }

    private UsersManager() {
    }

    public User getUser(Player player) {
        try {
        	DatabaseManager.getInstance();
            CraftUser user;
            for (User user2 : this.m_users) {
                if (!user2.getUUID().equals(player.getUniqueId())) continue;
                return user2;
            }
            String query = "SELECT * FROM `linariskits_users` WHERE user=?;";
            PreparedStatement state = DatabaseManager.prepareStatement(query);
            state.setString(1, player.getName());
            ResultSet result = state.executeQuery();
            if (result.next()) {
                int id = result.getInt("id");
                int coins = result.getInt("coins");
                HashMap<Integer, Integer> kits = new HashMap<Integer, Integer>();
                for (Kit kit : KitsManager.getInstance().getKits()) {
                    kits.put(kit.getId(), result.getInt(kit.getFieldName()));
                }
                user = new CraftUser(this, id, player, coins, kits);
            } else {
                query = "INSERT INTO `linariskits_users`(`user`) VALUES(?);";
                state = DatabaseManager.prepareStatement(query);
                state.setString(1, player.getName());
                state.executeUpdate();
                state.close();
                query = "SELECT LAST_INSERT_ID() AS id;";
                state = DatabaseManager.prepareStatement(query);
                ResultSet result_id = state.executeQuery();
                result_id.next();
                user = new CraftUser(this, result_id.getInt("id"), player, 0, new HashMap());
            }
            this.m_users.add(user);
            return user;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void removeUserFromCache(Player player) {
        int i = 0;
        while (i < this.m_users.size()) {
            if (this.m_users.get(i).getUUID().equals(player.getUniqueId())) {
                this.m_users.remove(i);
                return;
            }
            ++i;
        }
    }

    public void saveUser(User user) {
        try {
        	DatabaseManager.getInstance();
            StringBuilder query = new StringBuilder();
            query.append("UPDATE `linariskits_users` SET coins=?, ");
            LinkedList<Integer> args = new LinkedList<Integer>();
            int i = 0;
            for (Kit kit : KitsManager.getInstance().getKits()) {
                if (i++ > 0) {
                    query.append(", ");
                }
                query.append(kit.getFieldName()).append("=?");
                args.add(user.getLevelForKit(kit));
            }
            query.append(" WHERE id=?;");
            PreparedStatement state = DatabaseManager.prepareStatement(query.toString());
            state.setInt(1, user.getCoins());
            i = 0;
            while (i < args.size()) {
                state.setInt(i + 2, (Integer)args.get(i));
                ++i;
            }
            state.setInt(i + 2, user.getId());
            state.executeUpdate();
            state.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CraftUser
    implements User {
        private int m_id;
        private Player m_player;
        private int m_coins;
        private HashMap<Integer, Integer> m_kits;
        final UsersManager um;

        private CraftUser(UsersManager usersManager, int id, Player player, int coins, HashMap<Integer, Integer> kits) {
            this.um = usersManager;
            this.m_kits = new HashMap();
            this.m_id = id;
            this.m_player = player;
            this.m_coins = coins;
            this.m_kits = kits;
        }

        @Override
        public int getId() {
            return this.m_id;
        }

        @Override
        public Player getPlayer() {
            return this.m_player;
        }

        @Override
        public String getName() {
            return this.m_player.getName();
        }

        @Override
        public UUID getUUID() {
            return this.m_player.getUniqueId();
        }

        @Override
        public int getCoins() {
            return this.m_coins;
        }

        @Override
        public void setCoins(int coins) {
            this.m_coins = coins;
            this.um.saveUser(this);
        }

        @Override
        public void subCoins(int coins) {
            this.m_coins -= coins;
            this.um.saveUser(this);
        }

        @Override
        public void addCoins(int coins) {
            this.m_coins += coins;
            this.m_player.sendMessage("§6+<coins> §bcoins".replaceAll("<coins>", Integer.toString(coins)));
            this.um.saveUser(this);
        }

        @Override
        public int getLevelForKit(Kit kit) {
            if (!this.m_kits.containsKey(kit.getId())) {
                return 0;
            }
            return this.m_kits.get(kit.getId());
        }

        @Override
        public void addLevelForKit(Kit kit) {
            int actual = this.getLevelForKit(kit);
            this.m_kits.put(kit.getId(), actual + 1);
            this.um.saveUser(this);
        }

    }

}

