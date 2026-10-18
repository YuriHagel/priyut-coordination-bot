package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.application.command.RegisterVolunteerCommand;
import ru.priyut.coordination.domain.volunteer.Volunteer;

/**
 * Сценарий регистрации нового волонтёра (ТЗ 4.1).
 * Включает фиксацию согласия на обработку ПДн.
 */
public interface RegisterVolunteerUseCase {

    /**
     * Регистрация волонтёра с фиксацией согласия на ПДн.
     *
     * @param command данные анкеты
     * @return созданный волонтёр со статусом PENDING_MODERATION
     */
    Volunteer register(RegisterVolunteerCommand command);
}