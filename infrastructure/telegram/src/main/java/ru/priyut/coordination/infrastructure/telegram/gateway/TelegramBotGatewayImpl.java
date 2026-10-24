package ru.priyut.coordination.infrastructure.telegram.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.priyut.coordination.application.port.outbound.InlineButton;
import ru.priyut.coordination.application.port.outbound.TelegramGateway;
import ru.priyut.coordination.domain.shared.TelegramId;
import ru.priyut.coordination.infrastructure.telegram.bot.PriyutTelegramBot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBotGatewayImpl implements TelegramGateway {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotGatewayImpl.class);
    private final PriyutTelegramBot telegramBot;

    public TelegramBotGatewayImpl(PriyutTelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @Override
    public void sendMessage(TelegramId telegramId, String text) {
        sendMessage(telegramId, text, null);
    }

    @Override
    public void sendMessage(TelegramId telegramId, String text, List<List<InlineButton>> keyboard) {
        try {
            SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                    .chatId(String.valueOf(telegramId.value())) // ✅ Правильное преобразование long -> String
                    .text(text);

            if (keyboard != null && !keyboard.isEmpty()) { // ✅ Исправлена опечатка isEmptyа()
                builder.replyMarkup(buildInlineKeyboard(keyboard));
            }

            telegramBot.execute(builder.build());
        } catch (Exception e) {
            log.error("❌ Failed to send message to user {}: {}", telegramId, e.getMessage());
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    @Override
    public void sendDocument(TelegramId telegramId, InputStream documentStream, String fileName, String caption) {
        try {
            // InputFile может принимать InputStream, что идеально для файлов из classpath (resources)
            InputFile inputFile = new InputFile(documentStream, fileName);

            SendDocument document = SendDocument.builder()
                    .chatId(String.valueOf(telegramId.value())) // ✅ Правильное преобразование
                    .document(inputFile)
                    .caption(caption)
                    .build();

            telegramBot.execute(document);
            log.debug("✅ Successfully sent document '{}' to user {}", fileName, telegramId);
        } catch (Exception e) {
            log.error("❌ Failed to send document '{}' to user {}: {}", fileName, telegramId, e.getMessage());
            throw new RuntimeException("Failed to send Telegram document", e);
        }
    }

    @Override
    public void broadcastMessage(String text, List<TelegramId> recipients) {
        log.info("📢 Starting broadcast to {} recipients", recipients.size());
        int successCount = 0;
        int failCount = 0;

        for (TelegramId recipient : recipients) {
            try {
                sendMessage(recipient, text);
                successCount++;

                // Небольшая пауза для снижения нагрузки на API
                Thread.sleep(50);

            } catch (InterruptedException e) {
                // 1. СНАЧАЛА специфичное исключение
                Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
                log.error("Broadcast interrupted");
                throw new RuntimeException("Broadcast interrupted", e);

            } catch (Exception e) {
                // 2. ПОТОМ общее исключение (ловит всё остальное)
                failCount++;
                log.error("❌ Failed to broadcast to user {}: {}", recipient, e.getMessage());
                // Не прерываем цикл, чтобы отправить сообщение остальным
            }
        }

        log.info("✅ Broadcast completed. Success: {}, Failed: {}", successCount, failCount);
    }

    @Override
    public void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .showAlert(showAlert)
                    .build();

            telegramBot.execute(answer);
        } catch (Exception e) {
            // Логируем ошибку, но не выбрасываем исключение, чтобы не ломать
            // основной поток обработки команды, если ответ на кнопку не прошел.
            log.error("❌ Failed to answer callback query {}: {}", callbackQueryId, e.getMessage());
        }
    }

    /**
     * Маппинг нашего простого DTO в объекты библиотеки Telegram Bots
     */
    private InlineKeyboardMarkup buildInlineKeyboard(List<List<InlineButton>> keyboardDto) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (List<InlineButton> rowDto : keyboardDto) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (InlineButton btnDto : rowDto) {
                buttons.add(InlineKeyboardButton.builder()
                        .text(btnDto.text())
                        .callbackData(btnDto.callbackData())
                        .build());
            }
            rows.add(new InlineKeyboardRow(buttons));
        }
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}