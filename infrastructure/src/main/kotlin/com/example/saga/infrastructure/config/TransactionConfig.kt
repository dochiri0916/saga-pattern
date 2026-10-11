package com.example.saga.infrastructure.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@AutoConfiguration(
    afterName = ["org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration"],
    beforeName = ["org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration"],
)
@ConditionalOnSingleCandidate(PlatformTransactionManager::class)
class TransactionConfig {
    @Bean
    @ConditionalOnMissingBean
    fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate =
        TransactionTemplate(transactionManager)
}
