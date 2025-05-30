package lotto

import camp.nextstep.edu.missionutils.Console
import camp.nextstep.edu.missionutils.Randoms

fun main() {
    // Wrapped the main execution in a try-catch to ensure all IllegalArgumentExceptions
    // print their message (which includes "[ERROR]") and allow NsTest to capture it.
    try {
        Application().run()
    } catch (e: IllegalArgumentException) {
        println(e.message) 
    }
}

class Application {
    fun run() {
        val purchaseAmount = getPurchaseAmount()
        val numberOfTickets = purchaseAmount / 1000
        // Adjusted to match feature test output: "You have purchased 8 tickets."
        println("\nYou have purchased ${numberOfTickets} tickets.")

        val lottoTickets = generateLottoTickets(numberOfTickets)
        lottoTickets.forEach { ticket ->
            // Adjusted to match feature test output: "[8, 21, 23, 41, 42, 43]" (standard List.toString())
            println(ticket.getNumbers().sorted().toString())
        }

        val winningNumbers = getWinningNumbers()
        val bonusNumber = getBonusNumber(winningNumbers)

        val results = calculateResults(lottoTickets, winningNumbers, bonusNumber)
        val profitRate = calculateProfitRate(results, purchaseAmount)

        printResults(results, profitRate)
    }

    private fun getPurchaseAmount(): Int {
        println("구입금액을 입력해 주세요.")
        val amount = Console.readLine()?.toIntOrNull() 
            ?: throw IllegalArgumentException("[ERROR] Purchase amount must be a valid number.")
        if (amount % 1000 != 0) {
            throw IllegalArgumentException("[ERROR] Purchase amount must be a multiple of 1000.")
        }
        return amount
    }

    private fun generateLottoTickets(count: Int): List<Lotto> {
        return List(count) {
            Lotto(Randoms.pickUniqueNumbersInRange(1, 45, 6))
        }
    }

    private fun getWinningNumbers(): List<Int> {
        println("\n당첨 번호를 입력해 주세요.")
        val input = Console.readLine() ?: throw IllegalArgumentException("[ERROR] Winning numbers input cannot be empty.")
        val numbers = input.split(',').map {
            it.trim().toIntOrNull() ?: throw IllegalArgumentException("[ERROR] Winning numbers must be valid numbers.")
        }

        require(numbers.size == 6) { "[ERROR] Winning numbers must contain exactly 6 numbers." }
        require(numbers.toSet().size == numbers.size) { "[ERROR] Winning numbers must not contain duplicates." }
        numbers.forEach { number ->
            require(number in 1..45) { "[ERROR] Winning numbers must be between 1 and 45." }
        }
        return numbers.sorted()
    }

    private fun getBonusNumber(winningNumbers: List<Int>): Int {
        println("\n보너스 번호를 입력해 주세요.")
        val input = Console.readLine() ?: throw IllegalArgumentException("[ERROR] Bonus number input cannot be empty.")
        val number = input.toIntOrNull() ?: throw IllegalArgumentException("[ERROR] Bonus number must be a valid number.")

        require(number in 1..45) { "[ERROR] Bonus number must be between 1 and 45." }
        require(number !in winningNumbers) { "[ERROR] Bonus number must not be one of the winning numbers." }
        
        return number
    }

    internal fun calculateResults( // Changed from private to internal
        lottoTickets: List<Lotto>,
        winningNumbers: List<Int>,
        bonusNumber: Int
    ): Map<WinningRank, Int> {
        val results = mutableMapOf<WinningRank, Int>().withDefault { 0 }
        for (ticket in lottoTickets) {
            val matchCount = ticket.getNumbers().intersect(winningNumbers.toSet()).size
            val bonusMatch = ticket.getNumbers().contains(bonusNumber)

            val rank = when (matchCount) {
                6 -> WinningRank.FIRST
                5 -> if (bonusMatch) WinningRank.SECOND else WinningRank.THIRD
                4 -> WinningRank.FOURTH
                3 -> WinningRank.FIFTH
                else -> WinningRank.MISS
            }
            results[rank] = results.getValue(rank) + 1
        }
        return results
    }

    internal fun calculateProfitRate(results: Map<WinningRank, Int>, purchaseAmount: Int): Double { // Changed from private to internal
        val totalPrizeMoney = results.entries.sumOf { (rank, count) ->
            rank.prizeMoney * count
        }
        if (purchaseAmount == 0) {
            return if (totalPrizeMoney > 0) Double.POSITIVE_INFINITY else Double.NaN // Handle division by zero
        }
        return (totalPrizeMoney.toDouble() / purchaseAmount) * 100
    }

    private fun printResults(results: Map<WinningRank, Int>, profitRate: Double) {
        // Feature test expects "당첨 통계"
        println("\n당첨 통계") // This matches current output, no change needed.
        println("---") // This matches current output, no change needed.

        // Order: 5th, 4th, 3rd, 2nd, 1st as per typical Lotto result printouts
        // and likely what the feature test implies by its ordered list of expectations.
        val ranksToPrint = listOf(WinningRank.FIFTH, WinningRank.FOURTH, WinningRank.THIRD, WinningRank.SECOND, WinningRank.FIRST)

        ranksToPrint.forEach { rank ->
            val count = results.getOrDefault(rank, 0)
            // Using WinningRank.descriptionText and getFormattedPrize with "KRW"
            // Feature test format: "3 Matches (5,000 KRW) – 1 tickets"
            println("${rank.descriptionText} (${rank.getFormattedPrize("KRW")}) – ${count} tickets")
        }
        // Feature test expects: "Total return rate is 62.5%."
        // Note the trailing period.
        println("Total return rate is ${String.format("%.1f", profitRate)}%.")
    }
}

// Changed description to be a direct string property 'descriptionText'
// and getFormattedPrize to accept currency.
enum class WinningRank(val prizeMoney: Long, val descriptionText: String) {
    FIRST(2_000_000_000L, "6 Matches"),
    SECOND(30_000_000L, "5 Matches + Bonus Ball"),
    THIRD(1_500_000L, "5 Matches"),
    FOURTH(50_000L, "4 Matches"),
    FIFTH(5_000L, "3 Matches"),
    MISS(0L, ""); // MISS is not printed in the statistics

    // Method to get prize money formatted with currency string
    fun getFormattedPrize(currency: String = "원"): String {
        val separator = if (currency == "KRW") " " else "" // Add space for KRW before unit
        return String.format("%,d%s%s", prizeMoney, separator, currency)
    }
}
