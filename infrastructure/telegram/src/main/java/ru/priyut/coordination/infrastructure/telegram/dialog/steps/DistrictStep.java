package ru.priyut.coordination.infrastructure.telegram.dialog.steps;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.priyut.coordination.infrastructure.telegram.dialog.ConversationContext;
import ru.priyut.coordination.infrastructure.telegram.dialog.DialogStep;
import ru.priyut.coordination.infrastructure.telegram.dialog.StepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Шаг: выбор района из кнопок.
 */
public class DistrictStep implements DialogStep {

    private static final List<String> DISTRICTS = List.of(
            "Центральный",
            "Московский",
            "Всеволожский"
    );

    @Override
    public String name() {
        return "DISTRICT";
    }

    @Override
    public SendMessage render(ConversationContext context) {
        var msg = new SendMessage();
        msg.setText("📍 В каком районе вы находитесь?");

        var keyboard = new InlineKeyboardMarkup();
        var rows = new ArrayList<List<InlineKeyboardButton>>();

        for (String district : DISTRICTS) {
            var button = new InlineKeyboardButton();
            button.setText("📍 " + district);
            button.setCallbackData("district:" + district);
            rows.add(List.of(button));
        }

        keyboard.setKeyboard(rows);
        msg.setReplyMarkup(keyboard);
        return msg;
    }

    @Override
    public StepResult handle(Update update, ConversationContext context) {
        String district = null;

        // Обработка callback от кнопки
        if (update.hasCallbackQuery()) {
            var callbackData = update.getCallbackQuery().getData();
            if (callbackData.startsWith("district:")) {
                district = callbackData.substring("district:".length());
            }
        }
        // Обработка текстового ввода (если пользователь ввёл вручную)
        else if (update.hasMessage() && update.getMessage().hasText()) {
            district = update.getMessage().getText().trim();
        }

        if (district == null || district.isEmpty()) {
            return StepResult.ValidationError.of("Пожалуйста, выберите район из списка.");
        }

        if (!DISTRICTS.contains(district)) {
            return StepResult.ValidationError.of(
                    "❌ Неизвестный район. Пожалуйста, выберите из списка:\n" +
                            String.join(", ", DISTRICTS)
            );
        }

        context.put("district", district);
        return StepResult.empty();
    }
}