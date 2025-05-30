package lotto

import org.junit.jupiter.api.Test // This one is fine
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
// Removed redundant import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.assertEquals // JUnit 5 assertEquals

class LottoTest {

    @Test
    @DisplayName("Lotto 생성 시 번호가 6개가 아니면 (너무 적거나 많으면) 예외 발생")
    fun `constructor throws if numbers list size is not 6`() {
        assertThrows<IllegalArgumentException>("Exactly 6 numbers are required") {
            Lotto(listOf(1, 2, 3, 4, 5))
        }
        assertThrows<IllegalArgumentException>("Exactly 6 numbers are required") {
            Lotto(listOf(1, 2, 3, 4, 5, 6, 7))
        }
    }

    @Test
    @DisplayName("Lotto 생성 시 중복된 번호가 있으면 예외 발생")
    fun `constructor throws if numbers have duplicates`() {
        assertThrows<IllegalArgumentException>("Duplicate numbers are not allowed") {
            Lotto(listOf(1, 2, 3, 4, 5, 5))
        }
    }

    @ParameterizedTest(name = "번호 {0}은(는) 범위를 벗어남")
    @ValueSource(ints = [0, 46])
    @DisplayName("Lotto 생성 시 번호가 범위를 벗어나면 (1-45) 예외 발생")
    fun `constructor throws if numbers are out of range`(invalidNumber: Int) {
        assertThrows<IllegalArgumentException>("Numbers must be between 1 and 45") {
            Lotto(listOf(1, 2, 3, 4, 5, invalidNumber))
        }
    }
    
    @Test
    @DisplayName("Lotto 생성 시 모든 번호가 유효하면 정상 생성 및 getNumbers 확인")
    fun `constructor creates Lotto successfully and getNumbers works`() {
        val validNumbers = listOf(1, 2, 3, 4, 5, 6)
        val lotto = Lotto(validNumbers)
        assertEquals(validNumbers, lotto.getNumbers(), "getNumbers should return the initial numbers")
    }

    @Test
    @DisplayName("getNumbers는 생성자에게 전달된 정렬되지 않은 번호 목록을 그대로 반환")
    fun `getNumbers returns the correct unsorted list of numbers`() {
        val expectedNumbers = listOf(40, 1, 33, 22, 8, 45)
        val lotto = Lotto(expectedNumbers)
        // Lotto class itself does not sort, it stores them as is.
        assertEquals(expectedNumbers, lotto.getNumbers(), "getNumbers should return the numbers in the order they were provided")
    }
}
