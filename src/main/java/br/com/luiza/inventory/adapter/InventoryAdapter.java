package br.com.luiza.inventory.adapter;

import br.com.luiza.inventory.model.InventoryData;
import br.com.luiza.inventory.utils.ItemSerialization;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class InventoryAdapter {

    public InventoryData read(ResultSet resultSet) throws SQLException {
        return new InventoryData(
          UUID.fromString(resultSet.getString("id")),
          UUID.fromString(resultSet.getString("player_id")),
          ItemSerialization.deserializeArray(resultSet.getString("items")),
          resultSet.getTimestamp("created_at")
        );
    }

    public void insert(PreparedStatement statement, InventoryData data) throws SQLException {
        final String base64 = ItemSerialization.serializeArray(data.getItems());

        statement.setString(1, data.getId().toString());
        statement.setString(2, data.getPlayerId().toString());

        statement.setString(3, base64);
        statement.setTimestamp(4, data.getCreatedAt());

        statement.setString(5, base64);
    }
}
