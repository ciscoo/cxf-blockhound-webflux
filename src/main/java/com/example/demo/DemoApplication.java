package com.example.demo;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.tempuri.AddResponse;
import org.tempuri.CalculatorSoap;
import reactor.core.publisher.Mono;

import java.util.Collections;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> router(RouteHandler routeHandler) {
        return RouterFunctions.route().GET("/", routeHandler).build();
    }

    @Bean
    CalculatorSoap calculator(@Value("${calculator-address}") String url) {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(CalculatorSoap.class);
        factoryBean.setAddress(url);
        return (CalculatorSoap) factoryBean.create();
    }

    @Bean
    RouteHandler routeHandler(CalculatorSoap calculatorSoap) {
        return new RouteHandler(calculatorSoap);
    }

    static class RouteHandler implements HandlerFunction<ServerResponse> {

        private final CalculatorSoap calculator;

        public RouteHandler(CalculatorSoap calculator) {
            this.calculator = calculator;
        }

        @Override
        public Mono<ServerResponse> handle(ServerRequest request) {
            return Mono.<AddResponse>create(sink -> {
                calculator.addAsync(1, 1, handler -> {
                    try {
                        sink.success(handler.get());
                    } catch (Exception ex) {
                        sink.error(ex);
                    }
                });
            })
                    .flatMap(response -> ServerResponse.ok()
                            .bodyValue(Collections.singletonMap("result", response.getAddResult())));
        }
    }
}
