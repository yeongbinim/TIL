package com.example.outsourcing.config;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class CustomFunctionContributor implements FunctionContributor {

    private static final String FUNCTION1_NAME = "match_1params_against";
    private static final String FUNCTION1_PATTERN = "MATCH (?1) AGAINST (?2 in boolean mode)";

    private static final String FUNCTION2_NAME = "match_2params_against";
    private static final String FUNCTION2_PATTERN = "MATCH (?1, ?2) AGAINST (?3 in boolean mode)";

    @Override
    public void contributeFunctions(final FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry()
            .registerPattern(FUNCTION1_NAME, FUNCTION1_PATTERN,
                functionContributions.getTypeConfiguration().getBasicTypeRegistry()
                    .resolve(DOUBLE));
        functionContributions.getFunctionRegistry()
            .registerPattern(FUNCTION2_NAME, FUNCTION2_PATTERN,
                functionContributions.getTypeConfiguration().getBasicTypeRegistry()
                    .resolve(DOUBLE));
    }
}
