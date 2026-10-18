package ru.priyut.coordination.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.priyut.coordination.application.exception.VolunteerNotFoundException;
import ru.priyut.coordination.application.port.in.WithdrawConsentUseCase;
import ru.priyut.coordination.application.port.outbound.ConsentRepository;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.domain.shared.UtcClock;

@Service
@Transactional
public class ConsentService implements WithdrawConsentUseCase {

    private final ConsentRepository consentRepository;
    private final VolunteerRepository volunteerRepository;
    private final UtcClock clock;

    public ConsentService(
            ConsentRepository consentRepository,
            VolunteerRepository volunteerRepository,
            UtcClock clock
    ) {
        this.consentRepository = consentRepository;
        this.volunteerRepository = volunteerRepository;
        this.clock = clock;
    }

    @Override
    public void withdrawConsent(TelegramId telegramId, String reason) {
        // Проверяем, что волонтёр существует
        volunteerRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new VolunteerNotFoundException(telegramId.value()));

        // Находим согласие
        var consent = consentRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalStateException(
                        "Consent not found for telegram ID: " + telegramId));

        // Отзываем согласие
        consent.withdraw(reason, clock.now());
        consentRepository.save(consent);

        // Записываем в журнал аудита
        var auditEntry = ConsentAuditEntry.withdraw(
                telegramId,
                consent.getCurrentVersion(),
                clock.now(),
                "TELEGRAM_BOT",
                reason
        );
        consentRepository.saveAuditEntry(auditEntry);
    }
}