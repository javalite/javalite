package org.javalite.json;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract class MixIn {
    @JsonIgnore
    abstract int metaModelLocal(); // we don't need it!
}
