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
            title = linesList.get(0);
            if (title.startsWith("P") || title.startsWith("B") || title.startsWith("Featured")) {
                linesList.remove(0);
            }
            shifts = Integer.parseInt(linesList.get(1).split(" ")[0]);
            if (linesList.get(2).contains(";")) {
                linesList.set(2, linesList.get(2).replace(";", ",")); // Fix for job type parsing
            }
            jobType = Arrays.stream(linesList.get(2).split(": ")[1].split(", "))
                    .map(type -> {
                        try {
                            return JobType.valueOf(type.replace(" ", "_").toUpperCase());
                        } catch (IllegalArgumentException e) {
                            log.warn("Unknown job type encountered: {}. Mapping to UNKNOWN.", type);
                            return JobType.UNKNOWN;
                        }
                    })
                    .toArray(JobType[]::new);
            jobDuration = Arrays.stream(linesList.get(3).split(": ")[1].split(", "))
                    .map(duration -> JobDuration.valueOf(duration.replace(" ", "_").toUpperCase()))
                    .toArray(JobDuration[]::new);
            pay = Double.parseDouble(linesList.get(4).split("\\$")[1]);
            location = linesList.get(5).split("\\| ")[1];
            distance = Double.parseDouble(linesList.get(5).split(" ")[1]);
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
