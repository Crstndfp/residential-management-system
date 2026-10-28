package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CalendarSlotDto;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.model.Booking;
import com.residencial.amenities.domain.repository.AmenityRepository;
import com.residencial.amenities.domain.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAmenityCalendarUseCaseTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private BookingRepository bookingRepository;

    private GetAmenityCalendarUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAmenityCalendarUseCase(amenityRepository, bookingRepository);
    }

    @Test
    @DisplayName("US-302 Privacy: Calendar slots must show strictly 'Reservado' without resident names or event types")
    void shouldReturnProtectedCalendarSlots() {
        UUID amenityId = UUID.randomUUID();
        Amenity amenity = new Amenity(
                "GAZEBO", "Kiosco", "Area BBQ", 40,
                new BigDecimal("150.00"), new BigDecimal("100.00"),
                LocalTime.of(9, 0), LocalTime.of(20, 0), "BARBECUE"
        );

        LocalDate date = LocalDate.now().plusDays(2);
        Booking booking = new Booking(
                amenity, UUID.randomUUID(), "D-404", UUID.randomUUID(),
                "Secret Resident Name", "BARBECUE",
                date, LocalTime.of(12, 0), LocalTime.of(16, 0),
                new BigDecimal("150.00"), new BigDecimal("100.00"), UUID.randomUUID()
        );

        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(amenity));
        when(bookingRepository.findByAmenityIdAndBookingDateBetween(amenity.getId(), date, date.plusDays(7)))
                .thenReturn(List.of(booking));

        List<CalendarSlotDto> slots = useCase.execute(amenityId, date, date.plusDays(7));

        assertNotNull(slots);
        assertEquals(1, slots.size());
        assertEquals("Reservado", slots.getFirst().displayStatus());
        assertEquals(LocalTime.of(12, 0), slots.getFirst().startTime());
        assertEquals(LocalTime.of(16, 0), slots.getFirst().endTime());
    }
}
