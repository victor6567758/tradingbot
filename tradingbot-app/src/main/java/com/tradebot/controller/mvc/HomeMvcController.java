package com.tradebot.controller.mvc;

import com.tradebot.bitmex.restapi.config.BitmexAccountConfiguration;
import com.tradebot.response.ExecutionResponse;
import com.tradebot.service.TradingBotApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class HomeMvcController {

    private final TradingBotApi tradingBotApi;

    private final Environment environment;

    private final BuildProperties buildProperties;

    private final BitmexAccountConfiguration bitmexAccountConfiguration;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("symbolList", tradingBotApi.getAllSymbols());
        model.addAttribute("buildVersion", createBuildInfo());
        model.addAttribute("activeProfiles", String.join(", ", environment.getActiveProfiles()));

        return "home";
    }

    @GetMapping("/levelinfo")
    public String levelInfo(Model model, @RequestParam int level, @RequestParam String symbol) {
        model.addAttribute("level", level);

        List<ExecutionResponse> levelExecutions = tradingBotApi.getLastExecutionResponseList(symbol, level);
        if (levelExecutions == null) {
            throw new IllegalArgumentException("Invalid data for passed level");
        }

        model.addAttribute("levelExecutions", levelExecutions);
        return "levelinfo";
    }

    @GetMapping("/logviewer")
    public String logViewer(Model model) {

        model.addAttribute("elasticUrl", UriComponentsBuilder.fromUriString(bitmexAccountConfiguration.getBitmex().getElastic().getUrl())
            .pathSegment(bitmexAccountConfiguration.getBitmex().getElastic().getIndex())
            .build().toUriString());

        return "logviewer";
    }

    private String createBuildInfo() {
        return buildProperties.getVersion() +
            ":" + buildProperties.getArtifact() +
            ":" + (buildProperties.getTime() != null ? buildProperties.getTime().toString() : "N/A");
    }
}
