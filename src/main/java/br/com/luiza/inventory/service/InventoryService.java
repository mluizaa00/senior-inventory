package br.com.luiza.inventory.service;

import br.com.luiza.inventory.adapter.InventoryAdapter;
import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.utils.SQLReader;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class InventoryService {

    private final SQLReader reader;
    private final InventoryAdapter adapter;
    private final DataSource dataSource;

    public InventoryService(DataSource dataSource) throws IOException {
        this.dataSource = dataSource;
        this.reader = new SQLReader();
        this.adapter = new InventoryAdapter();

        reader.loadFromResources("sql/");

        createTable();
    }

    public void createTable() {
        try (
          final Connection connection = dataSource.getConnection();
          final PreparedStatement statement = connection.prepareStatement(reader.getSql("create_table"))
        ) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("A error occurred while creating inventory table.");
            exception.printStackTrace();
        }
    }

    public List<InventoryData> selectDatas(UUID uniqueId) {
        List<InventoryData> datas = new LinkedList<>();

        try (
          final Connection connection = dataSource.getConnection();
          final PreparedStatement statement = connection.prepareStatement(reader.getSql("select_datas"))
        ) {
            statement.setString(1, uniqueId.toString());

            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                datas.add(adapter.read(resultSet));
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return datas.stream()
          .sorted(Comparator.comparingLong(o -> o.getCreatedAt().getTime()))
          .collect(Collectors.toList());
    }

    public int deleteAfterExpired(long expireDays) {
        try (
          final Connection connection = dataSource.getConnection();
          final PreparedStatement statement = connection.prepareStatement(reader.getSql("delete_after_expired"))
        ) {

            statement.setLong(1, expireDays);

            return statement.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public void insert(InventoryData data) {
        try (
          final Connection connection = dataSource.getConnection();
          final PreparedStatement statement = connection.prepareStatement(reader.getSql("insert_data"))
        ) {
            adapter.insert(statement, data);

            statement.executeUpdate();

        } catch (SQLException exception) {
            System.out.println("A error occurred while inserting inventory to the database.");
            exception.printStackTrace();
        }
    }

}
