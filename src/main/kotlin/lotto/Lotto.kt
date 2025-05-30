package lotto

class Lotto(private val numbers: List<Int>) {
    init {
        require(numbers.size == 6) { "[ERROR] Lotto must contain exactly 6 numbers." }
        validate(numbers)
    }

    private fun validate(numbers: List<Int>) {
        require(numbers.toSet().size == numbers.size) { "[ERROR] Lotto numbers must not contain duplicates." }
        numbers.forEach { number ->
            require(number in 1..45) { "[ERROR] Lotto numbers must be between 1 and 45." }
        }
    }

    fun getNumbers(): List<Int> {
        return numbers
    }
}
