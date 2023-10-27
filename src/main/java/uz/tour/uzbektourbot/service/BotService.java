package uz.tour.uzbektourbot.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.tour.uzbektourbot.configuration.BotConfiguration;
import uz.tour.uzbektourbot.dto.FileDto;
import uz.tour.uzbektourbot.entity.Ads;
import uz.tour.uzbektourbot.entity.Applicant;
import uz.tour.uzbektourbot.entity.ApplicantRepository;
import uz.tour.uzbektourbot.entity.User;
import uz.tour.uzbektourbot.util.ButtonUtils;
import uz.tour.uzbektourbot.util.Steps;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Component
public class BotService extends TelegramLongPollingBot {


    private final BotConfiguration botConfiguration;
    private final LogicService logicService;
    private final ButtonService buttonService;
    private final ApplicantRepository applicantRepository;
    private final static Double LATITUDE = 41.368647;
    private final static Double LONGITUDE = 69.293928;

    @Override
    public String getBotUsername() {
        return this.botConfiguration.getUsername();
    }

    @Override
    public String getBotToken() {
        return this.botConfiguration.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        String text = logicService.getText(update);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(logicService.getChatId(update));
        User user = logicService.createUser(update);

        if ("/start".equals(text)) {
            startMethod(sendMessage, user);
            return;
        }

        if (update.hasCallbackQuery()) {
            applyMethod(update, sendMessage, user);
            return;
        }


        if (!user.getIsAdmin()) {
            if (ButtonUtils.CONTACTS.equals(text)) {
                sendMessage.setText("Bizning ijtimoiy tarmoqlardagi sahifalarimiz\n" +
                        "Admin @sizor_travel\n" +
                        "Telefon raqam +998951903333\n" +
                        "Telegram @sizortravel\n" +
                        "Instagram www.instagram.com/sizortravel.uz");
                execute(sendMessage);
                return;
            } else if (ButtonUtils.LOCATION.equals(text)) {
                SendLocation sendLocation = new SendLocation();
                sendLocation.setChatId(logicService.getChatId(update));
                sendLocation.setLatitude(LATITUDE);
                sendLocation.setLongitude(LONGITUDE);
                execute(sendLocation);
                return;
            }
            getContactUser(update, sendMessage, user);
        } else {
            if (ButtonUtils.REJECT_BUTTON.equals(text)) {
                rejectPage(sendMessage, user);
                return;
            } else if (ButtonUtils.SEND.equals(text)) {
                sendAds(sendMessage, user);
                return;
            } else if (ButtonUtils.SEND_ADS.equals(text)) {
                prepareforAds(sendMessage, user);
                return;
            } else if (ButtonUtils.STATISTICS.equals(text)) {
                sendMessage.setText("\uD83D\uDC65 Bot foydalanuvchilarining\numimiy soni - " + logicService.getUserCount());
                execute(sendMessage);
                return;
            } else if (ButtonUtils.EXPORT_EXCEL.equals(text)) {
                exportUsersDataToExcel(logicService.usersExcel(), sendMessage, user);
                return;
            } else if (ButtonUtils.APPLICANTS.equals(text)) {
                exportUsersDataToExcel(logicService.applicantsExcel(), sendMessage, user);
                return;
            }

            if (Steps.SEND_ADS.equals(user.getStep())) {
                sendAdsPage(update, text, user, sendMessage);
                return;
            }

            sendMessage.setText("Xato Buyruq Kiritildi!");
            execute(sendMessage);
        }
    }

    private void exportUsersDataToExcel(File logicService, SendMessage sendMessage, User user) throws TelegramApiException {
        if (logicService == null) {
            sendMessage.setText("Fayl Topilmadi");
            execute(sendMessage);
            return;
        }
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(user.getChatId());
        sendDocument.setDocument(new InputFile(logicService));
        execute(sendDocument);
    }


    private void rejectPage(SendMessage sendMessage, User user) throws TelegramApiException {
        logicService.closeActiveAds(user);
        logicService.makeUserRegistered(user);
        ReplyKeyboardMarkup buttons = buttonService.adminMainMenu();
        sendMessage.setReplyMarkup(buttons);
        sendMessage.setText("Admin panelga Xush kelibsiz");
        execute(sendMessage);
    }

    private void getContactUser(Update update, SendMessage sendMessage, User user) throws TelegramApiException {
        if (Steps.SEND_CONTACT.equals(user.getStep())) {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                Contact contact = message.getContact();
                String phoneNumber = contact.getPhoneNumber();
                phoneNumber = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;
                user.setPhoneNumber(phoneNumber);
                user.setStep(Steps.REGISTERED);
                user.setIsAuthenticated(true);
                logicService.save(user);
                sendMessage.setText("Assalomu alaykum, Siz bizning eng so'nggi yangiliklarimizni ushbu botdan olishingiz mumkin");
                sendMessage.setReplyMarkup(buttonService.userMainMenu());
                execute(sendMessage);
                return;
            }
        }

        sendMessage.setText("Xato Buyruq kiritildi\uD83D\uDE0A\n" +
                "Yangiliklarni kuting");
        execute(sendMessage);
    }

    private void applyMethod(Update update, SendMessage sendMessage, User user) throws TelegramApiException {
        long adsId = Long.parseLong(update.getCallbackQuery().getData());
        Applicant applicant = new Applicant();
        applicant.setAdsId(adsId);
        applicant.setPhoneNumber(user.getPhoneNumber());
        applicant.setFullName(user.getFullName());
        applicantRepository.save(applicant);
        sendMessage.setText("Sizning arizangiz yuborildi, Tez orada o'zimiz aloqaga chiqamiz !");
        execute(sendMessage);

        long count = applicantRepository.count();
        for (User admin : logicService.findAllAdmin()) {
            sendMessage.setText("Yangi ariza, Sizda (" + count + ") ariza mavjud");
            sendMessage.setChatId(admin.getChatId());
            execute(sendMessage);
        }
    }

    private void startMethod(SendMessage sendMessage, User user) throws TelegramApiException {
        if (user.getIsAdmin()) {
            ReplyKeyboardMarkup buttons = buttonService.adminMainMenu();
            sendMessage.setReplyMarkup(buttons);
            sendMessage.setText("Admin panelga Xush kelibsiz");
            execute(sendMessage);
        } else if (user.getIsAuthenticated()) {
            sendMessage.setText("Assalomu alaykum, Siz bizning eng so'nggi yangiliklarimizni ushbu botdan olishingiz mumkin");
            sendMessage.setReplyMarkup(buttonService.userMainMenu());
            execute(sendMessage);
        } else {
            ReplyKeyboardMarkup button = buttonService.shareContactButton();
            sendMessage.setText("Telefon raqamingizni yuboring");
            sendMessage.setReplyMarkup(button);
            user.setStep(Steps.SEND_CONTACT);
            logicService.save(user);
            execute(sendMessage);
        }
    }


    private void prepareforAds(SendMessage sendMessage, User user) throws TelegramApiException {
        user.setStep(Steps.SEND_ADS);
        user.setChildStep(Steps.send_ads_photo);
        logicService.save(user);
        sendMessage.setReplyMarkup(buttonService.rejectButton());
        sendMessage.setText("Reklama Faylini yuboring, (Video-Rasm)!");
        execute(sendMessage);
    }


    private void sendAdsPage(Update update, String text, User user, SendMessage sendMessage) throws TelegramApiException {
        if (Steps.send_ads_photo.equals(user.getChildStep())) {
            FileDto fileDto = getFileId(update);

            if (fileDto == null) {
                sendMessage.setText("Reklama fayli faqat rasm yoki video bo'lishi mumkin!");
                execute(sendMessage);
                return;
            }
            if (fileDto.getFileId() == null) {
                sendMessage.setText("Rasm yoki video topilmadi qaytadan urinib ko'ring!");
                execute(sendMessage);
                return;
            }
            user.setChildStep(Steps.send_ads_text);
            Ads ads = new Ads();
            ads.setIsActive(true);
            ads.setUserId(user.getId());
            ads.setFileId(fileDto.getFileId());
            ads.setContentType(fileDto.getContentType());
            logicService.saveAds(ads);
            logicService.save(user);
            sendMessage.setText("Reklamangiz uchun textni yuboring!");
            sendMessage.setReplyMarkup(buttonService.rejectButton());
            execute(sendMessage);
        } else if (Steps.send_ads_text.equals(user.getChildStep())) {
            if (text.equals("error_text")) {
                sendMessage.setText("Iltimos to'g'ri text yuboring");
                sendMessage.setReplyMarkup(buttonService.rejectButton());
                execute(sendMessage);
                return;
            }
            Ads ads = logicService.getActiveAds(user);
            if (ads.getId() == null) {
                sendMessage.setText("Reklama topilmadi!");
                sendMessage.setReplyMarkup(buttonService.adminMainMenu());
                execute(sendMessage);
                return;
            }
            ads.setText(text);
            ads = logicService.saveAds(ads);

            File file = getFileById(ads);
            if (file == null) {
                sendMessage.setText("Telegram serveridan file topilmadi!");
                sendMessage.setReplyMarkup(buttonService.adminMainMenu());
                execute(sendMessage);
                return;
            }

            if (".jpg".equals(ads.getContentType())) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(user.getChatId());
                sendPhoto.setCaption("Assalomu alaykum {Foydalanuvchi ismi}\n\n" + ads.getText());
                sendPhoto.setPhoto(new InputFile(file));
                sendPhoto.setReplyMarkup(buttonService.sendAdsButton());
                execute(sendPhoto);
            } else {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(user.getChatId());
                sendVideo.setVideo(new InputFile(file));
                sendVideo.setCaption("Assalomu alaykum {Foydalanuvchi ismi}\n\n" + ads.getText());
                sendVideo.setReplyMarkup(buttonService.sendAdsButton());
                execute(sendVideo);
            }

        }
    }

    private void sendAds(SendMessage sendMessage, User user) throws TelegramApiException {
        Ads ads = logicService.getActiveAds(user);
        if (ads.getId() == null) {
            sendMessage.setText("Ads Topilmadi!");
            sendMessage.setReplyMarkup(buttonService.adminMainMenu());
            execute(sendMessage);
            logicService.closeActiveAds(user);
            return;
        }
        File file = getFileById(ads);
        if (file == null) {
            sendMessage.setText("Reklama Fayli Topilmadi!");
            sendMessage.setReplyMarkup(buttonService.adminMainMenu());
            execute(sendMessage);
            logicService.closeActiveAds(user);
            return;
        }
        sendMessage.setText("Yuborish boshlandi...");
        execute(sendMessage);
        for (User allUser : logicService.getAllUsers()) {
            if (".jpg".equals(ads.getContentType())) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(allUser.getChatId());
                sendPhoto.setCaption("Assalomu alaykum <b>" + allUser.getFullName() + "</b>\n\n" + ads.getText());
                sendPhoto.setParseMode("HTML");
                sendPhoto.setPhoto(new InputFile(file));
                sendPhoto.setReplyMarkup(buttonService.createInlineButton(ads.getId()));
                execute(sendPhoto);
            } else {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(allUser.getChatId());
                sendVideo.setVideo(new InputFile(file));
                sendVideo.setCaption("Assalomu alaykum " + allUser.getFullName() + "\n\n" + ads.getText());
                sendVideo.setReplyMarkup(buttonService.createInlineButton(ads.getId()));
                execute(sendVideo);
            }
        }
        logicService.makeUserRegistered(user);
        sendMessage.setText("Yuborish Tugadi...");
        sendMessage.setReplyMarkup(buttonService.adminMainMenu());
        execute(sendMessage);
        logicService.closeActiveAds(user);
    }


    public FileDto getFileId(Update update) {
        FileDto fileDto = new FileDto();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasPhoto()) {
                List<PhotoSize> photos = message.getPhoto();
                PhotoSize largestPhoto = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
                fileDto.setFileId(largestPhoto != null ? largestPhoto.getFileId() : null);
                fileDto.setContentType(".jpg");
            } else if (message.hasVideo()) {
                Video video = message.getVideo();
                fileDto.setFileId(video.getFileId());
                fileDto.setContentType(".mp4");
            } else {
                return null;
            }
        }
        return fileDto;
    }


    public File getFileById(Ads ads) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(ads.getFileId());
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
            String fileDownloadUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath();
            InputStream in = new URL(fileDownloadUrl).openStream();
            File downloadedFile = File.createTempFile("downloaded-file", ads.getContentType());
            OutputStream out = Files.newOutputStream(downloadedFile.toPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return downloadedFile;
        } catch (Exception e) {
            return null;
        }
    }

}
