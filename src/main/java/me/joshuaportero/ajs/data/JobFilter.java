package me.joshuaportero.ajs.data;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

@Slf4j
public class JobFilter {

    private final String fieldName;
    private final String defaultValue;
    private final BiPredicate<JobData, String> biPredicate;

    public JobFilter(String fieldName, String defaultValue, BiPredicate<JobData, String> biPredicate) {
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
        this.biPredicate = biPredicate;
    }

    public void apply(Dotenv dotenv, List<JobData> jobsData) {
        String[] jobFieldValues = dotenv.get(fieldName).split(",");
        List<String> jobFieldValueList = Arrays.asList(jobFieldValues);
        if (jobFieldValueList.stream().noneMatch(defaultValue::equalsIgnoreCase)) {
            log.debug("Filtering jobs by " + fieldName + "...");
            jobsData.removeIf(jobData -> jobFieldValueList.stream().noneMatch(jobFieldValue -> biPredicate.test(jobData, jobFieldValue)));
            log.debug("There are " + jobsData.size() + " jobs remaining after filtering by " + fieldName + ".");
        }
    }

}
