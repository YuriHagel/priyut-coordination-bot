package ru.priyut.coordination.it.reminder;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.priyut.coordination.application.port.in.AssignVolunteerUseCase;
import ru.priyut.coordination.application.port.in.CreateTripUseCase;
import ru.priyut.coordination.application.port.out.TripRepository;
import ru.priyut.coordination.domain.trip.Trip;
import ru.priyut.coordination.infrastructure.scheduling.service.TripReminderService; // Адаптируйте под ваше имя сервиса
import ru.priyut.coordination.support.IntegrationTestBase;
import ru.priyut.coordination.test.fixture.TripFixture;
import ru.priyut.coordination.test.fixture.VolunteerFixture;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TripReminderIntegrationTest extends IntegrationTestBase {

    @Autowired
    private CreateTripUseCase createTripUseCase;

    @Autowired
    private AssignVolunteerUseCase assignVolunteerUseCase;

    @Autowired
    private TripReminderService tripReminderService; // Сервис, который вызывает @Scheduled задача

    @Autowired
    private TripRepository tripRepository;

    @MockBean
    private ru.priyut.coordination.application.port.out.TelegramGateway telegramGateway;

    @Test
    void shouldSend24HourReminderAndPreventDuplicates() {
        // given: Создаем поездку, которая начнется ровно через 24 часа (с небольшим запасом, чтобы попасть в диапазон запроса)
        var tripCommand = TripFixture.createCreateTripCommand(
                LocalDateTime.now().plusHours(24).plusMinutes(5),
                ru.priyut.coordination.domain.trip.TripType.REGULAR,
                "Клиника",
                "Задача"
        );
        Trip trip = createTripUseCase.createDraft(tripCommand, 1L);

        // Назначаем волонтера, чтобы было кому отправлять напоминание
        var volunteer = VolunteerFixture.createActiveVolunteer(111222333L);
        assignVolunteerUseCase.assign(trip.getId(), volunteer.getId(), 999L); // 999L - ID админа

        // when: Запускаем проверку напоминаний (эмулируем срабатывание @Scheduled)
        tripReminderService.checkAndSend24HourReminders();

        // then: Ждем асинхронной обработки (если она есть) и проверяем отправку
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(telegramGateway, times(1)).sendMessage(
                            org.mockito.ArgumentMatchers.eq(111222333L),
                            org.mockito.ArgumentMatchers.contains("⏰ НАПОМИНАНИЕ (за 24 часа)")
                    );
                });

        // then: Проверяем идемпотентность. Повторный запуск НЕ должен отправить второе сообщение
        tripReminderService.checkAndSend24HourReminders();

        // Общее количество вызовов должно остаться 1, а не 2
        verify(telegramGateway, times(1)).sendMessage(
                org.mockito.ArgumentMatchers.eq(111222333L),
                org.mockito.ArgumentMatchers.anyString()
        );

        // then: Проверяем, что в БД обновился флаг/время отправки напоминания
        Trip updatedTrip = tripRepository.findById(trip.getId()).orElseThrow();
        assertThat(updatedTrip.isReminder24hSent()).isTrue(); // Адаптируйте под имя вашего поля/метода
    }
}
