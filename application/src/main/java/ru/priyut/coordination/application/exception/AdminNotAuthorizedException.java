package ru.priyut.coordination.application.exception;

import ru.priyut.coordination.domain.shared.DomainException;

/**
 * Пользователь не является администратором.
 */
public class AdminNotAuthorizedException extends DomainException {

    public AdminNotAuthorizedException(Long telegramId) {
        super("User with telegram ID " + telegramId + " is not authorized as admin");
    }
}