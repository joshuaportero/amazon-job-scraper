package me.joshuaportero.ajs.api;

import lombok.extern.slf4j.Slf4j;
import me.joshuaportero.ajs.data.JobData;
import me.joshuaportero.ajs.data.JobDuration;
import me.joshuaportero.ajs.data.JobType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JobDataAPI {
    public static JobData fromStringToJobData(String jobCard) {

        String title;
        int shifts;
        JobType[] jobType;
        JobDuration[] jobDuration;
        double pay;
        String location;
        double distance;

        try {
            List<String> linesList = new ArrayList<>(Arrays.asList(jobCard.split("\n")));
            log.debug("Job card lines: " + linesList);
            title = linesList.get(0);
            if (title.startsWith("P") || title.startsWith("B") || title.startsWith("Featured")) {
                linesList.remove(0);
            }
            int line = 1;
            try {
                shifts = Integer.parseInt(linesList.get(line).split(" ")[0]);
                line += 1;
            } catch (NumberFormatException e) {
                shifts = -1;
            }
            if (linesList.get(line).contains(";")) {
                linesList.set(line, linesList.get(line).replace(";", ",")); // Fix for job type parsing
            }
            jobType = Arrays.stream(linesList.get(line).split(": ")[1].split(", "))
                    .map(type -> {
                        try {
                            return JobType.valueOf(type.replace(" ", "_").toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.warn("Unknown job type encountered: {}. Mapping to UNKNOWN.", type);
                            return JobType.UNKNOWN;
                        }
                    })
                    .toArray(JobType[]::new);
            line += 1;
            jobDuration = Arrays.stream(linesList.get(line).split(": ")[1].split(", "))
                    .map(duration -> JobDuration.valueOf(duration.replace(" ", "_").toUpperCase()))
                    .toArray(JobDuration[]::new);
            line += 1;
            pay = Double.parseDouble(linesList.get(line).split("\\$")[1]);
            line += 1;
            location = linesList.get(line).split("\\| ")[1];

            // Distance can contain "Within %num% mi" or "%num% miles"
            try {
                distance = Double.parseDouble(linesList.get(line).split(" ")[1]);
            } catch (NumberFormatException e) {
                try {
                    distance = Double.parseDouble(linesList.get(line).split(" ")[0]);
                } catch (NumberFormatException e2) {
                    distance = -1;
                }
            }
        } catch (Exception e) {
            log.error("Error parsing job data: " + e.getMessage());
            return null;
        }

        return JobData.builder()
                .title(title)
                .shifts(shifts)
                .jobType(jobType)
                .jobDurations(jobDuration)
                .pay(pay)
                .location(location)
                .distance(distance)
                .build();
    }
}
