package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${DATABASE_URL:}") String databaseUrl,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {

        DataSourceBuilder<?> builder = DataSourceBuilder.create();

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            ParsedDatabaseUrl parsed = ParsedDatabaseUrl.parse(databaseUrl);
            builder.url(parsed.jdbcUrl())
                    .username(parsed.username())
                    .password(parsed.password());
        } else {
            builder.url(url).username(username).password(password);
        }

        return builder.build();
    }

    record ParsedDatabaseUrl(String jdbcUrl, String username, String password) {

        static ParsedDatabaseUrl parse(String databaseUrl) {
            String withoutScheme = databaseUrl.trim().replaceFirst("^(?i)postgres(ql)?://", "");

            int separator = withoutScheme.lastIndexOf('@');
            String userInfo = separator >= 0 ? withoutScheme.substring(0, separator) : null;
            String hostPortAndPath = separator >= 0 ? withoutScheme.substring(separator + 1) : withoutScheme;

            String username = null;
            String password = null;
            if (userInfo != null && !userInfo.isBlank()) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    username = decode(userInfo.substring(0, colon));
                    password = decode(userInfo.substring(colon + 1));
                } else {
                    username = decode(userInfo);
                }
            }

            return new ParsedDatabaseUrl("jdbc:postgresql://" + hostPortAndPath, username, password);
        }

        /**
         * Percent-decodes a userinfo component. A literal '+' must survive, so it is
         * escaped before decoding rather than being interpreted as a space.
         */
        private static String decode(String value) {
            return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
        }
    }
}
