package ru.priyut.coordination.infrastructure.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.priyut.coordination.infrastructure.telegram.callback.CallbackDataCodec;

/**
 * Фабрика стандартных inline-клавиатур для сценариев ТЗ.
 */
public final class InlineKeyboardFactory {

    private InlineKeyboardFactory() {}

    /**
     * Клавиатура для шага 4.1 ТЗ: отправка PDF и кнопка регистрации.
     */
    public static InlineKeyboardMarkup createRegistrationStartKeyboard() {
        return new KeyboardBuilder()
                .addRow("✅ Зарегистрироваться", "reg:start")
                .addRow("❓ Как это работает", "reg:how_it_works")
                .build();
    }

    /**
     * Клавиатура для отклика на поездку (сценарий 4.3 ТЗ).
     */
    public static InlineKeyboardMarkup createTripResponseKeyboard(long tripId) {
        String callbackData = CallbackDataCodec.encode("trip:resp", String.valueOf(tripId));
        return new KeyboardBuilder()
                .addRow("🚕 Я поеду", callbackData)
                .addRow("❌ Пропустить", "trip:skip")
                .build();
    }

    /**
     * Клавиатура для админа: подтверждение или отклонение волонтёра.
     */
    public static InlineKeyboardMarkup createAdminApproveRejectKeyboard(long tripId) {
        String approveData = CallbackDataCodec.encode("trip:approve", String.valueOf(tripId));
        String rejectData = CallbackDataCodec.encode("trip:reject", String.valueOf(tripId));

        return new KeyboardBuilder()
                .addRow("✅ Подтвердить", approveData)
                .addRow("❌ Отклонить", rejectData)
                .build();
    }

    /**
     * Клавиатура для волонтёра: управление активной поездкой (сценарий Д, Е).
     */
    public static InlineKeyboardMarkup createActiveTripManagementKeyboard(long tripId) {
        String completeData = CallbackDataCodec.encode("trip:complete", String.valueOf(tripId));
        String cancelData = CallbackDataCodec.encode("trip:canc", String.valueOf(tripId));

        return new KeyboardBuilder()
                .addRow("🏁 Поездка выполнена", completeData)
                .addRow("❌ Отменить", cancelData)
                .build();
    }

    /**
     * Клавиатура Да/Нет (универсальная).
     */
    public static InlineKeyboardMarkup createYesNoKeyboard(String yesCallback, String noCallback) {
        return new KeyboardBuilder()
                .addRow("✅ Да", yesCallback)
                .addRow("❌ Нет", noCallback)
                .build();
    }
}