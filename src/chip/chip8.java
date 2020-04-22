/* * Initializing the Chip-8 system 
    ? simplistic and minimal
    ! full documentation for chip-8 here http://devernay.free.fr/hacks/chip8/C8TECH10.HTM
*/
package src.chip;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

public class chip8 {
    // * chip 8 has 4kb mem ie 8 bits of memory

    private char[] mem; // this is the soft representation of chip 8 memory
    private char[] V; // registers
    private char I; // 16 bit address register
    private char pc; // program counter

    private char[] stack; // the stack // * 12 levels of nesting
    private int stackPointer;

    // * the timers, both are clocked at 60Hz
    private int delay;
    private int sound;

    private byte[] inp; // input with a hex keyboard containing 16 keys 0 to F

    private byte[] display; // display is 64x32 pixels monochrome
    private boolean redrawf; // flag to check whether a screen update is required or not
    // * method to initialize the memory

    public void init() {
        mem = new char[4096]; // 4kB
        V = new char[16];
        I = 0x0;
        pc = 0x200;

        stack = new char[12];
        stackPointer = 0;

        delay = 0;
        sound = 0;

        inp = new byte[16];

        display = new byte[64 * 32]; // 64*32 is too small so scaled to 10 in the actual function

        redrawf = false;
        loadFontset(); // Loading the dataset for the Pong chan
    }

    // * run method for a execution of an instruction

    public void run() {

        char opcode = (char) ((mem[pc] << 8) | mem[pc + 1]);
        System.out.println(Integer.toHexString(opcode));

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x00FF) {
                    case 0x00E0: // clear screen
                        System.err.println("Unsupported opcode!");
                        System.exit(0);
                        break;

                    case 0x00EE: // Returns from a subroutine
                        stackPointer--;
                        pc = (char) (stack[stackPointer] + 2);
                        break;

                    default: // Call RCA 1802 at NNN
                        System.exit(0);
                        break;
                }
                break;
            case 0x1000: // Jumps to address NNN
                pc = (char) (opcode & 0x0FFF);
                break;
            case 0x2000:// Calls subroutine at NNN
                stack[stackPointer] = pc;
                stackPointer++;
                pc = (char) (opcode & 0x0FFF);
                break;
            case 0x3000: // Skips next if VX = NN
            {
                // 3XNN
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                if (V[x] == nn) {
                    pc += 4;
                } else {
                    pc += 2;
                }
                break;
            }
            case 0x4000: {// Skips next if VX != NN
                // 4XNN: Cond
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                if (V[x] != nn)
                    pc += 4;
                else
                    pc += 2;
                break;
            }
            default:
                System.err.println("Unsupported opcode!");
                System.exit(0);
                break;
            case 0x5000: // Skips next if VX = vY
                break;
            case 0x6000: // Sets VX = NN
            {
                int x = (opcode & 0x0F00) >> 8;
                V[x] = (char) (opcode & 0x00FF);
                pc += 2;
                break;
            }
            case 0x7000:
            // 7XNN
            // Adds NN to VX
            {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                V[x] = (char) ((V[x] + nn & 0xFF));
                pc += 2;
                break;
            }
            case 0x8000: // last nibble has more data

                switch (opcode & 0x000F) {
                    case 0x0000: {// 8XY0: Sets VX to VY
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        V[x] = V[y];
                        pc += 2;
                        break;
                    }
                    case 0x0002: {// 8XY2: Sets VX to VX AND VY //* (Bitwise)
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        V[x] = (char) (V[x] & V[y]);
                        pc += 2;
                        break;
                    }
                    case 0x0004: {// 8XY4: Math :: Adds VY to VX :: also sets VF in case of carry
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        if (V[y] > 255 - V[x])
                            V[0xF] = 1;
                        else
                            V[0xF] = 0;
                        V[x] = (char) ((V[x] + V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }
                    case 0x0005:{
                        //8XY5 : Math :: VX -= VY with borrow
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        if(V[x] > V[y])
                            V[0xF] = 1;
                        else
                            V[0xF] = 0;
                        V[x] = (char) ((V[x] - V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }
                    default:
                        System.err.println("Opcode is not supported!");
                        System.exit(0);
                        break;
                }
                break;
            case 0xA000: // Sets I to NNN
                I = (char) (opcode & 0x0FFF);
                pc += 2;
                break;
            case 0xE000:
                switch (opcode & 0x00FF) {
                    case 0x009E: {// EX9E : KeyOp :: skips next if key in VX is pressed
                        int x = (opcode & 0x0F00) >> 8;
                        int key = V[x];
                        if (inp[key] == 1) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                    }
                    case 0x00A1: { // EXA1 : KeyOp :: skips next if the key in VX isn't pressed
                        int x = (opcode & 0x0F00) >> 8;
                        int key = V[x];
                        if (inp[key] == 0) {
                            pc += 4;
                        } else {
                            pc += 2;
                        }
                        break;
                    }
                    default:
                        System.err.println("Unsupported opcode!");
                        System.exit(0);
                        return;
                }
                break;
            case 0xC000: {
                // CXNN : RAND :: VX = rand()&NN

                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                int rNum = new Random().nextInt(256) & nn;
                V[x] = (char) rNum;
                pc += 2;
                break;
            }
            case 0xD000: // draw: Draws a sprite at coordinates VX and VY
            // DXYN : X cord Y cord N is height
            {
                int x = V[(opcode & 0x0F00) >> 8];
                int y = V[(opcode & 0x00F0) >> 4];
                int n = (opcode & 0x000F);

                for (int _y = 0; _y < n; _y++) {
                    int line = mem[I + _y];
                    for (int _x = 0; _x < 8; _x++) {
                        int pixel = line & (0x80 >> _x);
                        if (pixel != 0) {
                            int totalX = x + _x;
                            int totalY = y + _y;

                            totalX = totalX % 64;
                            totalY = totalY % 32;
                            int index = totalY * 64 + totalX;

                            if (display[index] == 1)
                                V[0xF] = 1;

                            display[index] ^= 1;
                        }
                    }
                }

                pc += 2;
                redrawf = true;
                break;
            }
            case 0xF000:
                switch (opcode & 0x00FF) {

                    case 0x0007: {
                        // FX07: sets VX to delay timer
                        int x = (opcode & 0x0F00) >> 8;
                        V[x] = (char) delay;
                        pc += 2;
                        break;
                    }
                    case 0x0015: {
                        // FX15 : sets the delay timer to VX
                        int x = (opcode & 0x0F00) >> 8;
                        delay = V[x];
                        pc += 2;
                        break;
                    }
                    case 0x0018: {
                        //FX18 : Sound :: sets sound timer to VX
                        int x = (opcode & 0x0F00) >> 8;
                        sound = V[x];
                        pc += 2;
                        break;
                    }
                    case 0x0029: {
                        // FX29 : set I to the location of the sprite for the character in VX
                        int x = (opcode & 0x0F00) >> 8;
                        int character = V[x];
                        I = (char) (0x050 + (character * 5));
                        pc += 2;
                        break;
                    }
                    case 0x0033: {
                        // FX33 : BCD :: stores the BCD rep of VX msb at I middle bit at I+1 and LSB at
                        // I+2
                        int y = (opcode & 0x0F00) >> 8;
                        int x = V[y];

                        int hun = (x - (x % 100)) / 100;
                        x -= hun * 100;
                        int ten = (x - (x % 10)) / 10;
                        x -= ten * 10;
                        mem[I] = (char) hun;
                        mem[I + 1] = (char) ten;
                        mem[I + 2] = (char) x;
                        pc += 2;
                        break;
                    }
                    case 0x0065: {
                        // FX65 : fills V0 through VX with values starting at I and incementing by 1
                        int x = (opcode & 0x0F00) >> 8;
                        for (int i = 0; i <= x; i++) {
                            V[i] = mem[I + i];
                        }
                        I = (char)(I+x+1);
                        pc += 2;
                        break;
                    }
                    default:
                        System.err.println("Opcode is not supported!");
                        System.exit(0);
                }
            // default:
            // System.err.println("Unsupported opcode!");
            // System.exit(0);
            if(sound > 0)
                sound--;
            if(delay >0)
                delay--;
        }
    }

    // getter
    public byte[] getDisplay() {
        return display;
    }

    public boolean redraw() {
        return redrawf;
    }

    public void removeDrawFlag() {
        redrawf = false;
    }

    public void loadFontset() {
        for (int i = 0; i < data.fontset.length; i++) {
            mem[0x50 + i] = (char) (data.fontset[i] & 0xFF);
        }
    }

    public void setKeyBuffer(int[] keyBuffer) {
        for (int i = 0; i < inp.length; i++) {
            inp[i] = (byte) keyBuffer[i];
        }
    }

    public void loadProgram(String file) {
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(new File(file)));

            for (int offset = 0; input.available() > 0; offset++) {
                mem[0x200 + offset] = (char) (input.readByte() & 0xFF);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}