package me.joshuaportero.ajs;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Slf4j
public class Main {
    private Dotenv dotenv;

    public static void main(String[] args) {
        Main main = new Main();
        main.startUp();

        WebDriver driver = main.getWebDriver(main.getDotEnv());
        driver.get(main.getDotEnv().get("AMAZON_URL"));

        log.info("Closing driver...");
        driver.quit();
    }

    private WebDriver getWebDriver(Dotenv dotenv) {
        String headless = dotenv.get("HEADLESS");

        ChromeOptions options = new ChromeOptions();

        if (!headless.equals("true")) {
            log.info("Running in non-headless mode...");
        } else {
            log.info("Running in headless mode...");
            options.addArguments("--headless=new");
        }

        return new ChromeDriver(options);
    }

    private void startUp() {
        System.out.println(this.banner());
        log.debug("Loading environment variables from .env file...");
        dotenv = Dotenv.load();
        log.debug("Environment variables loaded!");
    }

    private String banner() {
        return """
                     
                     _  ___  ____    ____   ____ ____      _    ____  _____ ____ \s
                    | |/ _ \\| __ )  / ___| / ___|  _ \\    / \\  |  _ \\| ____|  _ \\\s
                 _  | | | | |  _ \\  \\___ \\| |   | |_) |  / _ \\ | |_) |  _| | |_) |
                | |_| | |_| | |_) |  ___) | |___|  _ <  / ___ \\|  __/| |___|  _ <\s
                 \\___/ \\___/|____/  |____/ \\____|_| \\_\\/_/   \\_\\_|   |_____|_| \\_\\
                 """;
    }

    private Dotenv getDotEnv() {
        return dotenv;
    }
}