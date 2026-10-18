package ru.priyut.coordination.infrastructure.telegram.dialog;

import org.springframework.stereotype.Component;
import ru.priyut.coordination.domain.shared.TelegramId;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory реализация ConversationStore.
 * Подходит для MVP и одного инстанса приложения.
 *
 * ⚠️ НЕ подходит для кластера — при перезапуске все диалоги теряются.
 * Для production замените на Redis или PostgreSQL.
 */
@Component
public class InMemoryConversationStore implements ConversationStore {

    private final Map<TelegramId, ConversationContext> store = new ConcurrentHashMap<>();

    @Override
    public Optional<ConversationContext> load(TelegramId telegramId) {
        return Optional.ofNullable(store.get(telegramId));
    }

    @Override
    public void save(ConversationContext context) {
        store.put(context.getTelegramId(), context);
    }

    @Override
    public void delete(TelegramId telegramId) {
        store.remove(telegramId);
    }

    @Override
    public int cleanupExpired(Duration ttl) {
        Instant now = Instant.now();
        int removed = 0;
        var iterator = store.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired(now, ttl)) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Количество активных диалогов (для метрик).
     */
    public int size() {
        return store.size();
    }
}