package TayJVLearn.StartJV.Demo.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Gender {
    @JsonProperty("male") MALE,
    @JsonProperty("female") FEMALE,
    @JsonProperty("other") OTHER;

    public static class NotFoundException extends RuntimeException {

    }
}
