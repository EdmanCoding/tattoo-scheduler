import com.tattoo.scheduler.domain.Booking;
import com.tattoo.scheduler.model.SessionType;
import com.tattoo.scheduler.service.policy.impl.NikitaBookingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NikitaBookingPolicyTest {
    private final NikitaBookingPolicy policy = new NikitaBookingPolicy();
    private static final LocalDateTime BASE_DATE_TIME = LocalDateTime.of(2026, 6, 26, 10, 0);

    @Test
    public void isDateAllowed_shouldReturnTrue_WhenDateIsTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);   // Arrange
        boolean result = policy.isDateAllowed(tomorrow);    // Act
        assertThat(result).isTrue();                        // Assert
    }
    @Test
    public void isDateAllowed_shouldReturnFalse_WhenDateIsToday() {
        LocalDate today = LocalDate.now();
        boolean result = policy.isDateAllowed(today);
        assertThat(result).isFalse();
    }
    @Test
    public void isDateAllowed_shouldReturnFalse_WhenDateIsMonthAgo() {
        LocalDate monthAgo = LocalDate.now().minusMonths(1);
        boolean result = policy.isDateAllowed(monthAgo);
        assertThat(result).isFalse();
    }
    @Test
    public void isDateAllowed_shouldReturnTrue_WhenDateIsMonthAhead(){
        LocalDate monthAhead = LocalDate.now().plusMonths(1);
        boolean result = policy.isDateAllowed(monthAhead);
        assertThat(result).isTrue();
    }

    @Test
    public void isWithinWorkingHours_shouldReturnTrue_WhenEndOfSessionExactlyAt20hours() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 4, 15, 16, 0);
        SessionType type = SessionType.MEDIUM;
        // Act
        boolean result = policy.isWithinWorkingHours(start, type);
        // Assert
        assertThat(result).isTrue();
    }
    @Test
    public void isWithinWorkingHours_shouldReturnFalse_WhenEndOfSessionCrosses20hours() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 15, 16, 1);
        SessionType type = SessionType.MEDIUM;
        boolean result = policy.isWithinWorkingHours(start, type);
        assertThat(result).isFalse();
    }
    @Test
    public void isWithinWorkingHours_shouldReturnTrue_WhenStartIsExactly10() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 15, 10, 0);
        SessionType type = SessionType.LARGE_CONSULTATION;
        boolean result = policy.isWithinWorkingHours(start, type);
        assertThat(result).isTrue();
    }
    @Test
    public void isWithinWorkingHours_shouldReturnFalse_WhenStartIsLessThan10() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 15, 9, 59);
        SessionType type = SessionType.LARGE_CONSULTATION;
        boolean result = policy.isWithinWorkingHours(start, type);
        assertThat(result).isFalse();
    }
}