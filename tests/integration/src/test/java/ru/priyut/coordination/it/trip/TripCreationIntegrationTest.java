package ru.priyut.coordination.it.trip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.priyut.coordination.application.port.in.CreateTripUseCase;
import ru.priyut.coordination.application.port.in.PublishTripUseCase;
import ru.priyut.coordination.application.port.out.TripRepository;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.domain.trip.TripStatus;
import ru.priyut.coordination.domain.trip.TripType;
import ru.priyut.coordination.support.IntegrationTestBase;
import ru.priyut.coordination.test.fixture.TripFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class TripCreationIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CreateTripUseCase createTripUseCase;

    @Autowired
    private PublishTripUseCase publishTripUseCase;

    @Autowired
    private TripRepository tripRepository;

    @MockBean
    private ru.priyut.coordination.application.port.out.TelegramGateway telegramGateway;

    @Test
    void shouldCreateDraftAndPublishTripSuccessfully() {
        // given: Админ создает черновик срочной поездки
        Long adminId = 987654321L;
        var createCommand = TripFixture.createCreateTripCommand(
                LocalDateTime.now().plusDays(1),
                TripType.URGENT,
                "ВетКлиника «Друг»",
                "Кот Барсик, стерилизация"
        );

        Trip draftTrip = createTripUseCase.createDraft(createCommand, adminId);
        assertThat(draftTrip.getStatus()).isEqualTo(TripStatus.DRAFT);

        // when: Админ публикует поездку
        publishTripUseCase.publish(draftTrip.getId(), adminId);

        // then: Статус поездки меняется на PUBLISHED
        Trip publishedTrip = tripRepository.findById(draftTrip.getId())
                .orElseThrow(() -> new AssertionError("Trip not found"));

        assertThat(publishedTrip.getStatus()).isEqualTo(TripStatus.PUBLISHED);
        assertThat(publishedTrip.getPublishedAt()).isNotNull();

        // then: Проверяем, что рассылка волонтерам была инициирована
        verify(telegramGateway).broadcastMessage(contains("🚨 СРОЧНАЯ заявка"), contains("Кот Барсик"));
    }
}
