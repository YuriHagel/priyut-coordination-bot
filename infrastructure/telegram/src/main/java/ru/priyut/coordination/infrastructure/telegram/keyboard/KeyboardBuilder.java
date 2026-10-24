package ru.priyut.coordination.infrastructure.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent Builder для создания InlineKeyboardMarkup.
 * Корректно использует InlineKeyboardRow и InlineKeyboardButton.Builder
 * в соответствии с требованиями библиотеки Telegram Bots API.
 */
public class KeyboardBuilder {

    private final List<InlineKeyboardRow> keyboard;

    public KeyboardBuilder() {
        this.keyboard = new ArrayList<>();
    }

    /**
     * Добавить новый ряд с одной или несколькими кнопками.
     * Оборачивает массив кнопок в требуемый библиотекой InlineKeyboardRow.
     */
    public KeyboardBuilder addRow(InlineKeyboardButton... buttons) {
        if (buttons != null && buttons.length > 0) {
            this.keyboard.add(new InlineKeyboardRow(Arrays.asList(buttons)));
        }
        return this;
    }

    /**
     * Удобный метод для добавления кнопки с текстом и callback_data в один вызов.
     * Использует Builder для InlineKeyboardButton.
     */
    public KeyboardBuilder addRow(String text, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
        return addRow(button);
    }

    /**
     * Добавить URL-кнопку (например, ссылка на сайт приюта).
     */
    public KeyboardBuilder addUrlRow(String text, String url) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
        return addRow(button);
    }

    /**
     * Собирает финальный объект InlineKeyboardMarkup.
     * Также использует Builder для соответствия требованиям библиотеки.
     */
    public InlineKeyboardMarkup build() {
        return InlineKeyboardMarkup.builder()
                .keyboard(this.keyboard)
                .build();
    }
}