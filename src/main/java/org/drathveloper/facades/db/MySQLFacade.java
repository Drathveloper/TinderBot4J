package org.drathveloper.facades.db;

import org.drathveloper.models.User;
import org.drathveloper.models.UserBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLFacade implements SQLFacade {

    private Logger logger = LoggerFactory.getLogger(MySQLFacade.class);

    private static MySQLFacade instance;

    private Connection connection;

    private MySQLFacade() {
        connection = MySQLConnection.getInstance();
    }

    public static MySQLFacade getInstance(){
        if(instance == null){
            instance = new MySQLFacade();
        }
        return instance;
    }

    @Override
    public boolean isConnectionAvailable(){
        return connection!=null;
    }


    @Override
    public void insertBatch(UserBatch batch){
        logger.info("Start writing users batch on database");
        PreparedStatement peopleStmt;
        PreparedStatement photosStmt;
        try {
            peopleStmt = connection.prepareStatement("INSERT INTO people (id, name, bio, traveling, liked, matched) VALUES(?,?,?,?,?,?)");
            photosStmt = connection.prepareStatement("INSERT INTO photos (person_id, url) VALUES (?,?)");
            for(int i=0; i<batch.size(); i++){
                User user = batch.getUser(i);
                this.buildPeopleBatch(peopleStmt, user);
                this.buildPhotosBatch(photosStmt, user);
            }
            peopleStmt.executeBatch();
            photosStmt.executeBatch();
        } catch(SQLException ex){
            logger.info("Error writing users on database: " + ex.getMessage());
        }
        logger.info("End writing users batch on database");
    }

    private void buildPhotosBatch(PreparedStatement ps, User user) throws SQLException {
        List<String> gallery = user.getPhotoURL();
        for(String url : gallery) {
            ps.setString(1, user.getId());
            ps.setString(2, url);
            ps.addBatch();
        }
    }

    private void buildPeopleBatch(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getBio());
        ps.setBoolean(4, user.isTraveling());
        ps.setBoolean(5, user.isLiked());
        ps.setBoolean(6, user.isMatch());
        ps.addBatch();
    }
}
