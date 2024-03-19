package it.paolinucs.ramenpersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import it.paolinucs.ramenpersist.config.CommandLineOptions;

@SpringBootApplication
public class RamenPersistApplication implements CommandLineRunner {

  @Autowired
  private CommandLineOptions commandLineOptions;

  public static void main(String[] args) {
    SpringApplication.run(RamenPersistApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    commandLineOptions.handleOptions(args);
  }

}