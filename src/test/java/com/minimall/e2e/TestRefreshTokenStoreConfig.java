package com.minimall.e2e;

import com.minimall.auth.jwt.RefreshTokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Profile("e2e")
public class TestRefreshTokenStoreConfig {

    @Bean
    @Primary
    public RefreshTokenStore refreshTokenStore() {
        return new InMemoryRefreshTokenStore();
    }

    private static class InMemoryRefreshTokenStore implements RefreshTokenStore {
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
