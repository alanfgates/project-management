package com.github.alanfgates.project.management;

class InputCollector<T> {

  private final String prompt;
  private final InputValidator<T> validator;
  private T input;

  InputCollector(String prompt, InputValidator<T> validator) {
    this.prompt = prompt;
    this.validator = validator;
  }

  String getPrompt() {
    return prompt;
  }

  T getInput() {
    return input;
  }

  void collect(String input) throws InvalidInputException {
    this.input = validator.validate(input);
  }
}
