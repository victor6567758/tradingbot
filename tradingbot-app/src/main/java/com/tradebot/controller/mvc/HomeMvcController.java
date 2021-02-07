package com.tradebot.controller.mvc;

import com.tradebot.service.impl.BitmexTradingBotImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeMvcController {

    private final BitmexTradingBotImpl bitmexTradingBot;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("symbolList", bitmexTradingBot.getAllSymbols());
        return "home";
    }

    @GetMapping("/wstest")
    public String wstestPage(Model model) {
        model.addAttribute("appName", appName);
        return "wstest";
    }
}
