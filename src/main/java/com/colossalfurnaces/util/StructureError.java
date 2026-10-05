package com.colossalfurnaces.util;

public enum StructureError {
    NOT_SHELL("not_shell"),
    TOO_SMALL("too_small"),
    TOO_LARGE("too_large"),
    NOT_CUBE("not_cube"),
    MULTIPLE_CONTROLLERS("multiple_controllers"),
    MISSING_CONTROLLER("missing_controller"),
    MISSING_BOUNDARY_BLOCK("missing_boundary_block"),
    INTERIOR_NOT_EMPTY("interior_not_empty"),
    NOT_CONNECTED("not_connected");

    private final String key;

    StructureError(String key) {
        this.key = key;
    }

    public String translationKey() {
        return "message.colossalfurnaces.structure." + this.key;
    }
}
