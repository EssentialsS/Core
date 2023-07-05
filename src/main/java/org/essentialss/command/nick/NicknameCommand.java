package org.essentialss.command.nick;

import net.kyori.adventure.text.Component;
import org.essentialss.api.player.data.SGeneralUnloadedData;
import org.essentialss.api.utils.SParameters;
import org.essentialss.misc.CommandHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

public final class NicknameCommand {

    private static final class Remove implements CommandExecutor {

        private final @NotNull Parameter.Value<SGeneralUnloadedData> target;

        private Remove(@NotNull Parameter.Value<SGeneralUnloadedData> player) {
            this.target = player;
        }

        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            SGeneralUnloadedData player = CommandHelper.playerDataOrTarget(context, this.target);
            return NicknameCommand.execute(player, null);
        }
    }

    private static final class Execute implements CommandExecutor {

        private final @NotNull Parameter.Value<Component> newNickname;
        private final @NotNull Parameter.Value<SGeneralUnloadedData> target;

        private Execute(@NotNull Parameter.Value<SGeneralUnloadedData> player, @NotNull Parameter.Value<Component> newNickname) {
            this.newNickname = newNickname;
            this.target = player;
        }

        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            SGeneralUnloadedData player = CommandHelper.playerDataOrTarget(context, this.target);
            Component component = context.requireOne(this.newNickname);
            return NicknameCommand.execute(player, component);
        }
    }

    private NicknameCommand() {
        throw new RuntimeException("Should not create");
    }

    public static Command.Parameterized createNicknameCommand() {
        Parameter.Value<SGeneralUnloadedData> player = SParameters.offlinePlayersNickname(false, t -> true).key("target").optional().build();
        Parameter.Value<Component> message = Parameter.formattingCodeText().key("nickname").build();

        Command.Parameterized removeCommand = Command.builder().executor(new Remove(player)).addParameter(player).build();


        return Command.builder().executor(new Execute(player, message)).addParameter(message).addParameter(player).addChild(removeCommand, "remove").build();
    }

    public static CommandResult execute(@NotNull SGeneralUnloadedData player, @Nullable Component component) {
        player.setDisplayName(component);
        return CommandResult.success();
    }

}
