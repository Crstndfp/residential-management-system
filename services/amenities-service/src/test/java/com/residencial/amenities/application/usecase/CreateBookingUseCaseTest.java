package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.BookingResponse;
import com.residencial.amenities.application.dto.CreateBookingRequest;
import com.residencial.amenities.domain.exception.BookingConflictException;
import com.residencial.amenities.domain.exception.DelinquentHouseException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.model.Booking;
import com.residencial.amenities.domain.model.BookingStatus;
import com.residencial.amenities.domain.port.HouseSolvencyPort;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBookingUseCaseTest {

    @Mock
    private AmenityRepository amenityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HouseSolvencyPort houseSolvencyPort;

    private CreateBookingUseCase useCase;

    private Amenity clubhouse;

    @BeforeEach
    void setUp() {
        useCase = new CreateBookingUseCase(amenityRepository, bookingRepository, houseSolvencyPort);
        clubhouse = new Amenity(
                "CLUB_HOUSE",
                "Casa Club",
                "Salon principal",
                120,
                new BigDecimal("500.00"),
                new BigDecimal("300.00"),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                "BIRTHDAY,WEDDING,FAMILY_GATHERING"
        );
    }

    @Test
    @DisplayName("US-302: Successfully create booking when house is solvent and slot is available")
    void shouldCreateBookingSuccessfully() {
        UUID houseId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(5);
        CreateBookingRequest request = new CreateBookingRequest(
                clubhouse.getId(),
                houseId,
                "A-101",
                UUID.randomUUID(),
                "Carlos Gómez",
                "BIRTHDAY",
                date,
                LocalTime.of(14, 0),
                LocalTime.of(18, 0)
        );

        when(amenityRepository.findById(clubhouse.getId())).thenReturn(Optional.of(clubhouse));
        when(houseSolvencyPort.isHouseSolvent(houseId)).thenReturn(true);
        when(bookingRepository.findByAmenityIdAndBookingDate(clubhouse.getId(), date)).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("A-101", response.houseNumber());
        assertEquals("BIRTHDAY", response.eventType());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(new BigDecimal("500.00"), response.totalCost());
        assertNotNull(response.financeChargeId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("US-302 Invariant: Reject booking when house is delinquent")
    void shouldRejectWhenHouseIsDelinquent() {
        UUID houseId = UUID.randomUUID();
        CreateBookingRequest request = new CreateBookingRequest(
                clubhouse.getId(),
                houseId,
                "B-202",
                UUID.randomUUID(),
                "Inquilino Moroso",
                "BIRTHDAY",
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                LocalTime.of(13, 0)
        );

        when(amenityRepository.findById(clubhouse.getId())).thenReturn(Optional.of(clubhouse));
        when(houseSolvencyPort.isHouseSolvent(houseId)).thenReturn(false);

        assertThrows(DelinquentHouseException.class, () -> useCase.execute(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-302 Invariant: Reject booking when slot overlaps with existing booking")
    void shouldRejectWhenSlotOverlaps() {
        UUID houseId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(4);

        Booking existingBooking = new Booking(
                clubhouse,
                UUID.randomUUID(),
                "C-303",
                UUID.randomUUID(),
                "Otro Residente",
                "FAMILY_GATHERING",
                date,
                LocalTime.of(14, 0),
                LocalTime.of(19, 0),
                new BigDecimal("500.00"),
                new BigDecimal("300.00"),
                UUID.randomUUID()
        );

        CreateBookingRequest overlappingRequest = new CreateBookingRequest(
                clubhouse.getId(),
                houseId,
                "A-101",
                UUID.randomUUID(),
                "Carlos Gómez",
                "BIRTHDAY",
                date,
                LocalTime.of(16, 0), // Overlaps between 14:00 and 19:00
                LocalTime.of(21, 0)
        );

        when(amenityRepository.findById(clubhouse.getId())).thenReturn(Optional.of(clubhouse));
        when(houseSolvencyPort.isHouseSolvent(houseId)).thenReturn(true);
        when(bookingRepository.findByAmenityIdAndBookingDate(clubhouse.getId(), date)).thenReturn(List.of(existingBooking));

        assertThrows(BookingConflictException.class, () -> useCase.execute(overlappingRequest));
        verify(bookingRepository, never()).save(any());
    }
}
