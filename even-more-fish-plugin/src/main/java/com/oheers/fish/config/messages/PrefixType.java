package com.oheers.fish.config.messages;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.AbstractMessage;

public enum PrefixType {

    NONE(null, null),
    ADMIN("prefix-admin", "&c[EvenMoreFish] "),
    DEFAULT("prefix-regular", "&a[EvenMoreFish] "),
    ERROR("prefix-error", "&c[EvenMoreFish] ");

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
    public AbstractMessage getPrefix() {
        if (id == null) {
            return EvenMoreFish.getAdapter().createMessage("");
        } else {
            return EvenMoreFish.getAdapter().createMessage(Messages.getInstance().getConfig().getString(id, normal));
        }
    }
}
