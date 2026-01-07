package com.minimall.auth;

import com.minimall.auth.jwt.RefreshTokenStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration
public class TestRefreshTokenStoreConfig {

    @Bean
    @Primary
    public RefreshTokenStore refreshTokenStore() {
        return new InMemoryRefreshTokenStore();
    }

    static class InMemoryRefreshTokenStore implements RefreshTokenStore {
        private final Map<Long, String> store = new ConcurrentHashMap<>();

        @Override
        public void save(Long memberId, String refreshJti, Duration ttl) {
            store.put(memberId, refreshJti);
        }

        @Override
        public String findJti(Long memberId) {
            return store.get(memberId);
        }

        @Override
        public void delete(Long memberId) {
            store.remove(memberId);
        }
    }
}
