package me.joshuaportero.ajs;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import me.joshuaportero.ajs.api.JobDataAPI;
import me.joshuaportero.ajs.data.JobData;
import me.joshuaportero.ajs.data.JobFilter;
import me.joshuaportero.ajs.data.JobType;
import me.joshuaportero.ajs.notifications.DiscordWebhook;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class JobScraper {
    private Dotenv dotenv;

    public static void main(String[] args) {
        JobScraper jobScraper = new JobScraper();
        jobScraper.startUp();

        DiscordWebhook discordWebhook = new DiscordWebhook(jobScraper.getDotEnv().get("DISCORD_WEBHOOK_URL"));

        WebDriver driver = jobScraper.getWebDriver(jobScraper.getDotEnv());
        driver.get(jobScraper.getDotEnv().get("AMAZON_URL"));

        // Accept cookies and go to job search page
        jobScraper.acceptCookies(driver);
        driver.findElements(By.cssSelector("a[href=\"https://hiring.amazon.com/app#/jobSearch\"]")).get(0).click();

        // Schedule job search
        HashMap<JobData, Long> jobsNotified = new HashMap<>();

        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger poolNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r);
                t.setName("scrap-" + poolNumber.getAndIncrement());
                return t;
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, threadFactory);
        Runnable checkJobCardsTask = () -> {
            // Search for jobs
            log.info("Searching for jobs...");
            List<WebElement> jobCards = jobScraper.getJobCards(driver);

            List<JobData> jobsData = new ArrayList<>();

            for (WebElement jobCard : jobCards) {
                jobsData.add(JobDataAPI.fromStringToJobData(jobCard.getText()));
            }

            log.info("Found " + jobsData.size() + " jobs.");

            // Filter jobs
            List<JobFilter> filters = new ArrayList<>();
            filters.add(new JobFilter("TYPE", "ANY", (jobData, jobFieldValue) -> Arrays.stream(jobData.getJobType())
                    .anyMatch(jobTypeEnum -> jobTypeEnum == JobType.UNKNOWN || jobTypeEnum.name().equalsIgnoreCase(jobFieldValue))));
            filters.add(new JobFilter("DURATION", "ANY", (jobData, jobFieldValue) -> Arrays.stream(jobData.getJobDurations()).anyMatch(jobDurationEnum -> jobDurationEnum.name().equalsIgnoreCase(jobFieldValue))));
            filters.add(new JobFilter("PAY_RATE", "ANY", (jobData, jobFieldValue) -> jobData.getPay() < Double.parseDouble(jobFieldValue)));
            filters.add(new JobFilter("BLACKLISTED_LOCATIONS", "NONE", (jobData, jobFieldValue) -> !jobData.getLocation().contains(jobFieldValue)));
            filters.add(new JobFilter("DISTANCE", "", (jobData, jobFieldValue) -> jobData.getDistance() < Double.parseDouble(jobFieldValue)));

            // Apply filters
            for (JobFilter filter : filters) {
                filter.apply(jobScraper.getDotEnv(), jobsData);
            }

            log.info("Found " + jobsData.size() + " jobs after filtering.");

            // Remove jobs that have been notified for more than a certain amount of time
            int notificationCool = Integer.parseInt(jobScraper.getDotEnv().get("NOTIFICATION_COOLDOWN"));
            if (!jobsNotified.isEmpty()) {
                jobsNotified.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > 1000L * notificationCool);
            }

            // Send notification to Discord
            if (!jobsData.isEmpty()) {
                List<String> userIds = Arrays.asList(jobScraper.getDotEnv().get("DISCORD_USER_IDS").split(","));
                for (JobData jobData : jobsData) {
                    if (!jobsNotified.containsKey(jobData)) {
                        jobsNotified.putIfAbsent(jobData, System.currentTimeMillis());
                        String mentions = userIds.stream()
                                .map(id -> "<@" + id + ">")
                                .collect(Collectors.joining(" "));
                        String embedMessage = "{"
                                + "\"content\": \"" + mentions + "\","
                                + "\"embeds\": ["
                                + "{"
                                + "\"title\": \"" + jobData.getTitle() + "\","
                                + "\"color\": 65280,"
                                + "\"thumbnail\": { \"url\": \"https://cdn.discordapp.com/attachments/817464794370146326/1114570634920464464/aa142d2c-681c-45a2-99e9-a6e63849b351.png\" },"
                                + "\"fields\": ["
                                + "{ \"name\": \"Shifts\", \"value\": \"" + jobData.getShifts() + " shift(s) available\", \"inline\": true },"
                                + "{ \"name\": \"Type\", \"value\": \"" + Arrays.toString(jobData.getJobType()).replaceAll("\\[", "").replaceAll("]", "") + "\", \"inline\": true },"
                                + "{ \"name\": \"Duration\", \"value\": \"" + Arrays.toString(jobData.getJobDurations()).replaceAll("\\[", "").replaceAll("]", "") + "\", \"inline\": true },"
                                + "{ \"name\": \"Pay\", \"value\": \"$" + jobData.getPay() + "\", \"inline\": true },"
                                + "{ \"name\": \"Location\", \"value\": \"" + jobData.getLocation() + "\", \"inline\": true },"
                                + "{ \"name\": \"Distance\", \"value\": \"" + jobData.getDistance() + " miles\", \"inline\": true }"
                                + "]}]}";
                        discordWebhook.sendNotification(embedMessage);
                    }
                }
            }

            //Reload page
            driver.navigate().refresh();
        };

        int period = Integer.parseInt(jobScraper.getDotEnv().get("RESET_TIME"));
        log.warn("The script will reset every " + period + " seconds. Until stopped.");
        executor.scheduleAtFixedRate(checkJobCardsTask, 0, period, TimeUnit.SECONDS);
    }

    private List<WebElement> getJobCards(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector("div[data-test-component='StencilReactCard'].jobCardItem")
                )
        );
    }

    private void acceptCookies(WebDriver driver) {
        try {
            Thread.sleep(1000);
            List<WebElement> buttons = driver.findElements(By.cssSelector("button[data-test-component='StencilReactButton']"));
            boolean consentClicked = false;
            for (WebElement button : buttons) {
                String buttonText = button.getText();
                if (buttonText.equals("I consent")) {
                    button.click();
                    consentClicked = true;
                    log.info("Clicked the 'I consent' button.");
                    break;
                }
            }
            if (!consentClicked) {
                log.error("Couldn't find the 'I consent' button.");
            }
        } catch (InterruptedException e) {
            log.error("Error while waiting for page to load: {}", e.getMessage());
            Thread.currentThread().interrupt();
            driver.quit();
        }
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

        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");

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