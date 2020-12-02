package com.finsther.evaluationscriptlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public final class App {

  private static final Logger log = Logger.getLogger(App.class);

  static {
    PropertyConfigurator.configure(
        App.class.getClassLoader().getResourceAsStream("logger/log4j.properties"));
  }

  private App() {}

  /** @param args The arguments of the program. */
  public static void main(String[] args) {
    /* initialize logger */

    EvaluationScriptlet eScriptlet = new EvaluationScriptlet();

    if (args.length < 2) {
      log.info("parametros incompletos");
      return;
    }

    log.info(
        String.format("%s", eScriptlet.convertNumberToWords(args[0], Boolean.valueOf(args[1]))));
  }
}
