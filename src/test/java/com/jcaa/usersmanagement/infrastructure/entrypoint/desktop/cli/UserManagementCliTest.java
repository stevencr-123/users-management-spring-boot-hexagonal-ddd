package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.LoginRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserManagementCli")
class UserManagementCliTest {

  @Test
  @DisplayName("maneja opciones, operaciones exitosas y errores de los handlers")
  void shouldRunMenuAndHandleOperations() {
    // Arrange
    final UserController userController = mock(UserController.class);
    final UserResponse response =
        new UserResponse("u-001", "Ada Lovelace", "ada@example.com", "ADMIN", "ACTIVE");
    final ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("invalid command");
    final ConstraintViolationException validationException =
        new ConstraintViolationException(Set.of(violation));

    when(userController.listAllUsers())
        .thenReturn(List.of())
        .thenReturn(List.of(response))
        .thenThrow(validationException)
        .thenThrow(new IllegalStateException("unexpected failure"));
    when(userController.findUserById("u-001")).thenReturn(response);
    when(userController.findUserById("missing"))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("missing"));
    when(userController.createUser(any(CreateUserRequest.class)))
        .thenReturn(response)
        .thenThrow(UserAlreadyExistsException.becauseEmailAlreadyExists("ada@example.com"));
    when(userController.updateUser(any(UpdateUserRequest.class)))
        .thenReturn(response)
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("missing"));
    doThrow(UserNotFoundException.becauseIdWasNotFound("missing"))
        .when(userController)
        .deleteUser("missing");
    when(userController.login(any(LoginRequest.class)))
        .thenReturn(response)
        .thenThrow(InvalidCredentialsException.becauseCredentialsAreInvalid());

    final String input =
        String.join(
            "\n",
            "not-a-number",
            "99",
            "1",
            "1",
            "1",
            "1",
            "2",
            "u-001",
            "2",
            "missing",
            "3",
            "",
            "u-002",
            "Ada Lovelace",
            "ada@example.com",
            "SecurePass123",
            "ADMIN",
            "3",
            "u-003",
            "Ada Lovelace",
            "ada@example.com",
            "SecurePass123",
            "ADMIN",
            "4",
            "u-001",
            "Ada Lovelace",
            "ada@example.com",
            "",
            "ADMIN",
            "ACTIVE",
            "4",
            "missing",
            "Ada Lovelace",
            "ada@example.com",
            "",
            "ADMIN",
            "ACTIVE",
            "5",
            "u-001",
            "5",
            "missing",
            "6",
            "ada@example.com",
            "SecurePass123",
            "6",
            "ada@example.com",
            "wrong",
            "0");
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    final ConsoleIO console = new ConsoleIO(new Scanner(input), new PrintStream(output));

    // Act
    new UserManagementCli(userController, console).start();
    final String result = output.toString();

    // Assert
    assertAll(
        () -> assertTrue(result.contains("Invalid input. Please enter a number.")),
        () -> assertTrue(result.contains("Invalid option. Please try again.")),
        () -> assertTrue(result.contains("No users found.")),
        () -> assertTrue(result.contains("Total: 1 user(s)")),
        () -> assertTrue(result.contains("Validation errors:")),
        () -> assertTrue(result.contains("invalid command")),
        () -> assertTrue(result.contains("Unexpected error: unexpected failure")),
        () -> assertTrue(result.contains("User created successfully.")),
        () -> assertTrue(result.contains("User updated successfully.")),
        () -> assertTrue(result.contains("User deleted successfully.")),
        () -> assertTrue(result.contains("Login successful. Welcome!")),
        () -> assertTrue(result.contains("Goodbye!")));
  }
}
