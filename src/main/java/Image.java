import swiftbot.*;

import java.util.Random;

public class Inc10_DubiousRandomChoice {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int DETECT_WITHIN_CM = 200;
    private static final int SCAREDY_WITHIN_CM = 50;

    public static void main(String[] args) {
        new Inc10_DubiousRandomChoice().run();
    }
    private void run() {
        setupXButtonStop();
        setBlue();
        System.out.println("[INC10] Dubious: on encounter<=2m randomly do Curious or Scaredy (simple demo). X to stop.");

        Random rnd = new Random();
        boolean triggered = false;

        while (!xPressed) {
            api.move(18, 20, 250);

            double d = readDistanceAvg(2);
            boolean within2m = d > 0 && d <= DETECT_WITHIN_CM;

            if (within2m && !triggered) {
                triggered = true;

                boolean chooseCurious = rnd.nextBoolean();
                System.out.println("[INC10] Encounter. Dubious chose: " + (chooseCurious ? "Curious" : "Scaredy"));

                if (chooseCurious) {
                    // Minimal "curious" demo: green blink and stop
                    setGreen();
                    api.stopMove();
                    blink(0,255,0);
                    setBlue();
                } else {
                    // Minimal "scaredy" demo: if <=50, blink red + back up
                    if (d <= SCAREDY_WITHIN_CM) {
                        setRed();
                        blink(255,0,0);
                        api.move(-35, -35, 600);
                        api.stopMove();
                        setBlue();
                    } else {
                        System.out.println("[INC10] Scaredy chosen but not within 50cm; no flee.");
                    }
                }
            }

            if (!within2m) triggered = false;
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
    }
    private void blink(int r,int g,int b){
        for(int i=0;i<3 && !xPressed;i++){
            api.fillUnderlights(new int[]{r,g,b});
            sleep(150);
            api.disableUnderlights();
            sleep(150);
        }
    }

    private double readDistanceAvg(int samples) {
        double sum=0; int ok=0;
        for(int i=0;i<samples;i++){
            try{
                double d=api.useUltrasound();
                if(d>0 && d<5000){ sum+=d; ok++; }
            }catch(Exception ignored){}
            sleep(40);
        }
        return ok==0 ? -1 : sum/ok;
    }
    private void setBlue(){ api.fillUnderlights(new int[]{0,0,255}); }
    private void setGreen(){ api.fillUnderlights(new int[]{0,255,0}); }
    private void setRed(){ api.fillUnderlights(new int[]{255,0,0}); }

    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }

    private static void sleep(int ms){
        try{ Thread.sleep(ms);} catch(InterruptedException ignored){}
    }
}


