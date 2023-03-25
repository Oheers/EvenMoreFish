package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;

public enum PrefixType {

    NONE(null, null),
    ADMIN("prefix-admin", "&c"),
    DEFAULT("prefix-regular", "&a"),
    ERROR("prefix-error", "&c");

    private final String id, normal;

    /**
     * This contains the id and normal reference to the prefixes. These can be obtained through the getPrefix() method.
     *
     * @param id     The config id for the prefix.
     * @param normal The default values for the prefix.
     */
    PrefixType(final String id, final String normal) {
        this.id = id;
        this.normal = normal;
    }

    /**
     * Gives the associated prefix colour + the default plugin prefix by creating two Message objects and concatenating them.
     * If the PrefixType is NONE, then just "" is returned.
     *
     * @return The unformatted prefix, unless the type is NONE.
     */
    public String getPrefix() {
        if (id == null) return "";
        else {
            return new Message(EvenMoreFish.msgs.config.getString(id, normal)).getRawMessage(false, false)
                    + new Message(EvenMoreFish.msgs.config.getString("prefix", "[EvenMoreFish]") + "&r").getRawMessage(false, false);
        }
    }
}
