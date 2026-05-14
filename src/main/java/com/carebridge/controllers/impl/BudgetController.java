package com.carebridge.controllers.impl;

import com.carebridge.dtos.CreateBudgetRequestDTO;
import com.carebridge.services.BudgetService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BudgetController {

    private static final Logger logger =
            LoggerFactory.getLogger(BudgetController.class);

    private final BudgetService budgetService =
            new BudgetService();

    public void create(Context ctx) {

        try {

            CreateBudgetRequestDTO request =
                    ctx.bodyAsClass(CreateBudgetRequestDTO.class);

            var response = budgetService.createBudget(request);

            ctx.status(201);

            ctx.json(response);

        } catch (IllegalArgumentException e) {

            ctx.status(400).result(e.getMessage());

        } catch (Exception e) {

            logger.error("Failed to create budget", e);

            ctx.status(500).result("Internal server error");
        }
    }
}