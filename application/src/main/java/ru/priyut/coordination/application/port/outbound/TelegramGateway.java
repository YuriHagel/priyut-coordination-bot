package ru.priyut.coordination.application.port.outbound;

import ru.priyut.coordination.domain.shared.TelegramId;
import java.io.InputStream;
import java.util.List;

public interface TelegramGateway {

    void sendMessage(TelegramId telegramId, String text);

    /**
     * Отправка сообщения с inline-клавиатурой.
     * @param keyboard Список рядов кнопок. Например: List.of(List.of(button1, button2))
     */
    void sendMessage(TelegramId telegramId, String text, List<List<InlineButton>> keyboard);

    void sendDocument(TelegramId telegramId, InputStream documentStream, String fileName, String caption);

    void broadcastMessage(String text, List<TelegramId> recipients);

    /**
     * Отвечает на callback-запрос, убирая индикатор загрузки в Telegram.
     */
    void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert);
}