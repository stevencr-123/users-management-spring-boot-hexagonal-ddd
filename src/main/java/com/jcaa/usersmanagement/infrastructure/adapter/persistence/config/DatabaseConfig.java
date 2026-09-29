package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

public record DatabaseConfig(
        String host,
        int port,
        String databaseName,
        String username,
        String password,
        String sslMode) {

    private static final String URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s?stringtype=unspecified&sslmode=%s";

    // Constructor sobrecargado para compatibilidad hacia atrás (por defecto disable)
    public DatabaseConfig(String host, int port, String databaseName, String username, String password) {
        this(host, port, databaseName, username, password, "disable");
    }

    public String buildJdbcUrl() {
        return String.format(URL_TEMPLATE, host, port, databaseName,
                (sslMode == null || sslMode.isBlank()) ? "disable" : sslMode);
    }
}