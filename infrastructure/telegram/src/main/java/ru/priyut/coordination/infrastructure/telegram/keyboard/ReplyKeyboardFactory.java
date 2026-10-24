package ru.priyut.coordination.infrastructure.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Фабрика для ReplyKeyboard (клавиатура под полем ввода текста).
 * Использует строгую типизацию (KeyboardRow + KeyboardButton) и Builder API
 * в полном соответствии с требованиями библиотеки Telegram Bots.
 */
public final class ReplyKeyboardFactory {

    private ReplyKeyboardFactory() {}

    /**
     * Главное меню для зарегистрированного волонтёра.
     */
    public static ReplyKeyboardMarkup createVolunteerMainMenu() {
        // 1. Создаем ряды и наполняем их кнопками
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📋 Мои поездки"));
        row1.add(new KeyboardButton("👤 Мой профиль"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("⚙️ Настройки доступности"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("🛑 Удалить анкету"));

        // 2. Собираем финальный объект через Builder, как показывает декомпилированный код
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row1, row2, row3)) // Передаем строго List<KeyboardRow>
                .resizeKeyboard(true)                // Сжать клавиатуру по высоте (подогнать под размер экрана)
                .oneTimeKeyboard(false)              // Не скрывать клавиатуру после первого нажатия
                .selective(true)                     // Показывать только тому, кому отправлено сообщение
                .isPersistent(true)                  // Клавиатура остается прикрепленной к чату постоянно
                .build();
    }

    /**
     * Простая клавиатура Да/Нет (универсальная).
     */
    public static ReplyKeyboardMarkup createYesNoKeyboard(String yesText, String noText) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(yesText));
        row.add(new KeyboardButton(noText));

        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true) // Скрыть после нажатия (для одноразовых подтверждений)
                .build();
    }
}