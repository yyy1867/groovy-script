package ml.guxing.script.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Scanner;

public class AudioStart implements Runnable {

    private static boolean status = true;
    private static boolean isPlay = true;
    private static TargetDataLine targetDataLine;
    private static SourceDataLine sourceDataLine;
    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(102400);
    private static AudioFormat audio = new AudioFormat(8000f, 16
            , 1, true, false);

    static {
//        audio = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000f, 16, 1
//                , 16, 8000f, true);
    }

    public static void main(String[] args) throws Exception {
//        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audio);
        targetDataLine = AudioSystem.getTargetDataLine(audio);
        targetDataLine.open(audio);
        targetDataLine.start();
        if (isPlay) {
            sourceDataLine = AudioSystem.getSourceDataLine(audio);
            sourceDataLine.open(audio);
            sourceDataLine.start();
        }
        new Thread(new AudioStart()).start();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String in = scanner.nextLine();
            if ("stop".equals(in)) {
                status = false;
                break;
            }
        }
    }

    @Override
    public void run() {
        try {
            byte[] temp = new byte[10240];
            while (status) {
                int cnt = targetDataLine.read(temp, 0, temp.length);
                if (isPlay) {
                    System.out.println(cnt);
                    sourceDataLine.write(temp, 0, cnt);
                }
                byteArrayOutputStream.write(temp, 0, cnt);
                byteArrayOutputStream.flush();
            }
            byteArrayOutputStream.close();
            byte[] cache = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cache);
            AudioInputStream audioInputStream = new AudioInputStream(inputStream, audio
                    , cache.length / audio.getFrameSize());
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File("test.wav"));
            audioInputStream.close();
            inputStream.close();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
