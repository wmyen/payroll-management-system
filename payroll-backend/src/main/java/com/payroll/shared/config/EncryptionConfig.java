package com.payroll.shared.config;

import com.payroll.shared.util.EncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class EncryptionConfig {

    @Bean
    public EncryptionUtil encryptionUtil(@Value("${encryption.aes-key}") String aesKey) {
        return new EncryptionUtil(aesKey);
    }
}
