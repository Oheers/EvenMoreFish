package com.oheers.fish.config.messages;

public enum PrefixType {

    NONE(null, null),
    ADMIN("prefix-admin", "&c"),
    DEFAULT("prefix-regular", "&a"),
    ERROR("prefix-error", "&c");

    private final String id;
    private final String normal;

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
        if (id == null) {
            return "";
        }
        final Messages messages = Messages.getInstance();
        return new Message(messages.getConfig().getString(id, normal)).getRawMessage(false)
                    + new Message(messages.getConfig().getString("prefix", "[EvenMoreFish]") + "&r").getRawMessage(false);

    }
}
