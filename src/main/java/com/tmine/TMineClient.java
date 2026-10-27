private static boolean isMyMace(MinecraftClient mc, ItemStack s) {
        var reg = mc.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<Enchantment> breach = reg.getOrThrow(Enchantments.BREACH);
        RegistryEntry<Enchantment> unbreaking = reg.getOrThrow(Enchantments.UNBREAKING);
        RegistryEntry<Enchantment> mending = reg.getOrThrow(Enchantments.MENDING);

        if (EnchantmentHelper.getLevel(breach, s) < 4) return false;
        if (EnchantmentHelper.getLevel(unbreaking, s) < 3) return false;
        if (EnchantmentHelper.getLevel(mending, s) < 1) return false;
        return true;
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
