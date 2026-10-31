package com.deep.studenthousing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Language support (English / Bengali).
 *
 * The chosen language is remembered in a cookie ("sh-lang") and switched with
 * a "?lang=en" or "?lang=bn" query parameter. English stays the default, so
 * every page that does not use message keys behaves exactly as before.
 * Right now only the property-owner pages are translated.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale BENGALI = Locale.forLanguageTag("bn");

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("sh-lang");
        resolver.setDefaultLocale(ENGLISH);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        // Ignore anything other than the supported languages (e.g. ?lang=xx).
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
