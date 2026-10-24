package ru.priyut.coordination.infrastructure.telegram.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface FinalUpdateHandler {
    void handle(Update update);
}
