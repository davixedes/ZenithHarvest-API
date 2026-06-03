package com.fiap.zenith.core.application.exception;

/**
 * Lançada quando uma operação viola uma regra de unicidade de negócio
 * (ex.: criar uma fazenda com um CAR já cadastrado). Mapeada para HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
