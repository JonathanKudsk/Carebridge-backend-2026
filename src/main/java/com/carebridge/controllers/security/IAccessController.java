package com.carebridge.controllers.security;

import io.javalin.http.Context;

public interface IAccessController {
    void accessHandler(Context ctx);
}
