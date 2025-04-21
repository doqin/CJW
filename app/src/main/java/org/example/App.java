package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
    System.out.println("Web app is running!");
  }

  /*
   * @Bean
   * public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
   * return args -> {
   * System.out.println("Let's inspect the beans provided by Spring Boot:");
   * 
   * String[] beanNames = ctx.getBeanDefinitionNames();
   * Arrays.sort(beanNames);
   * for (String beanName : beanNames) {
   * System.out.println(beanName);
   * }
   * System.out.println("Web App is running");
   * };
   * }
   */
}
