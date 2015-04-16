package org.javalite.templator.template_parser;

import java.util.Map;
import org.javalite.common.Convert;

/**
 * @author Eric Nielsen
 */
class BooleanId extends Exp {
    private final ChainedIds chainedIds;

    BooleanId(ChainedIds chainedIds) {
        this.chainedIds = chainedIds;
    }

    @Override
    boolean resultWith(Map values) {
        return Convert.toBoolean(chainedIds.valueFrom(values));
    }
}
