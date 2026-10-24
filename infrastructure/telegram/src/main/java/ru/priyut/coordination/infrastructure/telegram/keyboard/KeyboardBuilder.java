package ru.priyut.coordination.infrastructure.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent Builder для создания InlineKeyboardMarkup.
 * Упрощает создание сложных клавиатур, избавляя от вложенных списков.
 */
public class KeyboardBuilder {

    private final List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

    /**
     * Добавить новый ряд с одной или несколькими кнопками.
     */
    public KeyboardBuilder addRow(InlineKeyboardButton... buttons) {
        keyboard.add(Arrays.asList(buttons));
        return this;
    }

    /**
     * Удобный метод для добавления кнопки с текстом и callback_data в один вызов.
     */
    public KeyboardBuilder addRow(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return addRow(button);
    }

    /**
     * Добавить URL-кнопку (например, ссылка на сайт приюта).
     */
    public KeyboardBuilder addUrlRow(String text, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setUrl(url);
        return addRow(button);
    }

    public InlineKeyboardMarkup build() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }
}