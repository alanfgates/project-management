package com.github.alanfgates.project.management;

public interface InputValidator<T> {

  /**
   * Used by TextUI to decide if this is a valid input.
   * @param input String to validate.
   * @return transformed input, of the correct type
   * @throws InvalidInputException if the text is not valid.
   */
  T validate(String input) throws InvalidInputException;
}
