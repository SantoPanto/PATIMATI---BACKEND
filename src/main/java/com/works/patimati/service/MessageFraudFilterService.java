package com.works.patimati.service;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageFraudFilterService {
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:(?:\\+?90|0)?\\s*5\\d{2}[\\s.-]*\\d{3}[\\s.-]*\\d{2}[\\s.-]*\\d{2})",
            Pattern.CASE_INSENSITIVE
    );
    //IBAN
    private static final Pattern IBAN_PATTERN = Pattern.compile(
            "TR\\s*\\d{2}[\\s*\\d{4}]{5}\\s*\\d{2}",
            Pattern.CASE_INSENSITIVE
    );
    // Anahtar kelimeler devam edilebilir
    private static final Pattern SUSPICIOUS_WORDS_PATTERN = Pattern.compile(
            "\\b(iban|kapora|havale|eft|sahibinden|dolap|gardrops|whatsapp|wp|hesabima at|parayi gonder)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final String SENSORY_REPLACEMENT = "[SANSÜRLÜ - KİŞİSEL VERİ/GÜVENLİK İHLALİ]";
    public String filterContent(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return rawContent;
        }
        String filteredContent = rawContent;
        Matcher ibanMatcher = IBAN_PATTERN.matcher(filteredContent);
        if (ibanMatcher.find()) {
            filteredContent = ibanMatcher.replaceAll(SENSORY_REPLACEMENT);
        }
        Matcher phoneMatcher = PHONE_PATTERN.matcher(filteredContent);
        if (phoneMatcher.find()) {
            filteredContent = phoneMatcher.replaceAll(SENSORY_REPLACEMENT);
        }
        Matcher wordMatcher = SUSPICIOUS_WORDS_PATTERN.matcher(filteredContent);
        if (wordMatcher.find()) {
            filteredContent = wordMatcher.replaceAll(SENSORY_REPLACEMENT);
        }
        return filteredContent;
    }
}