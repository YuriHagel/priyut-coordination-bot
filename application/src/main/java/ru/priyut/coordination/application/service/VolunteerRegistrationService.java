package ru.priyut.coordination.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.priyut.coordination.application.command.RegisterVolunteerCommand;
import ru.priyut.coordination.application.port.in.RegisterVolunteerUseCase;
import ru.priyut.coordination.application.port.outbound.ConsentRepository;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.application.port.outbound.VolunteerRepository;
import ru.priyut.coordination.domain.consent.Consent;
import ru.priyut.coordination.domain.consent.ConsentAuditEntry;
import ru.priyut.coordination.domain.consent.ConsentVersion;
import ru.priyut.coordination.domain.shared.UtcClock;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerProfile;

@Service
@Transactional
public class VolunteerRegistrationService implements RegisterVolunteerUseCase {

    private final VolunteerRepository volunteerRepository;
    private final ConsentRepository consentRepository;
    private final TelegramGateway telegramGateway;
    private final UtcClock clock;

    public VolunteerRegistrationService(
            VolunteerRepository volunteerRepository,
            ConsentRepository consentRepository,
            TelegramGateway telegramGateway,
            UtcClock clock
    ) {
        this.volunteerRepository = volunteerRepository;
        this.consentRepository = consentRepository;
        this.telegramGateway = telegramGateway;
        this.clock = clock;
    }

    @Override
    public Volunteer register(RegisterVolunteerCommand command) {
        // 1. Создаём профиль волонтёра
        var profile = VolunteerProfile.of(
                command.telegramId(),
                command.fullName(),
                command.phone(),
                command.district(),
                command.car(),
                command.availability()
        );

        // 2. Регистрируем волонтёра (статус PENDING_MODERATION)
        var volunteer = Volunteer.register(profile, clock.now());
        volunteer = volunteerRepository.save(volunteer);

        // 3. Фиксируем согласие на ПДн
        var consentVersion = ConsentVersion.of(command.consentVersion(), "Politika_PDN_Priyut.pdf");
        var consent = Consent.grant(command.telegramId(), consentVersion, clock.now());
        consentRepository.save(consent);

        // 4. Записываем в журнал аудита
        var auditEntry = ConsentAuditEntry.grant(
                command.telegramId(),
                consentVersion,
                clock.now(),
                "TELEGRAM_BOT"
        );
        consentRepository.saveAuditEntry(auditEntry);

        // 5. Уведомляем админов о новой анкете
        notifyAdminsAboutNewVolunteer(volunteer);

        return volunteer;
    }

    private void notifyAdminsAboutNewVolunteer(Volunteer volunteer) {
        // TODO: Получить список админов из AdminWhitelistRepository
        // и отправить им уведомление
        var message = String.format(
                "⚠ Новая анкета на модерации!\n\n" +
                        "👤 %s\n" +
                        "📞 %s\n" +
                        "📍 %s\n" +
                        "🚗 %s\n" +
                        "⏰ %s",
                volunteer.getFullName(),
                volunteer.getPhone().masked(),
                volunteer.getDistrict().name(),
                volunteer.getCar().formatted(),
                volunteer.getAvailability().formatted()
        );
        // telegramGateway.broadcastMessage(message, adminTelegramIds);
    }
}