package com.example.saga.order.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface StockSnapshotJpaRepository : JpaRepository<StockSnapshotEntity, UUID> {

    @Modifying
    @Query(
        value = """
            insert into stock_snapshots (stock_id, name, price, quantity, version)
            values (:stockId, :name, :price, :quantity, :version) as incoming
            on duplicate key update
                name = if(incoming.version > stock_snapshots.version, incoming.name, stock_snapshots.name),
                price = if(incoming.version > stock_snapshots.version, incoming.price, stock_snapshots.price),
                quantity = if(incoming.version > stock_snapshots.version, incoming.quantity, stock_snapshots.quantity),
                version = greatest(incoming.version, stock_snapshots.version)
        """,
        nativeQuery = true,
    )
    fun upsertIfNewer(stockId: UUID, name: String, price: Int, quantity: Int, version: Long): Int
}
