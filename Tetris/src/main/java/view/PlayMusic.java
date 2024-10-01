package view;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

class PlayMusic implements Runnable {
    private String filename;
    private boolean repeat;

    public PlayMusic(String filename, boolean repeat) {
        this.filename = filename;
        this.repeat = repeat;
    }

    @Override
    public void run() {
        try {
            ClassLoader classLoader = PlayMusic.class.getClassLoader();
            File soundFile = new File(classLoader.getResource(filename).getFile());
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundFile);

            Clip clip = AudioSystem.getClip();
            clip.open(inputStream);
            if (repeat) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();

            while (!clip.isRunning()) Thread.sleep(10);
            while (clip.isRunning()) Thread.sleep(10);

            clip.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void playMusicInThread(String filename, boolean repeat) {
        PlayMusic playMusic = new PlayMusic(filename, repeat);
        Thread musicThread = new Thread(playMusic);
        musicThread.start();
    }
}
