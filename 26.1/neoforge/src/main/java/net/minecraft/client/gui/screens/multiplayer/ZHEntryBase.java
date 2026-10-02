package net.minecraft.client.gui.screens.multiplayer;

/**
 * Bridge class located in the multiplayer screens package to implement package-private matches()
 * without triggering Java package access or AbstractMethodError restrictions in Minecraft 26.1.
 */
public abstract class ZHEntryBase extends ServerSelectionList.Entry {

    @Override
    boolean matches(ServerSelectionList.Entry other) {
        return this.matchesEntry(other);
    }

    public boolean matchesEntry(ServerSelectionList.Entry other) {
        return this == other;
    }
}
