package com.util.handler;

/**
 *  A generic event handler.
 */
@FunctionalInterface
public interface BiHandler<T, E> {

  /**
   * Something has happened, so handle it.
   *
   */
  void handle(T t, E e);
}