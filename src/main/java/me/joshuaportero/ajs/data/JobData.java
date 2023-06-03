package me.joshuaportero.ajs.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobData {
    private String title;
    private int shifts;
    private JobType[] jobTypes;
    private JobDuration[] jobDurations;
    private double pay;
    private String location;
    private double distance;

}
