package com.tradebot.app;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;

@SpringBootApplication(exclude = {JmxAutoConfiguration.class})
@Slf4j
public class TradeBotApp implements CommandLineRunner {


    public static void main(String[] args) {
        log.info("STARTING THE APPLICATION");
        SpringApplication.run(TradeBotApp.class, args);
        log.info("APPLICATION FINISHED");

    }

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            log.info("args[{}]: {}", i, args[i]);
        }
    }

}
