# Amazon Warehouse Job Scraper
This is a Java-based project designed to scrape Amazon warehouse job listings, primarily aimed at finding summer job
opportunities. I built this tool to circumvent issues with Amazon's notification system. This utility provides an
automated, reliable way to find and notify me of Amazon job vacancies.

## Disclaimer
This project was created for educational purposes and as a fun way to explore new technologies. It is not intended for
misuse or to violate any terms of service. Use it responsibly.

## Project Status
There are currently no plans for further updates to this project. It has reached a point where there isn't much else to
add, and I feel it has accomplished its purpose as a personal and educational project. I encourage you to fork and
experiment with it, using it as a base for your own exploration and learning. Remember, use it responsibly.

## Features
- Automatically scrapes Amazon's website for warehouse jobs.
- Provides real-time, up-to-date job listings.
- Filters jobs based on parameters such as job type (Full Time, Part Time), duration (Seasonal, Regular), and distance
  from a certain location.
- Sends notifications with job details using Discord Webhook.

## Prerequisites
You need Java 17 installed on your machine to run this application. You can download it
from [aws](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html).

## Getting Started
To get a local copy up and running, follow these steps:
1. **Clone the repo**
   `git clone https://github.com/joshuaportero/AmazonJobScraper.git`
2. **Install dependencies and compile the code**
   Navigate to the root of your project directory and then:
   `./gradlew build`
3. **Setup the environment variables**
   Create a `.env` file in your project directory and fill it as shown below (replace placeholders with your actual
   Twilio account details):

    ```
   # Website to be scraped
   AMAZON_URL=https://hiring.amazon.com/#/
   
   # Headless mode
   # 'true' to use new headless mode
   # 'false' to disable headless mode
   HEADLESS=true # Default: true

   # Time to wait between page loads in seconds
   RESET_TIME=30 # Default: 30 seconds
   NOTIFICATION_COOLDOWN=300 # Default: 5 minutes

   # Current Location
   LATITUDE=
   LONGITUDE=

   # SOON TO BE IMPLEMENTED
   # ZIP_CODE=10314 # TODO: Gather latitude and longitude from zip code

   # Discord Webhook URL
   DISCORD_WEBHOOK_URL=
   # Discord User ID to mention when a job is found (separated by commas)
   DISCORD_USER_IDS=

   # Job Filter
   TITLE=ANY # ANY, FULFILLMENT, SORTATION, DELIVERY, XL, GROCERY, DISTRIBUTION, CENTRAL, AIR, CUSTOMER_SERVICE,
   REMOTE_CUSTOMER_SERVICE, DELIVERY_STATION_CUSTOMER_SERVICE
   TYPE=ANY # ANY, FULL_TIME, PART_TIME, FLEX_TIME, REDUCED_TIME
   DURATION=ANY # ANY, SEASONAL, REGULAR

   PAY_RATE=ANY # ANY, Any double value

   BLACKLISTED_LOCATIONS=NONE # This could be a list of states or cities separated by commas Default: NONE (no blacklist)

   # Radius from current location to job
   DISTANCE=30 # Default: 30 miles
   
   # SOON TO BE IMPLEMENTED FILTERS
   # TODO: Access an specific job and retrieve job schedule and ID
   # SCHEDULE=ANY # ANY, DAY, NIGHT, EVENING, MORNING, WEEKEND, WEEKDAY
    ```

4. **Run the application**
   `./gradlew shadowJar`

## Running the Pre-compiled JAR
If you want to run the pre-compiled JAR file instead of compiling the code yourself, you can follow these steps:

1. **Download the latest release**
   Go to the [Releases](https://github.com/joshuaportero/AmazonJobScraper/releases) page of this repository and download
   the latest JAR file.
2. **Setup the environment variables**
   Create a `.env` file in the same directory as your JAR file and fill it as shown above.
3. **Run the JAR file**
   Use the following command to run the JAR file:
   `java -jar AmazonJobScraper.jar`

## Usage
Use this application to stay updated with Amazon warehouse job listings. Once a job meeting the set criteria becomes
available, you'll receive a notification through Twilio.

## Contributing
Contributions are very welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
Distributed under the MIT License.
See [`LICENSE`](https://github.com/joshuaportero/amazon-job-scrapper/blob/jobScraper/LICENSE) for more information.


