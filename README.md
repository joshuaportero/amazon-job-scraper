# Amazon Warehouse Job Scraper

This is a Java-based project designed to scrape Amazon warehouse job listings, primarily aimed at finding summer job opportunities. I built this tool to circumvent issues with Amazon's notification system. This utility provides an automated, reliable way to find and notify me of Amazon job vacancies.

## Disclaimer

This project was created for educational purposes and as a fun way to explore new technologies. It is not intended for misuse or to violate any terms of service. Use it responsibly.

## Features

- Automatically scrapes Amazon's website for warehouse jobs.
- Provides real-time, up-to-date job listings.
- Filters jobs based on parameters such as job type (Full Time, Part Time), duration (Seasonal, Regular), and distance from a certain location.
- Sends notifications with job details using Twilio.

## Prerequisites

You need Java 17 installed on your machine to run this application. You can download it from [aws](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html).

## Getting Started

To get a local copy up and running, follow these steps:

1. **Clone the repo**

    `git clone https://github.com/joshuaportero/AmazonJobScraper.git`

2. **Install dependencies and compile the code**

   Navigate to the root of your project directory and then:

    `./gradlew build`

3. **Setup the environment variables**

    Create a `.env` file in your project directory and fill it as shown below (replace placeholders with your actual Twilio account details):

    ```
    # Website to be scraped
    AMAZON_URL=https://hiring.amazon.com/#/

    # Headless mode
    # 'new' to use new headless mode
    # 'false' to disable headless mode
    HEADLESS=false

    # Time to wait between page loads in seconds
    RESET_TIME=0

    # Current Location
    LATITUDE=0
    LONGITUDE=0

    # Twilio SMS Credentials
    TWILIO_ACCOUNT_SID=
    TWILIO_AUTH_TOKEN=
    TWILIO_FROM_NUMBER=
    TWILIO_TO_NUMBER=

    # Radius from current location to job
    MAX_RADIUS=12
    ```

4. **Run the application**

    `./gradlew run`

## Running the Pre-compiled JAR

If you want to run the pre-compiled JAR file instead of compiling the code yourself, you can follow these steps:

1. **Download the latest release**

   Go to the [Releases](https://github.com/joshuaportero/amazon-job-scrapper/releases) page of this repository and download the latest JAR file.

2. **Setup the environment variables**

    Create a `.env` file in the same directory as your JAR file and fill it as shown above.

3. **Run the JAR file**

   Use the following command to run the JAR file:

   `java -jar AmazonJobScraper.jar`

## Usage

Use this application to stay updated with Amazon warehouse job listings. Once a job meeting the set criteria becomes available, you'll receive a notification through Twilio.

## Contributing

Contributions are very welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See [`LICENSE`](https://github.com/joshuaportero/amazon-job-scrapper/blob/main/LICENSE) for more information.


