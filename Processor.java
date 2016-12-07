import java.io.*;
import java.lang.*;
import java.util.*;
/**
 * Created by Alan Padilla on 9/13/2016.
 * axp141330
 * Processor
 *
 */
public class Processor {
    private static int  PC,SP,IR,AC,X,Y, // Registers
                        timer, instCounter; // timer for clock and instCounter to check with timer
    private static boolean kernel = false; // kernel flag used to check if in kernel mode
    private static boolean interruptAllowed = true;
    private static boolean systemCall = false;
    public static void main(String[] args){
        timer = Integer.parseInt(args[1]);
        String fileName = args[0];
        try {
            int UserStack = SP = 1000; // user stack is at 1000
            int SystemStack = 2000; // system stack is at 2000
            IR = PC = instCounter = AC = X = Y = 0; // all initialized to zero

            Runtime rt = Runtime.getRuntime(); // get the runtime to create a process
            Process pipe = rt.exec("java Memory " + fileName); // runs Java Memory
            OutputStream outPipe = pipe.getOutputStream(); // connect the out put stream

            PrintWriter sendOn = new PrintWriter(outPipe); // create a PrinterWriter with output stream
            InputStream inPipe = pipe.getInputStream();  //input stream

            Scanner LI = new Scanner(inPipe); // Load Immediate LI holds the input stream

            sendOn.println("R " + PC); // send PC so memory sends PC location
            sendOn.flush();
            int add;
            while ((LI.hasNext())) { // reads the instructions
                IR = LI.nextInt(); // holds the instruction fetched from memory
                instCounter++; // increment instruction
                PC++; //prepare PC to read next line
                if((instCounter == timer) && interruptAllowed){ // checks if timer has reached instruction counter
                    interruptAllowed = false; //sets false so interrupts do not overlap
                    kernel = true; // system enters kernel mode
                    UserStack = SP; // userstack points to where SP points at that moment
                    SP = SystemStack; // SP now at system stack
                    sendOn.println("W " + --SP + " " + UserStack); // send User stack address to be written at system stack
                    sendOn.flush(); // send data

                    sendOn.println("W " + --SP + " " + --PC ); // send PC to system stack
                    sendOn.flush(); // send data
                    PC = 1000; // PC is at interrupt handler
                    IR = 0; // IR is set to 0
                }
                switch (IR){ // switch statement decides what the instruction requires
                    case 1:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush(); // send data
                        IR = LI.nextInt(); // read instruction
                        LoadValue(IR); // calls loadvalue
                        PC++; //set pc to get the next instruction
                        break;
                    case 2:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();// send data
                        IR = LI.nextInt();// read instruction
                        if(kernel){ //if kernel mode then access to memory is unrestricted
                            sendOn.println("R " + IR); // send PC to get next line
                            sendOn.flush(); // send data
                            IR = LI.nextInt(); // read instruction
                            LoadAddr(IR);
                        }
                        else if(!kernel && IR <= 999){ //if not in kernel mode IR must be within user space
                            sendOn.println("R " + IR); // send PC to get next line
                            sendOn.flush();
                            IR = LI.nextInt();
                            LoadAddr(IR);
                        }
                        else{
                            //if not print out an error message
                            System.out.println("Memory violation: accessing system address " + IR +
                                    " in user mode.");
                        }
                        PC++;
                        break;
                    case 3:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        sendOn.println("R " + IR); // send IR to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        sendOn.println("R " + IR); // send IR to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        LoadIndAddr(IR); // store it in AC
                        PC++;
                        break;
                    case 4:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt(); // IR holds where to load value from
                        add = IR + X; // IR + X to load from that address
                        sendOn.println("R " + add); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        LoadIdxXAddr(IR); // load that to an X
                        PC++;
                        break;
                    case 5:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt(); // IR holds where to load value from
                        add = IR + Y; // IR + Y added to load that address
                        sendOn.println("R " + add); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        LoadIdxYAddr(IR); // load that to Y
                        PC++;
                        break;
                    case 6:
                        add = SP + X;
                        sendOn.println("R " + add); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt(); // IR holds where to load value from
                        LoadSpX(IR);
                        break;
                    case 7:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        sendOn.println("W " + IR + " " + AC ); // Write AC at the Address in IR
                        sendOn.flush();
                        PC++;
                        break;
                    case 8: Get(); // call method GET()
                        break;
                    case 9:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt(); // IR holds where to load value from
                        PutPort(IR);
                        PC++;
                        break;
                    case 10: AddX();
                        break;
                    case 11: AddY();
                        break;
                    case 12: SubX();
                        break;
                    case 13: SubY();
                        break;
                    case 14: CopyToX();
                        break;
                    case 15: CopyFromX();
                        break;
                    case 16: CopyToY();
                        break;
                    case 17: CopyFromY();
                        break;
                    case 18:
                        CopyToSp();
                        break;
                    case 19:
                        CopyFromSp();
                        break;
                    case 20:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt(); // IR holds the address to jump too
                        JumpAddr(IR);
                        break;
                    case 21:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        if(!JumpIfEqualAddr(IR)){ // if jump did not work
                            PC++; //increment PC
                        }
                        break;
                    case 22:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        if(!JumpIfNotEqualAddr(IR)){ // if jump did not work
                            PC++;//increment PC
                        }
                        break;
                    case 23:
                        sendOn.println("R " + PC); // send PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        sendOn.println("W " + --SP + " " + ++PC); // Write PC to stack pointer
                        sendOn.flush();

                        CallAddr(IR);
                        break;
                    case 24:
                        sendOn.println("R " + SP++);//end PC to get next line
                        sendOn.flush();
                        IR = LI.nextInt();
                        Ret(IR);
                        break;
                    case 25: IncX();
                        break;
                    case 26: DecX();
                        break;
                    case 27:
                        sendOn.println("W " + --SP + " " + AC); // Write AC to stack
                        sendOn.flush();
                        break;
                    case 28:
                        sendOn.println("R " + SP++); // POP ac from stack
                        sendOn.flush();
                        IR = LI.nextInt();
                        Pop(IR);
                        break;
                    case 29:
                        if(interruptAllowed && !kernel){ // if not in kernel mode and interruptsAllowed is true
                            interruptAllowed = false; //set false so flag does not occur
                            kernel = true; // enter kernel mode
                            systemCall = true; // systemCall true used to not reset instruction counter
                            UserStack = SP;
                            SP = SystemStack; // SP now at system stack
                            sendOn.println("W " + --SP + " " + UserStack); // 1999 send PC to get next line
                            sendOn.flush();
                            sendOn.println("W " + --SP + " " + PC); // 1998 PC to get next line
                            sendOn.flush();
                            PC = 1500;
                        }
                        break;
                    case 30:
                        sendOn.println("R " + SP++); // send PC to get Y
                        sendOn.flush();
                        IR = LI.nextInt();
                        PC = IR;
                        sendOn.println("R " + SP++); // send PC to get Y
                        sendOn.flush();
                        IR = LI.nextInt();
                        SP = IR;
                        IRet(); //sets kernel back to false enters user mode and allows for interrupts
                        break;
                    case 50: End();
                        break;
                }
                sendOn.println("R " + PC); // send PC so memory sends PC location
                sendOn.flush();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    // 1 = load Value
    // Load the Value into the AC
    private static void LoadValue(int val){
        AC = val;
    }
    // 2 = Load addr
    // Load the value at the address into the AC
    private static void LoadAddr(int val){
        AC = val;
    }
    // 3 = LoadInd addr
    // Load the value from the address found in the given address into the AC
    // (for example, if LoadInd 500, and 500 contains 100, then load from 100).
    private static void LoadIndAddr(int val){
        AC = val;
    }
    // 4 = LoadIdxX addr
    // Load the value at (address+X) into the AC
    // (for example, if LoadIdxX 500, and X contains 10, then load from 510).
    private static void LoadIdxXAddr(int val){
        AC = val;
    }
    // 5 = LoadIdxY add
    // Load the value at (address+Y) into the AC
    private static void LoadIdxYAddr(int val){
        AC = val;
    }
    // 6 = LoadSpX
    // Load from (Sp+X) into the AC (if SP is 990, and X is 1, load from 991).
    private static void LoadSpX(int val){
        AC = val;
    }
    // 7 = Store addr
    // Store the value in the AC into the address
    private static void StoreAddr(){

    }
    // 8 = Get
    // Gets a random int from 1 to 100 into the AC
    private static void Get(){
        Random num = new Random();
        AC = num.nextInt(100) + 1;
        //System.out.println("Random Value :" + AC);
    }
    // 9 = Put port
    // If port=1, writes AC as an int to the screen
    // If port=2, writes AC as a char to the screen
    private static void PutPort(int port){
        if(port == 1){
          System.out.print(AC);
        }
        else if(port == 2){
          System.out.print((char)AC);
        }
    }
    // 10 = AddX
    // Add the value in X to the AC
    private static void AddX(){
        AC += X;
    }
    // 11 = AddY
    // Add the value in Y to the AC
    private static void AddY(){
        AC += Y;
    }
    // 12 = SubX
    // Subtract the value in X from the AC
    private static void SubX(){
        AC -= X;
    }
    // 13 = SubY
    // Subtract the value in Y from the AC
    private static void SubY(){
        AC -= Y;
    }
    // 14 = CopyToX
    // Copy the value in the AC to X
    private static void CopyToX(){
        X = AC;
    }
    // 15 = CopyFromX
    // Copy the value in X to the AC
    private static void CopyFromX(){
        AC = X;
    }
    // 16 = CopyToY
    // Copy the value in the AC to Y
    private static void CopyToY(){
        Y = AC;
    }
    // 17 = CopyFromY
    // Copy the value in Y to the AC
    private static void CopyFromY(){
        AC = Y;
    }
    // 18 = CopyToSp
    // Copy the value in AC to the SP
    private static void CopyToSp(){
        SP = AC;
    }
    // 19 = CopyFromSp
    // Copy the value in SP to the AC
    private static void CopyFromSp(){
        AC = SP;
    }
    // 20 = Jump addr
    // Jump to address
    private static void JumpAddr(int val){
        PC = val;
    }
    // 21 = JumpIfEqual addr
    // Jump to the address only if the value in the AC is zero
    private static boolean JumpIfEqualAddr(int val){
        if(AC == 0){
            PC = val;
            return true;
        }
        return false;
    }
    // 22 = JumpIfNotEqual addr
    // Jump to the address only if the value in the AC is not zero
    private static boolean JumpIfNotEqualAddr(int val){
        if(AC != 0) {
            PC = val;
            return true;
        }
        return false;
    }
    // 23 = Call addr
    // Push return address onto stack, jump to the address
    private static void CallAddr(int val){
        PC = val;
    }
    // 24 = Ret
    // Pop return address from the stack, jump to the address
    private static void Ret(int val){
        PC = val;
    }
    // 25 = IncX
    // Increment the value in X
    private static void IncX(){
        X = X + 1;
    }
    // 26 = DecX
    // Decrement the value in X
    private static void DecX(){
        X = X - 1;
    }

    // 28 = Pop
    // Pop from stack into AC
    private static void Pop(int val){
        AC = val;
    }

    // 30 = IRet
    // Return from system call
    private static void IRet(){
        interruptAllowed = true;
        kernel = false;
        if(!systemCall) { // if it was not a system call then reset counter
            instCounter = 0;
        }
        else {
            systemCall = false; // if not the just reset flag 
        }
    }
    // 50 = End
    // End execution
    private static void End(){
        System.exit(0);
    }
}
