package com.tradebot.controller.mvc;

import com.tradebot.service.BitmexTradingBot;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeMvcController {

    private final BitmexTradingBot bitmexTradingBot;

    private final Environment environment;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("symbolList", bitmexTradingBot.getAllSymbols());
        model.addAttribute("activeProfiles", Stream.of(environment.getActiveProfiles()).collect(Collectors.toList()));

        return "home";
    }

    @GetMapping("/wstest")
    public String wstestPage(Model model) {
        model.addAttribute("appName", appName);
        return "wstest";
    }
}
