package lotto

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertEquals // JUnit 5 assertEquals

// To make Application's methods testable, they should be 'internal' or tested via a public interface.
// We will assume 'calculateResults' and 'calculateProfitRate' are made 'internal'.

class ApplicationTest {

    private val application = Application() // Instance to call methods on

    companion object {
        @JvmStatic
        fun provideLottoResultsScenarios(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "1등 당첨 시나리오",
                    listOf(Lotto(listOf(1, 2, 3, 4, 5, 6))), // tickets
                    listOf(1, 2, 3, 4, 5, 6),    // winning numbers
                    7,                            // bonus number
                    mapOf(WinningRank.FIRST to 1) // expected results map
                ),
                Arguments.of(
                    "2등 당첨 시나리오",
                    listOf(Lotto(listOf(1, 2, 3, 4, 5, 10))), // 5 match + bonus
                    listOf(1, 2, 3, 4, 5, 6),     // winning numbers
                    10,                           // bonus number
                    mapOf(WinningRank.SECOND to 1)
                ),
                Arguments.of(
                    "3등 당첨 시나리오",
                    listOf(Lotto(listOf(1, 2, 3, 4, 5, 10))), // 5 match, no bonus
                    listOf(1, 2, 3, 4, 5, 6),     // winning numbers
                    7,                            // bonus (ticket has 10, bonus is 7)
                    mapOf(WinningRank.THIRD to 1)
                ),
                Arguments.of(
                    "4등 당첨 시나리오",
                    listOf(Lotto(listOf(1, 2, 3, 4, 10, 11))), // 4 match
                    listOf(1, 2, 3, 4, 5, 6),      // winning numbers
                    7,                             // bonus
                    mapOf(WinningRank.FOURTH to 1)
                ),
                Arguments.of(
                    "5등 당첨 시나리오",
                    listOf(Lotto(listOf(1, 2, 3, 10, 11, 12))), // 3 match
                    listOf(1, 2, 3, 4, 5, 6),       // winning
                    7,                              // bonus
                    mapOf(WinningRank.FIFTH to 1)
                ),
                Arguments.of(
                    "꽝 시나리오 (2개 일치)",
                    listOf(Lotto(listOf(1, 2, 10, 11, 12, 13))), // 2 match
                    listOf(1, 2, 3, 4, 5, 6),        // winning
                    7,                               // bonus
                    mapOf(WinningRank.MISS to 1) // Expect 1 MISS
                ),
                 Arguments.of(
                    "꽝 시나리오 (0개 일치)",
                    listOf(Lotto(listOf(10, 11, 12, 13, 14, 15))), // 0 match
                    listOf(1, 2, 3, 4, 5, 6),          // winning
                    7,                                 // bonus
                    mapOf(WinningRank.MISS to 1) // Expect 1 MISS
                ),
                Arguments.of(
                    "여러 티켓 혼합 시나리오",
                    listOf(
                        Lotto(listOf(1, 2, 3, 4, 5, 6)),    // 1st Prize
                        Lotto(listOf(1, 2, 3, 4, 5, 10)),   // 2nd Prize (bonus 10)
                        Lotto(listOf(1, 2, 3, 4, 11, 12)),  // 4th Prize
                        Lotto(listOf(10, 11, 12, 13, 14, 15)) // Miss
                    ),
                    listOf(1, 2, 3, 4, 5, 6),    // winning numbers
                    10,                           // bonus number (for 2nd prize)
                    mapOf(
                        WinningRank.FIRST to 1,
                        WinningRank.SECOND to 1,
                        WinningRank.FOURTH to 1,
                        WinningRank.MISS to 1
                    )
                )
            )
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideLottoResultsScenarios")
    @DisplayName("calculateResults는 다양한 시나리오에 대해 정확한 당첨 통계를 반환")
    fun `calculateResults returns correct statistics for various scenarios`(
        scenarioName: String,
        lottoTickets: List<Lotto>,
        winningNumbers: List<Int>,
        bonusNumber: Int,
        expectedResultsMap: Map<WinningRank, Int>
    ) {
        // This relies on calculateResults being 'internal' in Application.kt
        val actualResults = application.calculateResults(lottoTickets, winningNumbers, bonusNumber)
        
        // Check if all expected ranks are present and their counts match
        expectedResultsMap.forEach { (rank, expectedCount) ->
            assertEquals(expectedCount, actualResults.getOrDefault(rank, 0), "Count for $rank did not match.")
        }

        // Check if no unexpected ranks are present (i.e., their count is 0 or they are not in the map)
        (WinningRank.values().toSet() - expectedResultsMap.keys).forEach { rank ->
             assertEquals(0, actualResults.getOrDefault(rank, 0), "Rank $rank should have a count of 0 or be absent.")
        }
        
        // Ensure the sum of counts in actualResults matches the sum of counts in expectedResultsMap
        assertEquals(expectedResultsMap.values.sum(), actualResults.values.sum(), "Total count of ranked tickets does not match.")
    }

    @Test
    @DisplayName("calculateProfitRate는 구매 금액 대비 총 상금으로 정확한 수익률을 계산")
    fun `calculateProfitRate calculates correct profit rate`() {
        val results = mapOf(WinningRank.FIFTH to 1) // 1 ticket won 5th prize (5,000)
        val purchaseAmount = 8000
        // This relies on calculateProfitRate being 'internal'
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        assertEquals(62.5, profitRate, 0.01, "Profit rate for 5000 prize / 8000 cost should be 62.5%")
    }

    @Test
    @DisplayName("calculateProfitRate는 상금이 없을 때 0% 수익률을 반환")
    fun `calculateProfitRate returns 0 for no winnings`() {
        val results = mapOf(WinningRank.MISS to 2) // 2 tickets, no win
        val purchaseAmount = 2000
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        assertEquals(0.0, profitRate, 0.01, "Profit rate for no winnings should be 0.0%")
    }

    @Test
    @DisplayName("calculateProfitRate는 여러 당첨 건에 대해 정확한 수익률을 계산")
    fun `calculateProfitRate calculates correct profit rate for multiple wins`() {
        val results = mapOf(
            WinningRank.FIFTH to 2,  // 2 * 5,000 = 10,000
            WinningRank.FOURTH to 1 // 1 * 50,000 = 50,000
        ) // Total prize: 60,000
        val purchaseAmount = 100000 
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        assertEquals(60.0, profitRate, 0.01, "Profit rate for 60000 prize / 100000 cost should be 60.0%")
    }
     @Test
    @DisplayName("calculateProfitRate는 1등 당첨 시 수익률 계산")
    fun `calculateProfitRate for first prize`() {
        val results = mapOf(WinningRank.FIRST to 1) 
        val purchaseAmount = 1000 
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        val expectedProfitRate = (WinningRank.FIRST.prizeMoney.toDouble() / purchaseAmount) * 100
        assertEquals(expectedProfitRate, profitRate, 0.01)
    }

    @Test
    @DisplayName("calculateProfitRate는 투자금액이 0일때 Infinity를 반환해야함 (또는 예외처리)")
    fun `calculateProfitRate with zero purchase amount`() {
        val results = mapOf(WinningRank.FIFTH to 1)
        val purchaseAmount = 0
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        // Depending on requirements, this could be Infinity, or throw an exception.
        // Current implementation of Double / Int will give Infinity.
        assertEquals(Double.POSITIVE_INFINITY, profitRate, "Profit rate with 0 purchase and some winnings should be Infinity.")
    }

     @Test
    @DisplayName("calculateProfitRate는 투자금액과 상금이 모두 0일때 NaN 또는 0% (요구사항에 따라)")
    fun `calculateProfitRate with zero purchase and zero winnings`() {
        val results = mapOf(WinningRank.MISS to 1) // No prize money
        val purchaseAmount = 0
        val profitRate = application.calculateProfitRate(results, purchaseAmount)
        // 0.0/0.0 results in NaN for Doubles.
        // Depending on how this edge case should be handled (e.g. display as 0%), this test might change.
        assertEquals(Double.NaN, profitRate, "Profit rate with 0 purchase and 0 prize is NaN.")
    }
}
