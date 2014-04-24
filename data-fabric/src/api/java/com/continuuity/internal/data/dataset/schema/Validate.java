package com.continuuity.internal.data.dataset.schema;

/**
 * Indicates whether records should be validated against their schema.
 */
public enum Validate {

  /**
   * Validate the entire schema recursively.
   */
  STRICT,

  /**
   * Validate the entire schema, but assume that maps and lists are homogenous, that is,
   * if one element/entry passes validation, then the remaining elements/entries do not
   * need to be validated.
   */
  RELAXED,

  /**
   * Only validate the top-level type, but not its parameters. For example, if a field
   * type is a list, validate that the field value is a list, but do not validate the
   * types of the elements of the list.
   */
  SHALLOW,

  /**
   * Do not validate at all.
   */
  OFF
}
