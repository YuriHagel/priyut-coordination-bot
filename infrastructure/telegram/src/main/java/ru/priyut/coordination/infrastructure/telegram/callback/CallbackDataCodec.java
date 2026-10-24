package ru.priyut.coordination.infrastructure.telegram.callback;

import ru.priyut.coordination.domain.shared.DomainException;

/**
 * Утилита для кодирования и декодирования callback_data.
 * Telegram ограничивает callback_data 64 байтами.
 * Формат: "action:payload" (например, "trip:resp:12345")
 */
public final class CallbackDataCodec {

    private CallbackDataCodec() {}

    /**
     * Кодирует действие и полезную нагрузку в строку до 64 байт.
     */
    public static String encode(String action, String payload) {
        String data = action + ":" + payload;
        if (data.getBytes().length > 64) {
            throw new DomainException("Callback data exceeds 64 bytes limit: " + data);
        }
        return data;
    }

    /**
     * Декодирует строку callback_data.
     */
    public static CallbackData decode(String callbackData) {
        if (callbackData == null || !callbackData.contains(":")) {
            throw new DomainException("Invalid callback data format: " + callbackData);
        }
        int firstColon = callbackData.indexOf(':');
        String action = callbackData.substring(0, firstColon);
        String payload = callbackData.substring(firstColon + 1);
        return new CallbackData(action, payload);
    }

    public record CallbackData(String action, String payload) {}
}