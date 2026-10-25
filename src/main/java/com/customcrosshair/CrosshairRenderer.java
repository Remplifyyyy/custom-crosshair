package com.customcrosshair;

import com.customcrosshair.CrosshairConfig.Profile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static com.customcrosshair.CrosshairConfig.GRID;

public final class CrosshairRenderer {
    private CrosshairRenderer() { }

    /** Draws the in-game crosshair in the middle of the screen. */
    public static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        CrosshairConfig cfg = CrosshairConfig.get();
        if (cfg.hideInThirdPerson && !mc.options.getPerspective().isFirstPerson()) return;

        boolean targeting = cfg.targetEnabled && isLookingAtTarget(mc, cfg);
        Profile look = targeting ? cfg.target : cfg.normal;
        boolean[] shape = (targeting && cfg.targetUsesNormalShape) ? cfg.normal.pixels : look.pixels;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        draw(ctx, shape, look, cx, cy, look.scale);
    }

    public static boolean isLookingAtTarget(MinecraftClient mc, CrosshairConfig cfg) {
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY || !(hit instanceof EntityHitResult ehr)) return false;
        Entity e = ehr.getEntity();
        if (e == null) return false;
        return switch (cfg.targetMode) {
            case ALL -> true;
            case LIVING -> e instanceof LivingEntity;
            case HOSTILE -> e instanceof Monster;
            case PLAYERS -> e instanceof PlayerEntity;
        };
    }

    /**
     * Draws a crosshair drawing centred on (cx, cy).
     * @param pixelSize how many GUI pixels each drawn pixel takes up
     */
    public static void draw(DrawContext ctx, boolean[] shape, Profile look, int cx, int cy, int pixelSize) {
        int s = Math.max(1, pixelSize);
        int left = cx - (GRID * s) / 2;
        int top = cy - (GRID * s) / 2;

        if (look.outline) {
            for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) {
                if (!shape[y * GRID + x]) continue;
                int px = left + x * s, py = top + y * s;
                ctx.fill(px - 1, py - 1, px + s + 1, py + s + 1, look.outlineColor);
            }
        }
        for (int y = 0; y < GRID; y++) for (int x = 0; x < GRID; x++) {
            if (!shape[y * GRID + x]) continue;
            int px = left + x * s, py = top + y * s;
            ctx.fill(px, py, px + s, py + s, look.color);
        }
    }
}
