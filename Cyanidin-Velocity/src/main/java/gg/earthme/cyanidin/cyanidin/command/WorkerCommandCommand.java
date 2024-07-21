package gg.earthme.cyanidin.cyanidin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import gg.earthme.cyanidin.cyanidin.Cyanidin;
import gg.earthme.cyanidin.cyanidin.network.backend.MasterServerMessageHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorkerCommandCommand {
    public static void register(){
        final CommandMeta meta = Cyanidin.PROXY_SERVER.getCommandManager()
                .metaBuilder("dworkerc")
                .plugin(Cyanidin.INSTANCE)
                .build();
        Cyanidin.PROXY_SERVER.getCommandManager().register(meta, create());
    }

    public static @NotNull BrigadierCommand create() {
        LiteralCommandNode<CommandSource> registed = BrigadierCommand.literalArgumentBuilder("dworkerc")
                .requires(source -> source.hasPermission("cyanidin.commands.dworkerc"))
                .then(
                        BrigadierCommand.requiredArgumentBuilder("workerName", StringArgumentType.word()).suggests((ctx, builder) -> {
                            for (MasterServerMessageHandler connection : Cyanidin.registedWorkers.values()){
                                builder.suggest(connection.getWorkerName());
                            }
                            return builder.buildFuture();
                        })
                                .then(
                                        BrigadierCommand.requiredArgumentBuilder("mcCommand", StringArgumentType.word())
                                                .executes(context -> {
                                                    final CommandSource source = context.getSource();
                                                    final String workerName = StringArgumentType.getString(context, "workerName");
                                                    final String command = StringArgumentType.getString(context, "mcCommand");

                                                    MasterServerMessageHandler targetWorkerConnection = null;
                                                    for (MasterServerMessageHandler connection : Cyanidin.registedWorkers.values()){
                                                        if (workerName.equals(connection.getWorkerName())){
                                                            targetWorkerConnection = connection;
                                                            break;
                                                        }
                                                    }

                                                    if (targetWorkerConnection == null){
                                                        source.sendMessage(Cyanidin.languageManager.i18n("cyanidin.worker_command.worker_not_found", List.of(), List.of()));
                                                        return -1;
                                                    }

                                                    targetWorkerConnection.dispatchCommandToWorker(command, feedback -> {
                                                        if (feedback == null) {
                                                            return;
                                                        }

                                                        source.sendMessage(Cyanidin.languageManager.i18n("cyanidin.worker_command.command_feedback", List.of("feedback"), List.of(feedback)));
                                                    });
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )


                ).build();
        return new BrigadierCommand(registed);
    }

}