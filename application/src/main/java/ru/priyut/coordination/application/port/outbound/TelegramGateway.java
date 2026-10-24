package ru.priyut.coordination.application.port.outbound;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.io.InputStream;
import java.util.List;

public interface TelegramGateway {

    void sendMessage(TelegramId telegramId, String text);

    void sendMessage(TelegramId telegramId, String text, ReplyKeyboard replyMarkup);

    void sendDocument(TelegramId telegramId, InputStream documentStream, String fileName, String caption);

    void broadcastMessage(String text, List<TelegramId> recipients);

    /**
     * Отвечает на callback-запрос, убирая индикатор загрузки в Telegram.
     *
     * @param callbackQueryId ID запроса из update.getCallbackQuery().getId()
     * @param text            Текст всплывающего уведомления (опционально)
     * @param showAlert       Если true, показывает всплывающее окно, если false - только в строке состояния
     */
    void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert);
}