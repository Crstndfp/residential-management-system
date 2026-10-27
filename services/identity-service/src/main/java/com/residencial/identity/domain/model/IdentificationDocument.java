package com.residencial.identity.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class IdentificationDocument {

    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[A-Za-z0-9]{5,20}$");

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false, length = 20)
    private IdentificationType type;

    @Column(name = "id_number", nullable = false, length = 30)
    private String number;

    protected IdentificationDocument() {
        // JPA constructor
    }

    public IdentificationDocument(IdentificationType type, String number) {
        if (type == null) {
            throw new IllegalArgumentException("Identification type cannot be null.");
        }
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Identification number cannot be empty.");
        }
        String cleaned = number.trim().replaceAll("[\\s-]", "");
        if (type == IdentificationType.DPI) {
            validateDpi(cleaned);
        } else if (type == IdentificationType.PASSPORT) {
            validatePassport(cleaned);
        }
        this.type = type;
        this.number = cleaned;
    }

    /**
     * Validates Guatemalan CUI/DPI (13 digits):
     * Positions 1-8: correlative number.
     * Position 9: checksum digit calculated using Modulo 11 with weights 2, 3, 4, 5, 6, 7, 8, 9 from right to left.
     * Positions 10-11: department code (01-22).
     * Positions 12-13: municipality code.
     */
    private static void validateDpi(String dpi) {
        if (dpi.length() != 13 || !dpi.matches("\\d{13}")) {
            throw new IllegalArgumentException("DPI/CUI must contain exactly 13 digits.");
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            int digit = Character.getNumericValue(dpi.charAt(i));
            sum += digit * (i + 2);
        }

        int mod = sum % 11;
        int expectedVerifier = (11 - mod) % 11;
        int actualVerifier = Character.getNumericValue(dpi.charAt(8));

        if (actualVerifier != expectedVerifier) {
            throw new IllegalArgumentException("Invalid DPI/CUI checksum (Modulo 11 verification failed).");
        }

        int deptCode = Integer.parseInt(dpi.substring(9, 11));
        if (deptCode < 1 || deptCode > 22) {
            throw new IllegalArgumentException("Invalid department code in DPI/CUI. Must be between 01 and 22.");
        }
    }

    private static void validatePassport(String passport) {
        if (!PASSPORT_PATTERN.matcher(passport).matches()) {
            throw new IllegalArgumentException("Passport number must be between 5 and 20 alphanumeric characters.");
        }
    }

    public IdentificationType getType() {
        return type;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentificationDocument that = (IdentificationDocument) o;
        return type == that.type && Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}
