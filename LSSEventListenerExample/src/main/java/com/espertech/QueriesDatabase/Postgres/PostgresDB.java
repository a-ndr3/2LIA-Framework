package com.espertech.QueriesDatabase.Postgres;

import com.espertech.QueriesDatabase.QueriesDB;
import com.espertech.QueriesDatabase.QueryMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PostgresDB implements QueriesDB {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/esper_queries";
    private static final String DB_USER = "admin";
    private static final String DB_PASS = "admin";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PostgresDB() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<QueryMetadata> fetchQueries() {
        List<QueryMetadata> queries = new ArrayList<>();
        String sql = "SELECT q.id, q.name, q.query_statement, q.deploymentId, q.query, q.event_classes, c.name as category, q.created_at, q.updated_at, q.status, q.description " +
                     "FROM esper_queries q JOIN query_category c ON q.category_id = c.id"; //WHERE q.status = TRUE

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                QueryMetadata qm = new QueryMetadata(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("query_statement"),
                        rs.getString("deploymentId"),
                        rs.getString("query"),
                        objectMapper.readValue(rs.getString("event_classes"), List.class),
                        rs.getString("category"),
                        Timestamp.valueOf(rs.getString("created_at")).getTime(),
                        Timestamp.valueOf(rs.getString("updated_at")).getTime(),
                        rs.getBoolean("status"),
                        rs.getString("description")
                );
                queries.add(qm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return queries;
    }

    @Override
    public CompletableFuture<Void> insertQueriesAsync(Collection<QueryMetadata> queries) {
        return CompletableFuture.runAsync(() -> insertQueries(queries));
    }

    @Override
    public CompletableFuture<Collection<QueryMetadata>> fetchQueriesAsync() {
        return CompletableFuture.supplyAsync(this::fetchQueries);
    }

    @Override
    public Collection<String> getAvailableCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM query_category";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public void insertQueries(Collection<QueryMetadata> queries) {
        String sql = "INSERT INTO esper_queries (id, name, query_statement, deploymentId, query, event_classes, category_id, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?::jsonb, (SELECT id FROM query_category WHERE name = ?), ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            var counter = 0;
            for (var query : queries) {
                parseQuery(query, ps);
                ps.addBatch();
                ps.clearParameters();
                counter++;

                if (counter > 1500) {
                    ps.executeBatch();
                    counter = 0;
                }
            }
            ps.executeBatch();
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertQuery(QueryMetadata query) {
        String sql = "INSERT INTO esper_queries (id, name, query_statement, deploymentId, query, event_classes, category_id, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?::jsonb, (SELECT id FROM query_category WHERE name = ?), ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            parseQuery(query, ps);

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseQuery(QueryMetadata query, PreparedStatement ps) throws SQLException, JsonProcessingException {
        ps.setObject(1, query.id);
        ps.setString(2, query.name);
        ps.setString(3, query.queryStatement);
        ps.setObject(4, query.deploymentId);
        ps.setString(5, query.query);
        ps.setString(6, objectMapper.writeValueAsString(query.eventClasses));
        ps.setString(7, query.category);
        ps.setBoolean(8, query.status);
        ps.setString(9, query.description);
    }

    @Override
    public void updateQueryStatus(UUID queryId, boolean status) {
        String sql = "UPDATE esper_queries SET status = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, status);
            pstmt.setObject(2, queryId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean ping() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void deleteQuery(UUID queryId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM esper_queries WHERE id = ?")) {
            pstmt.setObject(1, queryId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveUpdatedQuery(QueryMetadata query) {
        String sql = "UPDATE esper_queries SET name = ?, query_statement = ?, deploymentId = ?, query = ?, event_classes = ?::jsonb, category_id = (SELECT id FROM query_category WHERE name = ?), status = ?, description = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, query.name);
            pstmt.setString(2, query.queryStatement);
            pstmt.setObject(3, query.deploymentId);
            pstmt.setString(4, query.query);
            pstmt.setString(5, objectMapper.writeValueAsString(query.eventClasses));
            pstmt.setString(6, query.category);
            pstmt.setBoolean(7, query.status);
            pstmt.setString(8, query.description);
            pstmt.setObject(9, query.id);

            pstmt.executeUpdate();
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
