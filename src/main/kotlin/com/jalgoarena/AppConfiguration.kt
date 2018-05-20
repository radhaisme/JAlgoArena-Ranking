package com.jalgoarena

import com.jalgoarena.data.SubmissionsRepository
import com.jalgoarena.ranking.*
import com.jalgoarena.web.ProblemsClient
import com.jalgoarena.web.SubmissionsClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Configuration
open class AppConfiguration {

    @Bean
    open fun rankingCalculator(
            submissionsRepository : SubmissionsRepository,
            problemsClient: ProblemsClient,
            submissionsClient: SubmissionsClient
    ): RankingCalculator {
        val scoreCalculator = BasicScoreCalculator()
        val rankingCalculator = BasicRankingCalculator(submissionsClient, submissionsRepository, scoreCalculator)

        return BonusPointsForBestTimeRankingCalculator(
                submissionsRepository, rankingCalculator
        )
    }

    @Bean
    open fun restTemplate(): RestOperations = RestTemplate()
}
