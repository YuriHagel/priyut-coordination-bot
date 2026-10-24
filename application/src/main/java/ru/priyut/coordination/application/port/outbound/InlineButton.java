package ru.priyut.coordination.application.port.outbound;

/**
 * Простое представление inline-кнопки, независимое от библиотеки Telegram.
 * Позволяет модулю application описывать клавиатуры, не зная о Telegram API.
 */
public record InlineButton(String text, String callbackData) {
}