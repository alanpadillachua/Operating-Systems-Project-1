/**
 * Created by Alan Padilla on 9/13/2016.
 * axp141330
 * Memory
 *
 */
import java.io.*;
import java.util.*;
public class Memory {
    private static int[] mainMemory = new int[2000]; // int array to hold the instructions and

    public static void main(String arg[]){
        //Strings that hold the string names
        String sample1 = "sample1.txt";
        String sample2 = "sample2.txt";
        String sample3 = "sample3.txt";
        String sample4 = "sample4.txt";
        String sample5 = "sample5.txt";
        String file = arg[0]; // the file specified is in the arg[0]

        Scanner command = new Scanner(System.in);
        // Reads from standard input scanner so reads the command sent by the processor
        String message; // string holds the command
        switch (file){ // switch statement for choosing which file to initialize
            case "sample1.txt":initialize(sample1);
                break;
            case "sample2.txt":initialize(sample2);
                break;
            case "sample3.txt":initialize(sample3);
                break;
            case "sample4.txt":initialize(sample4);
                break;
            case "sample5.txt":initialize(sample5);
        }
        /**
         * While loop reads the input from processor
         * reads strings which are then parsed
         */
        while(command.hasNext()){
            message = command.nextLine();
            parseCommand(message);
        }
    }
    //***********************************************************************
    // Initialize: Accepts a string that contains the file name.
    // It opens the file and reads the lines and correctly places the integers into
    // proper memory locations.
    //
    //***********************************************************************
    private static void  initialize(String file){
        String instruction; // string holds instruction line from file
        int counter = 0; // counter that controls where in the array it is being written too
        try{
            FileReader readFile = new FileReader(file); // file reader method to open file
            BufferedReader readBuffer = new BufferedReader(readFile); // buffer reader to read file
            while ((instruction = readBuffer.readLine()) != null){ // reads the file until it ends
                if (!(instruction.isEmpty())) { // checks that line is not empty
                    String[] parsedIns = instruction.split("\\s+",2); //parses the instruction to remove comments
                    try {
                        mainMemory[counter] = Integer.parseInt(parsedIns[0]); // converts string to int and stores it in array
                        counter++; // increment the address in Memory
                    }
                    catch (NumberFormatException e){ //
                        if(!(parsedIns[0].isEmpty())){
                            String[] dotCase = parsedIns[0].split(".",2); // this is dot case ex .1000
                            counter = Integer.parseInt(dotCase[1]); // counter is moved to the number after dot
                        }
                    }
                }
            }
            readBuffer.close(); // close file
        }catch (FileNotFoundException ex) { // catches errors in opening the file
            System.out.println("Unable to open file '" + file + "'");
        }
        catch(IOException ex) { // catches errors in reading the file
            System.out.println("Error reading file '" + file + "'");
        }

    }
    /** Parse Command is the method that ineracts with the processor message
     * and cause the fetch cycle to work
     * */
    public static void parseCommand(String data){
         String[] action = data.split("\\s+",2); // splits at the first space
         if(action[0].equals("R")){ //if the line starts with R then its a read command
             System.out.println(read(Integer.parseInt(action[1]))); // prints the data at address
         }
         else {
             // write needs two ints I must hold to inputs in here will figure out
             String[] newData = action[1].split("\\s+",2);// if not then its a write command
             write(Integer.parseInt(newData[0]),Integer.parseInt(newData[1])); // string is further parsed to get individual digits
         }
     }
    /**
     * Read takes an int that is the address to be read
     * returns a -1 if it is an out of bound address
     * */
   private static int read(int address){
        try {
            return mainMemory[address];
        }catch (ArrayIndexOutOfBoundsException e){
            return -1; //
        }
    }
    /**
     * Write does not return anything it takes two arguments
     * address and data to be written in that address
     * */
    private static void write(int add, int data){
        try {
            mainMemory[add] = data;
        }catch (ArrayIndexOutOfBoundsException e){
            //System.out.println("Address not valid.");
        }
    }
}
