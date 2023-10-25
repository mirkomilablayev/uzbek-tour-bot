package uz.tour.uzbektourbot.configuration;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfiguration {

    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;




}
