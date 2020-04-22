package src.emu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

import src.chip.chip8;

public class Frame extends JFrame implements KeyListener {
    private static final long serialVersionUID = 1L;
    private Panel panel;
    private int[] keyBuffer;
    private int[] keyIdToKey;

    public Frame(chip8 c) {
        setPreferredSize(new Dimension(640, 320));
        pack();
        setPreferredSize(
                new Dimension(640 + getInsets().left + getInsets().right, 320 + getInsets().top + getInsets().bottom));
        panel = new Panel(c);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Chip 8 Emulator");
        pack();
        setVisible(true);
        addKeyListener(this);

        keyIdToKey = new int[256];
        keyBuffer = new int[16];
        fillKeyIds();
    }

    private void fillKeyIds() {
        for (int i = 0; i < keyIdToKey.length; i++) {
            keyIdToKey[i] = -1;
        }
        /*
         * Input is done with a hex keyboard that has 16 keys ranging 0 to F. The '8',
         * '4', '6', and '2' keys are typically used for directional input. Three
         * opcodes are used to detect input. One skips an instruction if a specific key
         * is pressed, while another does the same if a specific key is not pressed. The
         * third waits for a key press, and then stores it in one of the data registers.
         */
        keyIdToKey['1'] = 1;
        keyIdToKey['2'] = 2;
        keyIdToKey['3'] = 3;
        keyIdToKey['Q'] = 4;
        keyIdToKey['W'] = 5;
        keyIdToKey['E'] = 6;
        keyIdToKey['A'] = 7;
        keyIdToKey['S'] = 8;
        keyIdToKey['D'] = 9;
        keyIdToKey['Z'] = 0xA;
        keyIdToKey['X'] = 0;
        keyIdToKey['C'] = 0xB;
        keyIdToKey['4'] = 0xC;
        keyIdToKey['R'] = 0xD;
        keyIdToKey['F'] = 0xE;
        keyIdToKey['V'] = 0xF;
    }
    //P1 goes up by 1 and down by Q
    //P2 goes up by 4 and down by R
    @Override
    public void keyPressed(KeyEvent e) {
        if (keyIdToKey[e.getKeyCode()] != -1) {
            keyBuffer[keyIdToKey[e.getKeyCode()]] = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (keyIdToKey[e.getKeyCode()] != -1) {
            keyBuffer[keyIdToKey[e.getKeyCode()]] = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public int[] getKeyBuffer() {
        return keyBuffer;
    }
}