package by.Rsh;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class IzzyasDealApplication {
    public static void main(String[] args) {
        SpringApplication.run(IzzyasDealApplication.class, args);
    }
    @Bean
    public CommandLineRunner testDatabaseConnection() {
        return args -> {
            System.out.println("=================================================");
            System.out.println("ИИИХА! Приложение успешно запустилось!");
            System.out.println("База данных 'izzy_games' успешно подключена.");
            System.out.println("=================================================");
        };
    }
}
