package com.tradebot.bitmex;

import com.tradebot.bitmex.service.BitmexTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class BitMexAnalytic implements CommandLineRunner {

    private final BitmexTransactionService bitmexTransactionService;

    public static void main(String[] args) {
        SpringApplication.run(BitMexAnalytic.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            bitmexTransactionService.saveNewTransactions();
        } catch (RuntimeException re) {
          log.error("Error", re);
        }
    }
}
