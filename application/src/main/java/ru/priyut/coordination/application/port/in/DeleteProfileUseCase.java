package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Сценарий удаления персональных данных (ТЗ 4.8, 152-ФЗ).
 */
public interface DeleteProfileUseCase {

    /**
     * Запрос на удаление профиля волонтёра.
     *
     * @param telegramId Telegram ID волонтёра
     */
    void requestDeletion(TelegramId telegramId);

    /**
     * Фактическое удаление персональных данных (выполняется фоново).
     *
     * @param telegramId Telegram ID волонтёра
     */
    void deletePersonalData(TelegramId telegramId);
}