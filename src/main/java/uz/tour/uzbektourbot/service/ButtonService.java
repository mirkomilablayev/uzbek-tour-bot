package uz.tour.uzbektourbot.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.tour.uzbektourbot.util.ButtonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class ButtonService {

    public ReplyKeyboardMarkup shareContactButton() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setRequestContact(true);
        keyboardButton.setText(ButtonUtils.SHARE_CONTACT);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardMarkup rejectButton() {
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(ButtonUtils.REJECT_BUTTON);
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboardRow(new KeyboardRow(new ArrayList<>(Collections.singletonList(keyboardButton)))).build();
    }

    public ReplyKeyboardRemove createEmptyKeyboard() {
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        return keyboardRemove;
    }


    public ReplyKeyboardMarkup adminMainMenu() {
        List<String> buttonNames = new ArrayList<>(Arrays.asList(ButtonUtils.SEND_ADS, ButtonUtils.STATISTICS, ButtonUtils.EXPORT_EXCEL, ButtonUtils.APPLICANTS));
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        int i = 0;
        int counter = 0;
        int qoldiq = buttonNames.size() % 2;
        int size = buttonNames.size();
        for (String name : buttonNames) {
            keyboardRow.add(name);
            i++;
            if (i == 2 || (size - counter == qoldiq && i == qoldiq)) {
                keyboardRowList.add(keyboardRow);
                keyboardRow = new KeyboardRow();
                counter += i;
                i = 0;
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }


    public ReplyKeyboardMarkup createButtons(int countPerLine, List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        int i = 0;
        int counter = 0;
        int qoldiq = buttonNames.size() % countPerLine;
        int size = buttonNames.size();
        for (String name : buttonNames) {
            keyboardRow.add(name);
            i++;
            if (i == countPerLine || (size - counter == qoldiq && i == qoldiq)) {
                keyboardRowList.add(keyboardRow);
                keyboardRow = new KeyboardRow();
                counter += i;
                i = 0;
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }

    private static ReplyKeyboardMarkup getReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;
    }


    public ReplyKeyboard sendAdsButton() {
        List<String> buttonNames = new ArrayList<>(Arrays.asList(ButtonUtils.SEND, ButtonUtils.REJECT_BUTTON));
        ReplyKeyboardMarkup replyKeyboardMarkup = getReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        for (String name : buttonNames) {
            keyboardRow.add(name);
            keyboardRowList.add(keyboardRow);
            keyboardRow = new KeyboardRow();
        }
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        return replyKeyboardMarkup;
    }


    public InlineKeyboardMarkup createInlineButton(Long callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton("Ariza Yuborish");
        button.setCallbackData(callbackData+"");
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        keyboardMarkup.setKeyboard(Collections.singletonList(row));
        return keyboardMarkup;
    }


}

