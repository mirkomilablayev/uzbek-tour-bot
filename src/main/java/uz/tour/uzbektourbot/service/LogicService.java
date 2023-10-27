package uz.tour.uzbektourbot.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.tour.uzbektourbot.entity.*;
import uz.tour.uzbektourbot.util.Steps;

import javax.jws.soap.SOAPBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LogicService {
    private final UserRepository userRepository;
    private final AdsRepository adsRepository;
    private final ApplicantRepository applicantRepository;

    public User createUser(Update update) {
        String realId = getRealId(update);
        org.telegram.telegrambots.meta.api.objects.User tgUser = getUser(update);
        String chatId = getChatId(update);

        Optional<User> userOptional = userRepository.findByChatId(realId);
        if (!userOptional.isPresent()) {
            User user = new User();
            user.setIsAdmin(Boolean.FALSE);
            user.setChatId(chatId);
            user.setRealId(realId);
            user.setFullName(tgUser.getFirstName() + (tgUser.getLastName() == null ? "" : " " + tgUser.getLastName()));
            user.setStep(Steps.REGISTERED);
            return userRepository.save(user);
        }
        return userOptional.get();

    }

    public List<User> findAllAdmin() {
        return userRepository.findAllByIsAdmin(true);
    }

    public void makeUserRegistered(User user) {
        user.setStep(Steps.REGISTERED);
        user.setChildStep(null);
        user = userRepository.save(user);
        System.out.println(user.getId());
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public String getRealId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId().toString();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom().getId().toString();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom().getId().toString();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom().getId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom().getId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom().getId().toString();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom().getId().toString();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom().getId().toString();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser().getId().toString();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom().getId().toString();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom().getId().toString();
        }
        return "";
    }

    private org.telegram.telegrambots.meta.api.objects.User getUser(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getFrom();
        } else if (update.hasChatMember()) {
            return update.getChatMember().getFrom();
        } else if (update.hasChosenInlineQuery()) {
            return update.getChosenInlineQuery().getFrom();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getFrom();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getFrom();
        } else if (update.hasMyChatMember()) {
            return update.getMyChatMember().getFrom();
        } else if (update.hasInlineQuery()) {
            return update.getInlineQuery().getFrom();
        } else if (update.hasPollAnswer()) {
            return update.getPollAnswer().getUser();
        } else if (update.hasPreCheckoutQuery()) {
            return update.getPreCheckoutQuery().getFrom();
        } else if (update.hasShippingQuery()) {
            return update.getShippingQuery().getFrom();
        }
        return new org.telegram.telegrambots.meta.api.objects.User();
    }


    public String getChatId(Update update) {
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId().toString();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getChatId().toString();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getChatId().toString();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId().toString();
        } else {
            return update.getMessage().getChatId().toString();
        }
    }

    public String getText(@NonNull Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText() != null) {
                return update.getMessage().getText();
            }
            return "error_text";
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else if (update.hasChannelPost()) {
            return update.getChannelPost().getText();
        } else if (update.hasEditedChannelPost()) {
            return update.getEditedChannelPost().getText();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getText();
        }
        return "";
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Long getUserCount() {
        return userRepository.count();
    }

    public Ads saveAds(Ads ads) {
        return adsRepository.save(ads);
    }

    public void closeActiveAds(User user) {
        Optional<Ads> adsOptional = adsRepository.findByUserIdAndIsActive(user.getId(), true);
        if (adsOptional.isPresent()) {
            Ads ads = adsOptional.get();
            ads.setIsActive(false);
            Ads ads1 = saveAds(ads);
            System.out.println(ads1.getId());
        }
    }

    public File usersExcel() {
        List<User> users = userRepository.findAll();
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Data");

            // Create the header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Full Name");
            headerRow.createCell(2).setCellValue("Phone Number");

            int rowNum = 1;
            for (User user : users) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(user.getId());
                dataRow.createCell(1).setCellValue(user.getFullName());
                dataRow.createCell(2).setCellValue(user.getPhoneNumber());
            }
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            File excelFile = File.createTempFile("user-data", ".xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            }

            return excelFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public File applicantsExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Data");

            // Create the header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Full Name");
            headerRow.createCell(2).setCellValue("Phone Number");

            int rowNum = 1;
            List<Applicant> allApplicant = applicantRepository.findAll();
            applicantRepository.deleteAll();
            for (Applicant applicant : allApplicant) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(applicant.getId());
                dataRow.createCell(1).setCellValue(applicant.getFullName());
                dataRow.createCell(2).setCellValue(applicant.getPhoneNumber());
            }
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            File excelFile = File.createTempFile("applicants-", ".xlsx");
            try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
                workbook.write(fileOut);
            }

            return excelFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Ads getActiveAds(User user) {
        return adsRepository.findByUserIdAndIsActive(user.getId(), true).orElse(new Ads());
    }
}