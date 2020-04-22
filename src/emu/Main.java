package src.emu;

import src.chip.chip8;

public class Main extends Thread{
    private chip8 c8;
    private Frame frame;

    public Main(){
        c8 = new chip8();
        c8.init();
        c8.loadProgram("D:/folder/testing/src/emu/pong2.c8");
        frame = new Frame(c8);
    }

    public void run() {
        while(true) {
            c8.setKeyBuffer(frame.getKeyBuffer());
            c8.run();
            if(c8.redraw()) {
                frame.repaint();
                c8.removeDrawFlag();
            }
            try{
            Thread.sleep(8);}
            catch(InterruptedException e){
                //does not require handling 
            }
        }
    }
    public static void main(String[] args) {
        Main main = new Main();
        main.start();
    }
}