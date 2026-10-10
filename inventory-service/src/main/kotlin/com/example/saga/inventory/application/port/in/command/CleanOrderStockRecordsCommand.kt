package com.example.saga.inventory.application.port.`in`.command

import java.time.Duration

data class CleanOrderStockRecordsCommand(

    val retention: Duration,

    val decreasedRetention: Duration,

    val limit: Int,
)
