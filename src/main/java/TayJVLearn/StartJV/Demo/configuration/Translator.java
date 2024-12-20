package TayJVLearn.StartJV.Demo.configuration;


import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Translator {
    private static ResourceBundleMessageSource messageSource;

    public Translator(ResourceBundleMessageSource messageSource){ // khoi tao
        Translator.messageSource= messageSource;
    }
    public static String toLocale(String msgCode){
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, null, locale);
    }
}
