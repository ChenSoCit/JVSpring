package TayJVLearn.StartJV.Demo.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserType {
    @JsonProperty("user")
    USER,
    @JsonProperty("admin")
    ADMIN
}
