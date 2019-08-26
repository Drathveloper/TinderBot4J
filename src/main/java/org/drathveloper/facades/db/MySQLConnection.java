package org.drathveloper.facades.db;

import com.mysql.cj.MysqlConnection;
import org.drathveloper.facades.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class MySQLConnection {

    private Logger logger = LoggerFactory.getLogger(MysqlConnection.class);

    private static final String PROPS_FILE = "db.properties";

    private static Connection connection;

    private MySQLConnection() {
        try {
            FileLoader loader = FileLoader.getInstance();
            String path = loader.getExecutionPath();
            Map<String, String> props = loader.readPropertiesFile(path + PROPS_FILE);
            String url = props.get("url");
            String user = props.get("user");
            String pass = props.get("pass");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException | FileNotFoundException ex){
            logger.debug("Connection couldn't be stablished");
            connection = null;
        }
    }

    public static Connection getInstance() {
        if(connection==null){
            new MySQLConnection();
        }
        return connection;
    }

}
