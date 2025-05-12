package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  @GetMapping("/")
  public String redirectToHome() {
    return "redirect:/home";
  }

  @GetMapping("/home")
  public String showHomePage() {
    return "home";
  }

  @GetMapping("/rps")
  public String showRPSPage() {
    return "rps";
  }

  @GetMapping("/val")
    public String showValFetcherPage() {
        return "val";
    }
}
