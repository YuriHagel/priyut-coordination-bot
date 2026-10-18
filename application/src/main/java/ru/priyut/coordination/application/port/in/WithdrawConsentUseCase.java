package ru.priyut.coordination.application.port.in;

import ru.priyut.coordination.domain.shared.TelegramId;

/**
 * Сценарий отзыва согласия на обработку ПДн (ТЗ 4.8, 152-ФЗ).
 */
public interface WithdrawConsentUseCase {

    /**
     * Отзыв согласия на обработку ПДн.
     *
     * @param telegramId Telegram ID волонтёра
     * @param reason причина отзыва (опционально)
     */
    void withdrawConsent(TelegramId telegramId, String reason);
}