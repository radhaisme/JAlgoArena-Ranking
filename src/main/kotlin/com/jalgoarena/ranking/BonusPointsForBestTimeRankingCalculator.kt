package com.jalgoarena.ranking

import com.jalgoarena.domain.*
import com.jalgoarena.ranking.RankingCalculator.Companion.acceptedWithBestTimes
import com.jalgoarena.web.SubmissionsClient

class BonusPointsForBestTimeRankingCalculator(
        private val submissionsClient : SubmissionsClient,
        private val rankingCalculator: RankingCalculator
) : RankingCalculator {

    override fun ranking(users: List<User>, submissions: List<Submission>, problems: List<Problem>): List<RankEntry> {

        val bonusPoints = calculateBonusPointsForFastestSolutions(submissions, users)

        return rankingCalculator.ranking(users, submissions, problems).map { rankEntry ->
            val id = users.firstOrNull { it.username == rankEntry.hacker }?.id ?: ""

            RankEntry(
                    rankEntry.hacker,
                    rankEntry.score + bonusPoints[id] as Double,
                    rankEntry.solvedProblems,
                    rankEntry.region,
                    rankEntry.team
            )
        }.sortedByDescending { it.score }
    }

    override fun problemRanking(problemId: String, users: List<User>, problems: List<Problem>): List<ProblemRankEntry> {
        val problemSubmissions = acceptedWithBestTimes(submissionsClient.findByProblemId(problemId))

        val bonusPoints = calculateBonusPointsForFastestSolutions(problemSubmissions, users)

        return rankingCalculator.problemRanking(problemId, users, problems).map { problemRankEntry ->
            val user = users.first { it.username == problemRankEntry.hacker }

            ProblemRankEntry(
                    problemRankEntry.hacker,
                    problemRankEntry.score + bonusPoints[user.id] as Double,
                    problemRankEntry.elapsedTime
            )
        }.sortedBy { it.elapsedTime }
    }

    private fun calculateBonusPointsForFastestSolutions(submissions: List<Submission>, users: List<User>): Map<String, Double> {

        val bonusPoints = mutableMapOf<String, Double>()
        users.forEach { bonusPoints[it.id] = 0.0 }

        val problems = submissions.map { it.problemId }.distinct()

        problems.forEach { problem ->
            val problemSubmissions = submissions.filter { it.problemId == problem }
            val fastestSubmission = problemSubmissions.minBy { it.elapsedTime }

            if (fastestSubmission != null && bonusPoints[fastestSubmission.userId] != null) {
                bonusPoints[fastestSubmission.userId] = bonusPoints[fastestSubmission.userId] as Double + 1.0
            }
        }

        return bonusPoints
    }
}
