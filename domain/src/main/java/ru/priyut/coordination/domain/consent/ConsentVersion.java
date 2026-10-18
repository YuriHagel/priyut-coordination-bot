package ru.priyut.coordination.domain.consent;

import ru.priyut.coordination.domain.shared.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Версия политики обработки персональных данных.
 * Value object: неизменяемый, с валидацией формата.
 */
public record ConsentVersion(String version, String documentName) {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?$");

    public ConsentVersion {
        Objects.requireNonNull(version, "Consent version must not be null");
        Objects.requireNonNull(documentName, "Document name must not be null");

        version = version.trim();
        documentName = documentName.trim();

        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new DomainException(
                    "Invalid consent version format. Expected: X.Y or X.Y.Z, got: " + version);
        }
        if (documentName.isEmpty()) {
            throw new DomainException("Document name must not be empty");
        }
        if (documentName.length() > 200) {
            throw new DomainException("Document name is too long");
        }
    }

    public static ConsentVersion of(String version, String documentName) {
        return new ConsentVersion(version, documentName);
    }

    /**
     * Форматированное представление: "1.0 (Politika_PDN_Priyut.pdf)"
     */
    public String formatted() {
        return version + " (" + documentName + ")";
    }

    @Override
    public String toString() {
        return formatted();
    }
}