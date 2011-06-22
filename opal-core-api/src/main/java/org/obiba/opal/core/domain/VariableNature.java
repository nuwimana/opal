package org.obiba.opal.core.domain;

import org.obiba.magma.Category;
import org.obiba.magma.Variable;

/**
 * Determines the nature of a variable by inspecting its {@code ValueType} and any associated {@code Category} instance.
 */
public enum VariableNature {

  /**
   * A categorical variable: its value can take one of the predefined {@code Category}.
   */
  CATEGORICAL,
  /**
   * A continuous variable: its value can take any value of it's {@code ValueType}. Some values may have a particular
   * meaning: they indicate a missing value. These are defined as missing {@code Category} intances.
   */
  CONTINUOUS,
  /**
   * A temporal variable: it's value is a date or time.
   */
  TEMPORAL,
  /**
   * None of the above. Variables with {@code LocaleType} will be of this nature.
   */
  UNDETERMINED;

  public static VariableNature getNature(Variable variable) {
    if(variable.hasCategories()) {
      if(isAllMissing(variable.getCategories()) == false) {
        return CATEGORICAL;
      } else {
        return CONTINUOUS;
      }
    }
    if(variable.getValueType().isNumeric()) {
      return CONTINUOUS;
    }
    if(variable.getValueType().isDateTime()) {
      return TEMPORAL;
    }
    return UNDETERMINED;
  }

  private static boolean isAllMissing(Iterable<Category> categories) {
    for(Category c : categories) {
      if(c.isMissing() == false) return false;
    }
    return true;
  }

}
