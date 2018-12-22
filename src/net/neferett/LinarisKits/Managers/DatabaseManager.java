
package net.neferett.LinarisKits.Managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import net.neferett.LinarisKits.LinarisKits;
import net.neferett.LinarisKits.api.Kit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DatabaseManager {
    private Connection m_connection;
    public static final String TABLE_USERS = "linariskits_users";
    public static final String TABLE_KITS = "linariskits_kits";
    public static final String TABLE_KITS_LEVELS = "linariskits_kitslevels";
    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        try {
            if (DatabaseManager.instance.m_connection.isClosed()) {
                instance.open();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static void closeConnection() {
        if (instance != null) {
            try {
                DatabaseManager.instance.m_connection.close();
                instance = null;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void open() {
        try {
            String url = "jdbc:mysql://" + LinarisKits.getString("mysql.host") + ":" + LinarisKits.getInt("mysql.port") + "/" + LinarisKits.getString("mysql.database");
            String user = LinarisKits.getString("mysql.user");
            String passwd = LinarisKits.getString("mysql.password");
            this.m_connection = DriverManager.getConnection(url, user, passwd);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.open();
            Statement state = this.m_connection.createStatement();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS `").append("linariskits_kits").append("` ").append("(`id` int(11) NOT NULL AUTO_INCREMENT,").append("`name` varchar(50) NOT NULL,").append("`permission` varchar(60) NOT NULL,").append("`game` varchar(50) NOT NULL,").append("`desc` varchar(100) NOT NULL,").append("`item` varchar(100) NOT NULL,").append("UNIQUE KEY `id` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
            state.executeUpdate(sqlBuilder.toString());
            sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS `").append("linariskits_kitslevels").append("` ").append("(`id` int(11) NOT NULL AUTO_INCREMENT,").append("`kit_id` int(11) NOT NULL,").append("`level` int(11) NOT NULL,").append("`price` int(11) NOT NULL,").append("`lore` text NOT NULL,").append("`items` text NOT NULL,").append("UNIQUE KEY `id` (`id`)) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
            state.executeUpdate(sqlBuilder.toString());
            sqlBuilder = new StringBuilder();
            sqlBuilder.append("CREATE TABLE IF NOT EXISTS `").append("linariskits_users").append("` ").append("(`id` int(11) NOT NULL AUTO_INCREMENT,").append("`user` varchar(50) NOT NULL,").append("`coins` int(11) NOT NULL DEFAULT 0,").append("PRIMARY KEY (`id`),").append("UNIQUE KEY `user` (`user`)").append(") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1;");
            state.executeUpdate(sqlBuilder.toString());
            state.close();
        }
        catch (ClassNotFoundException e) {
            System.out.println("**********************************************");
            System.out.println("[LinarisKits] Driver MySQL introuvable");
            System.out.println("[LinarisKits] Plugin désactivé ...");
            System.out.println("**********************************************");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin((Plugin)LinarisKits.getInstance());
        }
        catch (SQLException e) {
            System.out.println("**********************************************");
            System.out.println("[LinarisKits] Probl\u00e8me de BDD : " + e.getMessage());
            System.out.println("[LinarisKits] Plugin désactivé ...");
            System.out.println("**********************************************");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin((Plugin)LinarisKits.getInstance());
        }
    }

    public void update() {
        try {
            Statement state = this.m_connection.createStatement();
            if (KitsManager.getInstance().getKits().size() > 0) {
                ResultSet result = state.executeQuery("DESCRIBE `linariskits_users`;");
                LinkedList<String> fields = new LinkedList<String>();
                while (result.next()) {
                    fields.add(result.getString("Field"));
                }
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("ALTER TABLE `linariskits_users`");
                int i = 0;
                for (Kit kit : KitsManager.getInstance().getKits()) {
                    if (fields.contains(kit.getFieldName())) continue;
                    if (i++ > 0) {
                        sqlBuilder.append(",");
                    }
                    sqlBuilder.append(" ADD `");
                    sqlBuilder.append(kit.getFieldName());
                    sqlBuilder.append("` int(11) NOT NULL DEFAULT 0");
                }
                sqlBuilder.append(";");
                state.executeUpdate(sqlBuilder.toString());
            }
            state.close();
        }
        catch (SQLException e) {
            System.out.println("**********************************************");
            System.out.println("[LinarisKits] Probl\u00e8me de BDD : " + e.getMessage());
            System.out.println("[LinarisKits] Plugin désactivé ...");
            System.out.println("**********************************************");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin((Plugin)LinarisKits.getInstance());
        }
    }

    public static PreparedStatement prepareStatement(String query) throws SQLException {
        return DatabaseManager.getInstance().m_connection.prepareStatement(query);
    }

    public void showState() {
        try {
            System.out.println("------- LINARIS KIT BDD -------");
            System.out.println("isClosed = " + this.m_connection.isClosed());
            System.out.println("isReadOnly = " + this.m_connection.isReadOnly());
            System.out.println("isValid(1000) = " + this.m_connection.isValid(1000));
            System.out.println("-------------------------------");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

