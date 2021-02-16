package com.luizaprestes.storage;

import com.celeste.function.SqlFunction;
import com.celeste.provider.SQLConnectionProvider;
import com.celeste.util.item.ItemSerialization;
import com.luizaprestes.model.InventoryData;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryStorage {

    private static final String CREATE_DATA_SQL = "CREATE TABLE IF NOT EXISTS `inventory_data` (" +
        "id CHAR(36) PRIMARY KEY NOT NULL," +
        "player_id CHAR(36) NOT NULL," +
        "items TEXT NOT NULL," +
        "created_at TIMESTAMP NOT NULL DEFAULT now()" +
        ");";
    private static final String STORE_DATA_SQL = "REPLACE INTO `inventory_data` VALUES (?, ?, ?, ?);";
    private static final String SELECT_ALL_DATA_SQL = "SELECT * FROM `inventory_data` WHERE player_id=?;";
    private static final String DELETE_AFTER_EXPIRE = "DELETE FROM `ìnventory_data` WHERE DATEDIFF(now(), inventory_data.created_at) > ?;";

    private final SQLConnectionProvider provider;
    private final SqlFunction<ResultSet, InventoryData> function;

    public InventoryStorage(SQLConnectionProvider provider) {
        this.provider = provider;
        this.function = this::read;

        createTable();
    }

    public void createTable() {
        provider.executeUpdate(CREATE_DATA_SQL);
    }

    public void store(InventoryData data) {
        provider.executeUpdate(
            STORE_DATA_SQL,
            data.getId(),
            data.getPlayerId(),
            ItemSerialization.serialize(data.getItems()),
            data.getCreationDate()
        );
    }

    public void deleteAfterExpire(long expireDays) {
        provider.executeUpdate(
            DELETE_AFTER_EXPIRE,
            expireDays
        );
    }

    public List<InventoryData> getByValue(UUID id) {
        final List<InventoryData> datas = provider.selectAsList(SELECT_ALL_DATA_SQL, function).join();

        return datas.stream()
            .sorted(Comparator.comparingLong(o -> o.getCreationDate().getTime()))
            .collect(Collectors.toList());
    }

    public InventoryData read(ResultSet resultSet) throws SQLException {
        return new InventoryData(
            UUID.fromString(resultSet.getString("id")),
            UUID.fromString(resultSet.getString("player_id")),
            ItemSerialization.deserialize(resultSet.getString("items")),
            resultSet.getTimestamp("created_at")
        );
    }

}
