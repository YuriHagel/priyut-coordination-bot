package ru.priyut.coordination.it.volunteer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.priyut.coordination.application.port.in.RegisterVolunteerUseCase;
import ru.priyut.coordination.application.port.out.ConsentRepository;
import ru.priyut.coordination.application.port.out.VolunteerRepository;
import ru.priyut.coordination.domain.volunteer.Volunteer;
import ru.priyut.coordination.domain.volunteer.VolunteerStatus;
import ru.priyut.coordination.support.IntegrationTestBase;
import ru.priyut.coordination.test.fixture.VolunteerFixture;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class VolunteerRegistrationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private RegisterVolunteerUseCase registerVolunteerUseCase;

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private ConsentRepository consentRepository;

    @MockBean
    private ru.priyut.coordination.application.port.out.TelegramGateway telegramGateway;

    @Test
    void shouldRegisterVolunteerAndSaveConsent() {
        // given: Пользователь нажал "Зарегистрироваться" после ознакомления с PDF
        Long telegramId = 123456789L;
        String policyVersion = "1.0";

        var command = VolunteerFixture.createRegistrationCommand(telegramId);

        // when: Выполняем use-case регистрации
        registerVolunteerUseCase.register(command, policyVersion, Instant.now());

        // then: Проверяем, что волонтер сохранен со статусом "На модерации"
        Volunteer savedVolunteer = volunteerRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new AssertionError("Volunteer not found"));

        assertThat(savedVolunteer.getStatus()).isEqualTo(VolunteerStatus.PENDING_MODERATION);
        assertThat(savedVolunteer.getFullName()).isEqualTo(command.fullName());

        // then: Проверяем, что согласие на ПДн зафиксировано в БД (Требование 152-ФЗ)
        var consents = consentRepository.findByVolunteerId(savedVolunteer.getId());
        assertThat(consents).hasSize(1);
        assertThat(consents.get(0).getConsentVersion()).isEqualTo(policyVersion);
        assertThat(consents.get(0).getGrantedAt()).isNotNull();

        // then: Проверяем, что админ был уведомлен (через мок шлюза)
        verify(telegramGateway).sendMessage(anyLong(), contains("Новая анкета на модерации"));
    }
}
