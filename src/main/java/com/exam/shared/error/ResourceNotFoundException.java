package com.exam.shared.error;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s introuvable : %s".formatted(resource, id));
    }
}
