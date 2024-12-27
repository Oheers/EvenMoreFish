package com.oheers.fish.database.extras;

import org.jetbrains.annotations.NotNull;
import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;
import org.jooq.tools.StringUtils;

// This is needed to ensure prefix placeholders don't stay during code generation.
public class PrefixNamingStrategy extends DefaultGeneratorStrategy {

    @Override
    public String getJavaClassName(final Definition definition, final Mode mode) {
        String name = replacePrefix(super.getJavaClassName(definition, mode));

        return StringUtils.toUC(name);
    }

    @Override
    public String getJavaIdentifier(final Definition definition) {
        return replacePrefix(super.getJavaIdentifier(definition));
    }

    private @NotNull String replacePrefix(final @NotNull String name) {
        return name
                .replaceAll("\\$\\{(TABLEPREFIX|tablePrefix|table_Prefix|TABLE_PREFIX)}", "")  // Matches variations of ${TABLEPREFIX}
                .replaceAll("\\$_7b(TABLEPREFIX|tablePrefix|table_Prefix|TABLE_PREFIX)_7d", "") // Matches variations of $_7bTABLEPREFIX_7d
                .replaceAll("\\$\\{.*?}", "") // Generic fallback for any unmatched ${PLACEHOLDER}
                .replaceAll("\\$_7b.*?_7d", ""); // Generic fallback for any unmatched $_7bPLACEHOLDER_7d
    }


}
