package com.tradebot.controller.mvc;

import com.tradebot.service.BitmexTradingBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeMvcController {

    private final BitmexTradingBot bitmexTradingBot;

    private final Environment environment;

    private final BuildProperties buildProperties;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("symbolList", bitmexTradingBot.getAllSymbols());
        model.addAttribute("buildVersion", buildProperties.getVersion() + ":" + buildProperties.getArtifact() + ":" +
            buildProperties.getTime().toString());
        model.addAttribute("activeProfiles", String.join(", ", environment.getActiveProfiles()));

        return "home";
    }

    @GetMapping("/wstest")
    public String wstestPage(Model model) {
        model.addAttribute("appName", appName);
        return "wstest";
    }
}
