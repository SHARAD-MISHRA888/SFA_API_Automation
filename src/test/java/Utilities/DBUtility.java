package Utilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUtility {

    private static final String DB_URL = "jdbc:mysql://staging.prism-sfa-dev.net:3306/sfa_db";
    private static final String USER = "PrismSFA";
    private static final String PASSWORD = "PrismSFA123_";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL,USER,PASSWORD);
    }

    public static List<Map<String,Object>> executeQuery(String query, Object... params){
        List<Map<String,Object>> resultList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i =0;i<params.length;i++){
                stmt.setObject(i+1,params[i]);
            }

            try(ResultSet rs = stmt.executeQuery()){
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()){
                    Map<String,Object> row = new HashMap<>();
                    for(int i =1;i<=columnCount;i++){
                        row.put(metaData.getColumnLabel(i),rs.getObject(i));
                    }

                    resultList.add(row);
                }
            }



        }catch (SQLException e) {
            throw new RuntimeException("DB Query Failed :" + e.getMessage(), e);
        }

        return resultList;
    }

    public static int executeUpdate(String query,Object... params){
        try(Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)){
            for (int i =0;i<params.length;i++){
                stmt.setObject(i+1,params[i]);
            }
            return stmt.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("DB Query Failed" +e.getMessage());
        }

    }


    public static Object getSingleValue(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Fetch Failed: " + e.getMessage(), e);
        }
        return null;
    }


    public static List<Object> getColumnValues(String query, Object... params) {
        List<Object> values = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getObject(1)); // always fetch first column
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Column Fetch Failed: " + e.getMessage(), e);
        }
        return values;
    }

    public static int getSingleIntValue(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        throw new RuntimeException("No value found for query: " + query);
    }

    public static List<Integer> getIntList(String query, Object... params) {
        List<Integer> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        return list;
    }

    public static Double getSingleDoubleValue(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        throw new RuntimeException("No value found for query: " + query);
    }

    public static double[] getLatLong(String query, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new double[]{
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude")
                    };
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB Query Failed: " + e.getMessage(), e);
        }
        throw new RuntimeException("No value found for query: " + query);
    }

    public static Integer getMemberIdByMobile(String mobile) {
        return DBUtility.getSingleIntValue(
                "SELECT id FROM sfa_db.members WHERE mobile = ?;", mobile
        );
    }

    public static Integer getManagerIdByMemberId(Integer memberId) {
        return DBUtility.getSingleIntValue(
                "SELECT reporting_manager_id FROM sfa_db.members WHERE id = ?;", memberId
        );
    }





}
