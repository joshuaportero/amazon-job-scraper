package me.joshuaportero.ajs;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import me.joshuaportero.ajs.api.JobDataAPI;
import me.joshuaportero.ajs.data.JobData;
import me.joshuaportero.ajs.notifications.DiscordWebhook;
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
import java.util.concurrent.TimeUnit;
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

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable checkJobCardsTask = () -> {
            // Search for jobs
            log.info("Searching for jobs...");
            List<WebElement> jobCards = jobScraper.getJobCards(driver);

            List<JobData> jobsData = new ArrayList<>();

            for (WebElement jobCard : jobCards) {
                jobsData.add(JobDataAPI.fromStringToJobData(jobCard.getText()));
            }

            log.info("Found " + jobsData.size() + " jobs.");

            // Filter jobs by title
            String[] jobTitles = jobScraper.getDotEnv().get("TITLE").split(",");
            List<String> jobTitleList = Arrays.asList(jobTitles);
            if (jobTitleList.stream().noneMatch("ANY"::equalsIgnoreCase)) {
                log.info("Filtering jobs by title...");
                jobsData.removeIf(jobData -> jobTitleList.stream().noneMatch(jobTitle -> jobData.getTitle().contains(jobTitle)));
            }

            // Filter jobs by type
            String[] jobTypes = jobScraper.getDotEnv().get("TYPE").split(",");
            List<String> jobTypeList = Arrays.asList(jobTypes);

            if (jobTypeList.stream().noneMatch("ANY"::equalsIgnoreCase)) {
                log.info("Filtering jobs by type...");
                jobsData.removeIf(jobData ->
                        jobTypeList.stream().noneMatch(jobType ->
                                Arrays.stream(jobData.getJobType()).anyMatch(jobTypeEnum ->
                                        jobTypeEnum.name().equalsIgnoreCase(jobType)
                                )
                        )
                );
            }

            // Filter jobs by duration
            String[] jobDurations = jobScraper.getDotEnv().get("DURATION").split(",");
            List<String> jobDurationList = Arrays.asList(jobDurations);

            if (jobDurationList.stream().noneMatch("ANY"::equalsIgnoreCase)) {
                log.info("Filtering jobs by duration...");
                jobsData.removeIf(jobData ->
                        jobDurationList.stream().noneMatch(jobDuration ->
                                Arrays.stream(jobData.getJobDurations()).anyMatch(jobDurationEnum ->
                                        jobDurationEnum.name().equalsIgnoreCase(jobDuration)
                                )
                        )
                );
            }

            // Filter jobs by pay
            String[] jobPay = jobScraper.getDotEnv().get("PAY_RATE").split(",");
            List<String> jobPayList = Arrays.asList(jobPay);
            if (jobPayList.stream().noneMatch("ANY"::equalsIgnoreCase)) {
                log.info("Filtering jobs by pay...");
                jobsData.removeIf(jobData ->
                        jobPayList.stream().anyMatch(jobPayString ->
                                jobData.getPay() < Double.parseDouble(jobPayString)
                        )
                );
            }

            // Filter jobs by blacklisted location
            String[] jobBlacklistedLocations = jobScraper.getDotEnv().get("BLACKLISTED_LOCATIONS").split(",");
            List<String> jobBlacklistedLocationList = Arrays.asList(jobBlacklistedLocations);
            if (jobBlacklistedLocationList.stream().noneMatch("NONE"::equalsIgnoreCase)) {
                log.info("Filtering jobs by blacklisted location...");
                jobsData.removeIf(jobData ->
                        jobBlacklistedLocationList.stream().anyMatch(jobBlacklistedLocation ->
                                jobData.getLocation().contains(jobBlacklistedLocation)
                        )
                );
            }

            // Filter jobs by distance
            String[] jobDistance = jobScraper.getDotEnv().get("DISTANCE").split(",");
            List<String> jobDistanceList = Arrays.asList(jobDistance);
            if (!jobDistanceList.get(0).isEmpty()) {
                log.info("Filtering jobs by distance...");
                jobsData.removeIf(jobData ->
                        jobDistanceList.stream().noneMatch(jobDistanceString ->
                                jobData.getDistance() < Double.parseDouble(jobDistanceString)
                        )
                );
            }

            log.info("Found " + jobsData.size() + " jobs after filtering.");

            // Remove jobs that have been notified for more than a certain amount of time
            int notificationCool = Integer.parseInt(jobScraper.getDotEnv().get("NOTIFICATION_COOLDOWN"));
            if (!jobsNotified.isEmpty()) {
                jobsNotified.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > 1000L * notificationCool);
            }

            // Send notification to Discord
            if (jobsData.size() > 0) {
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
        };


        int period = Integer.parseInt(jobScraper.getDotEnv().get("RESET_TIME"));
        log.warn("The script will reset every " + period + " seconds. Until stopped.");
        executor.scheduleAtFixedRate(checkJobCardsTask, 0, period, TimeUnit.SECONDS);

        // Close the driver
//        log.info("Closing driver...");
//        Thread.sleep(1000 * 5);
//        driver.quit();
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