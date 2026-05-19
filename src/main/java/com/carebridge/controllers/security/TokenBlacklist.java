package com.carebridge.controllers.security;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBlacklist {

    private static final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void add(String token) {
        blacklist.add(token);
    }

    public static boolean contains(String token) {
        return blacklist.contains(token);
    }
}
