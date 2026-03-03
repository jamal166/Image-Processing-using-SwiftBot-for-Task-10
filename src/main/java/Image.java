import swiftbot.*;

public class Inc08_CuriousOnlyBuffer {

    private static volatile boolean xPressed = false;
    private final SwiftBotAPI api = SwiftBotAPI.INSTANCE;

    private static final int BUFFER_CM = 30;
    private static final int TOLERANCE_CM = 2;

    private static final int DETECT_WITHIN_CM = 200;

    private static final int WANDER_L = 18;
    private static final int WANDER_R = 20;

    public static void main(String[] args) {
        new Inc08_CuriousOnlyBuffer().run();
    }
    private void run() {
        setupXButtonStop();
        setBlue();
        System.out.println("[INC08] Curious only. When object <=2m: keep buffer=30cm. X to stop.");

        while (!xPressed) {
            api.move(WANDER_L, WANDER_R, 250);

            double d = readDistanceAvg(2);
            if (d > 0 && d <= DETECT_WITHIN_CM) {
                curiousMaintainBuffer();
                setBlue();
            }
        }
        api.stopMove();
        api.disableUnderlights();
        api.disableAllButtons();
        System.out.println("[INC08] Stopped.");
    }
    private void curiousMaintainBuffer() {
        setGreen();
        api.stopMove();

        while (!xPressed) {
            double d = readDistanceAvg(2);
            if (d <= 0) break;

            if (d > BUFFER_CM + TOLERANCE_CM) {
                api.startMove(25, 25);      // forward
                sleep(120);
            } else if (d < BUFFER_CM - TOLERANCE_CM) {
                api.startMove(-25, -25);    // backward
                sleep(120);
            } else {
                api.stopMove();
                blinkGreen();
                break;
            }
        }
        api.stopMove();
        api.disableUnderlights();
    }
    private void blinkGreen() {
        for (int i=0;i<3 && !xPressed;i++){
            setGreen(); sleep(200);
            api.disableUnderlights(); sleep(200);
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

    private void setupXButtonStop() {
        api.disableAllButtons();
        api.enableButton(Button.X, () -> xPressed = true);
    }