package com.mkforge.pacxon;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoundManager {
    private static final Logger LOGGER = Logger.getLogger(SoundManager.class.getName());
    private static Clip backgroundClip;
    private static Clip moveClip;
    private static Clip destroyClip;
    private static Clip powerupClip;
    private static Clip winClip;
    private static Clip loseClip;
    private static long lastMovePlayTime = 0;
    private static long lastDestroyPlayTime = 0;
    private static final long MOVE_COOLDOWN = 200000000;
    private static final long DESTROY_COOLDOWN = 300000000;

    public static void initialize() {
        try {
            backgroundClip = loadClip("sounds/background.wav");
            moveClip = loadClip("sounds/move.wav");
            destroyClip = loadClip("sounds/destroy.wav");
            powerupClip = loadClip("sounds/powerup.wav");
            winClip = loadClip("sounds/win.wav");
            loseClip = loadClip("sounds/lose.wav");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Nepodařilo se inicializovat zvukový manažer", e);
        }
    }

    private static Clip loadClip(String path) throws Exception {
        URL url = SoundManager.class.getResource(path);
        if (url == null) return null;
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        return clip;
    }

    public static void playBackground() {
        if (backgroundClip == null || !GameController.isMusicEnabled()) return;
        if (backgroundClip.isRunning()) backgroundClip.stop();
        backgroundClip.setFramePosition(0);
        backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void stopBackground() {
        if (backgroundClip != null && backgroundClip.isRunning())
            backgroundClip.stop();
    }

    public static void playMove() {
        if (moveClip == null || GameController.areSoundEffectsDisabled()) return;
        long now = System.nanoTime();
        if (now - lastMovePlayTime < MOVE_COOLDOWN) return;
        lastMovePlayTime = now;
        moveClip.setFramePosition(0);
        moveClip.start();
    }

    public static void playDestroy() {
        if (destroyClip == null || GameController.areSoundEffectsDisabled()) return;
        long now = System.nanoTime();
        if (now - lastDestroyPlayTime < DESTROY_COOLDOWN) return;
        lastDestroyPlayTime = now;
        destroyClip.setFramePosition(0);
        destroyClip.start();
    }

    public static void playPowerUp() {
        if (powerupClip == null || GameController.areSoundEffectsDisabled()) return;
        powerupClip.setFramePosition(0);
        powerupClip.start();
    }

    private static void playClipWithBackgroundRestart(Clip clip) {
        if (clip == null || GameController.areSoundEffectsDisabled()) return;
        if (backgroundClip != null && backgroundClip.isRunning()) backgroundClip.stop();

        clip.setFramePosition(0);
        clip.start();

        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (GameController.isMusicEnabled() && backgroundClip != null)
                        backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
                    clip.removeLineListener(this);
                }
            }
        });
    }

    public static void playWin() {
        playClipWithBackgroundRestart(winClip);
    }

    public static void playLose() {
        playClipWithBackgroundRestart(loseClip);
    }
}