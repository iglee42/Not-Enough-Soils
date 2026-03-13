package fr.iglee42.modpackutilities.modules.lore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;

public class LoreSoundHandler {

    private static LoreSoundHandler INSTANCE;
    private SimpleSoundInstance playingSound;
    private LoreSoundHandler() {}

    public static LoreSoundHandler getInstance(){
        if (INSTANCE == null) INSTANCE = new LoreSoundHandler();
        return INSTANCE;
    }

    public void playSound(@Nullable SoundEvent sound){
        if (playingSound != null){
            Minecraft.getInstance().getSoundManager().stop(playingSound);
        }

        playingSound = createInstance(sound);
        if (playingSound != null){
            Minecraft.getInstance().getSoundManager().play(playingSound);
        }
    }

    public void stopSound(){
        playSound(null);
    }

    public boolean isSoundPlaying(){
        return playingSound != null && Minecraft.getInstance().getSoundManager().isActive(playingSound);
    }

    public boolean isSoundPlaying(SoundEvent event){
        return isSoundPlaying() && playingSound.getLocation().equals(event.getLocation());
    }

    private @Nullable SimpleSoundInstance createInstance(SoundEvent sound) {
        if (sound == null) return null;
        return new SimpleSoundInstance(
                sound.getLocation(), SoundSource.VOICE, 1f,1f, SoundInstance.createUnseededRandom(),false,0, SoundInstance.Attenuation.NONE,0.0,0.0,0.0,true
        );
    }
}
