package games.mrlaki5.soundtest;

public class FFTCalculationThread {

    private Thread thread;
    private boolean work=true;
    RecordTask rootTask;

    public void start(){
        if(thread!=null) return;
        work=true;
        thread= new Thread(new Runnable() {
            @Override
            public void run() {
                while(work){
                    ChunkElement currElement=rootTask.getElementForCalculation();

                }
            }
        }, FFTCalculationThread.class.getName());
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }
}
