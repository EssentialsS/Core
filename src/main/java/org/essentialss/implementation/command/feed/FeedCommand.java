package org.essentialss.implementation.command.feed;

import org.essentialss.implementation.permissions.permission.SPermissions;
import org.spongepowered.api.command.Command;

public final class FeedCommand {

    private FeedCommand() {
        throw new RuntimeException("Should not generate");
    }

    public static Command.Parameterized createFeedCommand() {
        return SetFoodLevelCommand
                .createFoodCommand(Command.builder())
                .permission(SPermissions.FEED_SELF.node())
                .addChild(SetFoodLevelCommand.createFoodCommand(), "set")
                .addChild(SetUnlimitedFoodCommand.createSetUnlimitedFoodCommand(), "unlimited")
                .build();
    }

}
