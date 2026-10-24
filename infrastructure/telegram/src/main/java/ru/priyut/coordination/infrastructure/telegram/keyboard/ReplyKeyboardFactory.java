package ru.priyut.coordination.infrastructure.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Фабрика для ReplyKeyboard (кнопки под полем ввода текста).
 * Используется реже, чем Inline, но полезна для главного меню.
 */
public final class ReplyKeyboardFactory {

    private ReplyKeyboardFactory() {}

    /**
     * Главное меню для зарегистрированного волонтёра.
     */
    public static ReplyKeyboardMarkup createVolunteerMainMenu() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true); // Сжать клавиатуру по высоте
        keyboard.setSelective(true);      // Показывать только этому пользователю

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📋 Мои поездки");
        row1.add("👤 Мой профиль");
        keyboardRows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⚙️ Настройки доступности");
        keyboardRows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add("🛑 Удалить анкету");
        keyboardRows.add(row3);

        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
}