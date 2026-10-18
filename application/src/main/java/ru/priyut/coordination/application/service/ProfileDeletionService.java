package ru.priyut.coordination.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.priyut.coordination.application.exception.VolunteerNotFoundException;
import ru.priyut.coordination.application.port.in.DeleteProfileUseCase;
import ru.priyut.coordination.application.port.outbound.ConsentRepository;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.Volunteer;

@Service
@Transactional
public class ProfileDeletionService implements DeleteProfileUseCase {

    private final VolunteerRepository volunteerRepository;
    private final ConsentRepository consentRepository;
    private final UtcClock clock;

    public ProfileDeletionService(
            VolunteerRepository volunteerRepository,
            ConsentRepository consentRepository,
            UtcClock clock
    ) {
        this.volunteerRepository = volunteerRepository;
        this.consentRepository = consentRepository;
        this.clock = clock;
    }

    @Override
    public void requestDeletion(TelegramId telegramId) {
        var volunteer = findVolunteerOrThrow(telegramId);
        volunteer.requestDeletion(clock.now());
        volunteerRepository.save(volunteer);

        // Записываем в журнал аудита
        var auditEntry = ConsentAuditEntry.of(
                telegramId,
                ConsentAuditEntry.AuditAction.PROFILE_DELETION_REQUESTED,
                null,
                clock.now(),
                "TELEGRAM_BOT",
                null
        );
        consentRepository.saveAuditEntry(auditEntry);
    }

    @Override
    public void deletePersonalData(TelegramId telegramId) {
        var volunteer = findVolunteerOrThrow(telegramId);
        volunteer.deletePersonalData(clock.now());
        volunteerRepository.save(volunteer);

        // Записываем в журнал аудита
        var auditEntry = ConsentAuditEntry.of(
                telegramId,
                ConsentAuditEntry.AuditAction.PROFILE_DELETED,
                null,
                clock.now(),
                "BACKGROUND_JOB",
                null
        );
        consentRepository.saveAuditEntry(auditEntry);
    }

    private Volunteer findVolunteerOrThrow(TelegramId telegramId) {
        return volunteerRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new VolunteerNotFoundException(telegramId.value()));
    }
}