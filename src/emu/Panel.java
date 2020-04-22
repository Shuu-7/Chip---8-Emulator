package src.emu;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import src.chip.chip8;

public class Panel extends JPanel {
 
    /**
     *
     */
    private static final long serialVersionUID = -7414492175313664696L;
    private chip8 chip;
    public Panel (chip8 chip){
        this.chip = chip;
    }
    public void paint(Graphics g){
        byte[] display = chip.getDisplay();
        for(int i=0;i<display.length; i++){
            if(display[i] == 0){
                g.setColor(Color.BLACK);
            }
            else{
                g.setColor(Color.WHITE);
            }
            int y = (int)Math.floor(i / 64);
            int x = (i % 64);

            g.fillRect(x*10, y*10, 10, 10);
        }
    }
}