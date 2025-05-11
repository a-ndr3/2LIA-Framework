package com.espertech.ESPERQueries;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IDCreator implements IQueryID {
    private static IDCreator instance;
    private static HashSet<UUID> ids = new HashSet<>();

    private IDCreator() {
    }

    public static IDCreator getInstance() {
        if (instance == null) {
            instance = new IDCreator();
        }
        return instance;
    }

    @Override
    public String getQueryIDString() {
        return getQueryID().toString();
    }

    @Override
    public UUID getQueryID() {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (ids.contains(id));
        ids.add(id);
        return id;
    }
}
