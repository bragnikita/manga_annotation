package jp.bragnikita.manan.backend.controllers.utils;

import org.springframework.util.StringUtils;

import java.util.Arrays;

public class ValidationHelper {

    public static void assertNotPresentTogether(String message, String ...params) {
        if (Arrays.stream(params).filter(StringUtils::hasText).count() > 1) {
            throw new InvalidRequestException(message);
        }
    }
}
