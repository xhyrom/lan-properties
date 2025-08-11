package dev.xhyrom.lanprops.common.mixin;

import com.google.common.base.MoreObjects;
import com.mojang.datafixers.DataFixer;
import dev.xhyrom.lanprops.common.accessors.CustomDedicatedServerProperties;
import dev.xhyrom.lanprops.common.accessors.CustomIntegratedServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.Proxy;
import java.util.Optional;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer implements CustomIntegratedServer {
    public IntegratedServerMixin(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super(thread, levelStorageAccess, packRepository, worldStem, proxy, dataFixer, services, chunkProgressListenerFactory);
    }

    @Shadow public abstract boolean isPublished();

    @Shadow @Nullable private GameType publishedGameType;
    @Unique
    public DedicatedServerSettings lan_properties$settings;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(Thread thread, Minecraft minecraft, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
        this.lan_properties$settings = new DedicatedServerSettings(levelStorageAccess.getLevelDirectory().path().resolve("server.properties"));
        this.lan_properties$settings.forceSave();
    }

    @Inject(method = "initServer", at = @At("RETURN"))
    public void onInitServer(CallbackInfoReturnable<Boolean> cir) {
        DedicatedServerProperties dedicatedServerProperties = this.lan_properties$settings.getProperties();
        this.setUsesAuthentication(dedicatedServerProperties.onlineMode);
        this.setPreventProxyConnections(dedicatedServerProperties.preventProxyConnections);
        this.setPvpAllowed(dedicatedServerProperties.pvp);
        this.setFlightAllowed(dedicatedServerProperties.allowFlight);
        this.setMotd(dedicatedServerProperties.motd);
        super.setPlayerIdleTimeout(dedicatedServerProperties.playerIdleTimeout.get());
        this.setEnforceWhitelist(dedicatedServerProperties.enforceWhitelist);
    }

    @Inject(method = "publishServer", at = @At("HEAD"))
    public void onPublishServer(@Nullable GameType gameType, boolean bl, int i, CallbackInfoReturnable<Integer> cir) {
        this.lan_properties$customProperties().lan_properties$serverPort(i);
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    public void onStopServer(CallbackInfo ci) {
        this.lan_properties$settings.forceSave();
    }

    public boolean isSpawningMonsters() {
        return this.lan_properties$settings.getProperties().spawnMonsters && super.isSpawningMonsters();
    }

    @Unique
    public @NotNull DedicatedServerProperties lan_properties$properties() {
        return this.lan_properties$settings.getProperties();
    }

    @Unique
    public @NotNull CustomDedicatedServerProperties lan_properties$customProperties() {
        return (CustomDedicatedServerProperties) this.lan_properties$settings.getProperties();
    }

    public void forceDifficulty() {
        this.setDifficulty(this.lan_properties$properties().difficulty, true);
    }

    public boolean isLevelEnabled(Level level) {
        return level.dimension() != Level.NETHER || this.lan_properties$properties().allowNether;
    }

    public void setPlayerIdleTimeout(int i) {
        super.setPlayerIdleTimeout(i);
        this.lan_properties$settings.update((dedicatedServerProperties) -> dedicatedServerProperties.playerIdleTimeout.update(this.registryAccess(), i));
    }

    public int getRateLimitPacketsPerSecond() {
        return this.lan_properties$properties().rateLimitPacketsPerSecond;
    }

    public boolean isEpollEnabled() {
        return this.lan_properties$properties().useNativeTransport;
    }

    public boolean isCommandBlockEnabled() {
        return this.lan_properties$properties().enableCommandBlock;
    }

    public int getSpawnProtectionRadius() {
        return this.lan_properties$properties().spawnProtection;
    }

    public boolean repliesToStatus() {
        return this.lan_properties$properties().enableStatus;
    }

    public boolean hidesOnlinePlayers() {
        return this.lan_properties$properties().hideOnlinePlayers;
    }

    public int getOperatorUserPermissionLevel() {
        return this.lan_properties$properties().opPermissionLevel;
    }

    public int getFunctionCompilationLevel() {
        return this.lan_properties$properties().functionPermissionLevel;
    }

    public boolean shouldRconBroadcast() {
        return this.lan_properties$properties().broadcastRconToOps;
    }

    public boolean shouldInformAdmins() {
        return this.lan_properties$properties().broadcastConsoleToOps;
    }

    public int getAbsoluteMaxWorldSize() {
        return this.lan_properties$properties().maxWorldSize;
    }

    public int getCompressionThreshold() {
        return this.lan_properties$properties().networkCompressionThreshold;
    }

    public boolean logIPs() {
        return this.lan_properties$properties().logIPs;
    }

    public boolean enforceSecureProfile() {
        DedicatedServerProperties dedicatedServerProperties = this.lan_properties$properties();
        return dedicatedServerProperties.enforceSecureProfile && dedicatedServerProperties.onlineMode && this.services.canValidateProfileKeys();
    }

    public int getMaxChainedNeighborUpdates() {
        return this.lan_properties$properties().maxChainedNeighborUpdates;
    }

    public int getScaledTrackingDistance(int i) {
        return this.lan_properties$properties().entityBroadcastRangePercentage * i / 100;
    }

    public boolean forceSynchronousWrites() {
        return this.lan_properties$settings.getProperties().syncChunkWrites;
    }

    @Nullable
    public GameType getForcedGameType() {
        return this.lan_properties$settings.getProperties().forceGameMode ? this.worldData.getGameType() : (isPublished() && !this.isHardcore() ? (GameType) MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null);
    }

    public @NotNull Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
        return this.lan_properties$settings.getProperties().serverResourcePackInfo;
    }

    public boolean acceptsTransfers() {
        return this.lan_properties$settings.getProperties().acceptsTransfers;
    }

    public int pauseWhileEmptySeconds() {
        return this.lan_properties$settings.getProperties().pauseWhenEmptySeconds;
    }
}
