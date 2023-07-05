package org.essentialss.implementation.world;

import net.kyori.adventure.audience.Audience;
import org.essentialss.api.utils.arrays.UnmodifiableCollection;
import org.essentialss.api.utils.arrays.impl.SingleUnmodifiableCollection;
import org.essentialss.api.utils.validation.ValidationRules;
import org.essentialss.api.utils.validation.Validator;
import org.essentialss.api.world.SPreGenData;
import org.essentialss.api.world.SWorldData;
import org.essentialss.api.world.points.SPoint;
import org.essentialss.api.world.points.jail.SJailSpawnPoint;
import org.essentialss.api.world.points.jail.SJailSpawnPointBuilder;
import org.essentialss.api.world.points.spawn.SSpawnPoint;
import org.essentialss.api.world.points.spawn.SSpawnPointBuilder;
import org.essentialss.api.world.points.spawn.SSpawnType;
import org.essentialss.api.world.points.warp.SWarp;
import org.essentialss.api.world.points.warp.SWarpBuilder;
import org.essentialss.implementation.events.point.register.RegisterPointPostEventImpl;
import org.essentialss.implementation.events.point.register.RegisterPointPreEventImpl;
import org.essentialss.implementation.world.points.spawn.SSpawnPointImpl;
import org.essentialss.implementation.world.points.spawn.SSpawnWrapperImpl;
import org.essentialss.implementation.world.points.warps.SWarpsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mose.property.impl.unknown.UnknownProperty;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SWorldDataImpl implements SWorldData {

    private final @Nullable ResourceKey key;
    private final @Nullable String id;
    private final Collection<SPoint> points = new LinkedHashSet<>();
    private final Collection<SSpawnType> mainSpawnType = new ArrayList<>();
    private final UnknownProperty<Boolean, Boolean> isLoaded;
    private @Nullable SPreGenDataImpl preGen;

    SWorldDataImpl(@NotNull SWorldDataBuilder builder) {
        this.key = builder.worldKey();
        this.id = builder.worldId();
        this.isLoaded = new UnknownProperty<>(t -> t, () -> this.spongeWorld().isPresent());

        if ((null == this.key) && (null == this.id)) {
            throw new IllegalArgumentException("World has not been specified");
        }
        if ((null != this.id) && Sponge.isServerAvailable()) {
            throw new IllegalStateException("Server can be used. Use ResourceKey");
        }
        this.points.addAll(new Validator<>(builder.points()).rule(ValidationRules.notNull()).validate());
        this.mainSpawnType.addAll(new Validator<>(builder.mainSpawnTypes()).validate());

    }

    @Override
    public void clearPoints() {
        this.points.clear();
    }

    @Override
    public boolean deregister(@NotNull SSpawnPoint builder, boolean runEvent, @Nullable Cause cause) {
        return this.deregisterPoint(builder, runEvent, cause);
    }

    @Override
    public boolean deregister(@NotNull SWarp builder, boolean runEvent, @Nullable Cause cause) {
        return this.deregisterPoint(builder, runEvent, cause);
    }

    @Override
    public boolean deregister(@NotNull SJailSpawnPoint builder, boolean runEvent, @Nullable Cause cause) {
        throw new RuntimeException("Jail not implemented yet");
    }

    @Override
    public Optional<SPreGenData> generatingChunkData() {
        return Optional.ofNullable(this.preGen);
    }

    @Override
    public UnknownProperty<Boolean, Boolean> isLoadedProperty() {
        return this.isLoaded;
    }

    @Override
    public boolean isWorld(@NotNull World<?, ?> world) {
        if ((world instanceof ServerWorld) && ((ServerWorld) world).key().equals(this.key)) {
            return true;
        }
        return world.context().toString().equalsIgnoreCase(this.id);
    }

    @Override
    public @NotNull Optional<CompletableFuture<World<?, ?>>> loadWorld() {
        if ((null != this.key) && Sponge.isServerAvailable()) {
            CompletableFuture<World<?, ?>> future = Sponge.server().worldManager().loadWorld(this.key).thenApply(w -> w);
            return Optional.of(future);
        }
        if ((null != this.id) && Sponge.isClientAvailable()) {
            CompletableFuture<World<?, ?>> future = new CompletableFuture<>();
            Optional<ClientWorld> opWorld = Sponge.client().world().filter(world -> world.context().toString().equalsIgnoreCase(this.id));
            if (!opWorld.isPresent()) {
                return Optional.empty();
            }
            future.complete(opWorld.get());
            return Optional.of(future);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull UnmodifiableCollection<SPoint> points() {
        LinkedList<SPoint> list = new LinkedList<>(this.points);
        list.add(new SSpawnWrapperImpl(this.mainSpawnType, this));

        return new SingleUnmodifiableCollection<>(list);
    }

    @Override
    public Optional<SSpawnPoint> register(@NotNull SSpawnPointBuilder builder, boolean runEvent, @Nullable Cause cause) {
        new Validator<>(builder.position()).notNull().validate();
        SSpawnPoint spawnPoint = new SSpawnPointImpl(builder, this);
        Optional<SSpawnPoint> register = this.register(spawnPoint, runEvent, cause);
        if (!register.isPresent()) {
            return Optional.empty();
        }
        if (builder.spawnTypes().contains(SSpawnType.MAIN_SPAWN)) {
            //noinspection DataFlowIssue
            Optional<World<?, ?>> opWorld = this.spongeWorld();
            if (opWorld.isPresent()) {
                Vector3d point = Objects.requireNonNull(builder.position()).get();
                opWorld.get().properties().setSpawnPosition(point.toInt());
                return Optional.of(spawnPoint);
            }
            if (Sponge.isServerAvailable()) {
                Sponge
                        .server()
                        .worldManager()
                        .loadProperties(this.key)
                        .thenAccept(opProperties -> opProperties.ifPresent(properties -> properties.setSpawnPosition(spawnPoint.position().toInt())));
            }
        }
        return Optional.of(spawnPoint);
    }

    @Override
    public Optional<SWarp> register(@NotNull SWarpBuilder builder, boolean runEvent, @Nullable Cause cause) {
        new Validator<>(builder.name()).notNull().validate();

        Optional<SWarp> opWarp = this.warps().parallelStream().filter(warp -> warp.position().equals(builder.point())).findAny();
        if (opWarp.isPresent()) {
            throw new IllegalArgumentException("Another warp (" + opWarp.get().identifier() + ") with that location has been found ");
        }
        //noinspection DataFlowIssue
        if (this.warp(builder.name()).isPresent()) {
            throw new IllegalArgumentException("Another warp has the name of " + builder.name());
        }
        return this.register(new SWarpsImpl(builder, this), runEvent, cause);
    }

    @Override
    public Optional<SJailSpawnPoint> register(@NotNull SJailSpawnPointBuilder builder, boolean runEvent, @Nullable Cause cause) {
        throw new RuntimeException("Jail is not implemented");
    }

    @Override
    public Optional<SPreGenData> setPreGeneratingData(@NotNull Vector3i center, double radius, @Nullable Audience audience) {
        return Optional.of(new SPreGenDataImpl(this, center, radius, audience));
    }

    @Override
    public @NotNull Optional<World<?, ?>> spongeWorld() {
        if ((null != this.key) && Sponge.isServerAvailable()) {
            return Sponge.server().worldManager().world(this.key).map(sWorld -> sWorld);
        }
        if ((null != this.id) && Sponge.isClientAvailable()) {
            return Sponge.client().world().filter(world -> world.context().toString().equalsIgnoreCase(this.id)).map(sWorld -> sWorld);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResourceKey> worldKey() {
        return Optional.empty();
    }

    private boolean deregisterPoint(@NotNull SPoint point, boolean runEvent, @Nullable Cause cause) {
        //TODO events
        return this.points.remove(point);
    }

    @Override
    public @NotNull String identifier() {
        if (null != this.id) {
            return this.id;
        }
        //noinspection DataFlowIssue
        return this.key.formatted();
    }

    private <T extends SPoint> Optional<T> register(@NotNull T point, boolean runEvents, @Nullable Cause cause) {
        if (runEvents) {
            if (null == cause) {
                throw new IllegalArgumentException("Cause cannot be null when running events");
            }
            RegisterPointPreEventImpl preEvent = new RegisterPointPreEventImpl(point, cause);
            Sponge.eventManager().post(preEvent);
            if (preEvent.isCancelled()) {
                return Optional.empty();
            }
        }
        boolean added = this.points.add(point);
        if (runEvents) {
            Event postEvent = new RegisterPointPostEventImpl(point, cause);
            Sponge.eventManager().post(postEvent);
        }
        return added ? Optional.of(point) : Optional.empty();

    }

    @Override
    public void reloadFromConfig() throws ConfigurateException {
        SWorldDataSerializer.load(this);
    }

    @Override
    public void saveToConfig() throws ConfigurateException, SerializationException {
        SWorldDataSerializer.save(this);
    }

    public Optional<SPreGenData> setGeneratingChunkData(@NotNull SPreGenDataImpl preGen) {
        if (null != this.preGen) {
            return Optional.empty();
        }
        this.preGen = preGen;
        preGen.start();
        return Optional.of(this.preGen);
    }
}
