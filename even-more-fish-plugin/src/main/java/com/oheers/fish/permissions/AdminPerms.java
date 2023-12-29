package com.oheers.fish.permissions;

public class AdminPerms {
    private AdminPerms() {
        throw new UnsupportedOperationException();
    }

    public static final String ADMIN = "emf.admin";
    public static final String UPDATE_NOTIFY = "emf.admin.update.notify";
    public static final String MIGRATE = "emf.admin.migrate";
}
