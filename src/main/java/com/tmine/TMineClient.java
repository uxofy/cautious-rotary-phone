package com.tmine;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public class TMineClient implements ClientModInitializer {

    private static final float MIN_COOLDOWN = 0.92f;
    private static final int MIN_DELAY_TICKS = 0;
    private static final int MAX_DELAY_TICKS = 2;
    private static final boolean ONLY_PLAYERS = false;

    private static KeyBinding toggleKey;
    private static boolean enabled = false;

    private static int delayLeft = -1;
    private static Entity lastTarget = null;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("tmine", "main"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tmine.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, cat));

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            msg(mc, "tmine: " + (enabled ? "ON" : "OFF"));
        }

        if (player == null) return;
        if (mc.world == null) return;
        if (mc.interactionManager == null) return;
        if (!enabled) return;
        if (mc.currentScreen != null) return;
        if (player.isUsingItem()) return;

        HitResult hit = mc.crosshairTarget;
        if (!(hit instanceof EntityHitResult ehr)) {
            lastTarget = null;
            delayLeft = -1;
            return;
        }

        Entity target = ehr.getEntity();
        if (!(target instanceof LivingEntity living)) return;
        if (!living.isAlive()) return;
        if (living == player) return;
        if (ONLY_PLAYERS) {
            if (!(target instanceof PlayerEntity)) return;
        }

        if (target != lastTarget) {
            lastTarget = target;
            delayLeft = rnd();
        }
        if (delayLeft > 0) {
            delayLeft--;
            return;
        }

        ItemStack held = player.getMainHandStack();
        boolean sword = held.isIn(ItemTags.SWORDS);
        boolean axe = held.isIn(ItemTags.AXES);
        if (!sword) {
            if (!axe) return;
        }
        if (!canCrit(player)) return;
        if (player.getAttackCooldownProgress(0.5f) < MIN_COOLDOWN) return;

        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);
        delayLeft = rnd();
    }

    private static boolean canCrit(ClientPlayerEntity p) {
        if (p.fallDistance <= 0.0f) return false;
        if (p.getVelocity().y >= 0.0) return false;
        if (p.isOnGround()) return false;
        if (p.isClimbing()) return false;
        if (p.isTouchingWater()) return false;
        if (p.hasVehicle()) return false;
        if (p.isSprinting()) return false;
        if (p.hasStatusEffect(StatusEffects.BLINDNESS)) return false;
        return true;
    }

    private static int rnd() {
        return ThreadLocalRandom.current().nextInt(MIN_DELAY_TICKS, MAX_DELAY_TICKS + 1);
    }

    private static void msg(MinecraftClient mc, String s) {
        if (mc.player != null) mc.player.sendMessage(Text.literal(s), true);
    }
}
