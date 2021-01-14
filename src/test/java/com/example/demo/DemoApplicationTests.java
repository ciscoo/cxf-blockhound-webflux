package com.example.demo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(locations = "classpath:test.properties")
class DemoApplicationTests {

    private static WireMockServer wireMockServer;

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(wireMockConfig().notifier(new Slf4jNotifier(true)));
        wireMockServer.start();
        System.setProperty("wiremock-port", String.valueOf(wireMockServer.port()));
        BlockHound.install();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
        System.clearProperty("wiremock-port");
    }

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToApplicationContext(this.applicationContext).build();
    }

    @Test
    void shouldNotBlock() {
        this.webTestClient.get().uri("/").exchange()
                .expectStatus()
                    .is2xxSuccessful()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .isEqualTo(Collections.singletonMap("result", 2));
    }

}
