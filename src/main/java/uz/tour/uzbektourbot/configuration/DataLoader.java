package uz.tour.uzbektourbot.configuration;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.tour.uzbektourbot.service.BotService;


@RequiredArgsConstructor
@Component
@Slf4j
public class DataLoader implements CommandLineRunner {


    private final BotService botService;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;

    @Override
    public void run(String... args) throws Exception {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(this.botService);

        if (ddl.equalsIgnoreCase("create")
                || ddl.equalsIgnoreCase("create-drop")) {
            System.out.println();
            log.info("------------------------------------");
        }


    }


}
