package games.mrlaki5.soundtest.SoundClient;

import java.util.ArrayList;

public class BitFrequencyConverter {

    //Number of bits transferred in one tone
    private int numberOfBitsInOneTone;
    //Start frequency of transfer
    private int startFrequency;
    //End frequency of transfer
    private int endFrequency;
    //Padding between to data bind frequencies
    private int padding;
    //Handshake start frequency
    private int handshakeStartFreq;
    //Handshake end frequency
    private int handshakeEndFreq;
    //Read bytes
    private ArrayList<Byte> readBytes;
    //Current reading byte
    private byte currByte;
    //Bit position on current reading byte
    private int currShift;

    public BitFrequencyConverter(int startFrequency, int endFrequency, int numberOfBitsInOneTone){
        this.numberOfBitsInOneTone=numberOfBitsInOneTone;
        //Calculate padding, depending on number of bits per tone + 4 (startHandshake, endHandshake,
        //frequency for 1 and frequency for 0 (used if transferred data is not mod number of bits per tone))
        this.padding=(endFrequency-startFrequency)/(4+(int)Math.pow(2, numberOfBitsInOneTone));
        //HandshakeStart nad HandshakeEnd are highest two frequencies
        this.handshakeEndFreq=endFrequency;
        this.handshakeStartFreq=endFrequency-this.padding;
        //Start and end of frequencies for data transfer
        this.startFrequency=startFrequency;
        this.endFrequency=endFrequency-2*this.padding;
        readBytes=new ArrayList<Byte>();
        currByte=0x00;
        currShift=0;
    }

    //Called to calculate bit from given frequency
    public void calculateBits(double frequency){
        byte resultBytes=0x00;
        boolean freqFound=false;
        boolean lastPart=false;
        int counter=0;
        //Go through all frequencies and find where given one belongs
        for(int i=(startFrequency); i<=(endFrequency); i+=padding, counter++){
            if(frequency>=(i-(padding/2)) && (frequency<=(i+(padding/2)))){
                //If its one of first two frequencies, then its transfer of 1 or 0 b
                if(counter==0 || counter==1){
                    lastPart=true;
                }
                else{
                    freqFound=true;
                }
                break;
            }
            else{
                if(counter!=0 && counter!=1) {
                    resultBytes += 0x01;
                }
            }
        }
        if(freqFound){
            //Add bits of found frequency to read bytes list through current byte
            int tempCounter=numberOfBitsInOneTone;
            while(tempCounter>0){
                byte mask=0x01;
                mask<<=(tempCounter-1);
                currByte<<=1;
                if((mask&resultBytes)!=0x00){
                   currByte+=0x01;
                }
                currShift++;
                if(currShift==8){
                    readBytes.add(currByte);
                    currShift=0;
                    currByte=0x00;
                }
                tempCounter--;
            }
        }
        else {
            if (lastPart){
                //Its one of first tow frequencies for transfer 0 or 1 bit, add it to read bytes
                currByte<<=1;
                if(counter==1){
                    currByte+=0x01;
                }
                currShift++;
                if(currShift==8){
                    readBytes.add(currByte);
                    currByte=0x00;
                    currShift=0;
                }
            }
        }
    }

    //Called to get and reset all read bytes
    public byte[] getAndResetReadBytes(){
        byte[] retArr;
        if(currShift!=0){
            retArr= new byte[readBytes.size()+1];
            retArr[retArr.length-1]=currByte;
        }
        else{
            retArr= new byte[readBytes.size()];
        }
        int i=0;
        for (byte tempB : readBytes) {
            retArr[i]=tempB;
            i++;
        }
        readBytes.clear();
        currByte=0x00;
        currShift=0;
        return retArr;
    }

    //Called to calculate frequency for given byte
    private int specificFrequency(byte sample){
        //Jump over first two frequencies for bit 0,1 bit transfer
        int freq=startFrequency+padding*2;
        //Go through all bytes and find matching with given byte, return its frequency
        int numberOfFreq=(int)Math.pow(2, numberOfBitsInOneTone);
        byte tempByte=0x00;
        for(int i=0; i<numberOfFreq; i++){
            if(tempByte==sample){
                break;
            }
            tempByte+=0x01;
            freq+=padding;
        }
        return freq;
    }

    //Called to get bit on specific position from byte
    private int getBit(byte check ,int position){
        return (check >> position) & 1;
    }

    //Called to calculate list of frequencies for given byte array
    public ArrayList<Integer> calculateFrequency(byte[] byteArray){
        ArrayList<Integer> resultList=new ArrayList<Integer>();
        //Check if length of byte array can be cut down in chunks of number of bits to send in tone
        boolean isDataModulo=(byteArray.length*8 % numberOfBitsInOneTone)==0;
        byte currByte=0x00;
        int currShift=0;
        //go through byte array and calculate frequencies
        for(int i=0; i<byteArray.length; i++){
            byte tempByte=byteArray[i];
            for(int j=7; j>=0; j--){
                //if length of byte array can't be cut down in chunks of number of bits to send in one tone
                //and number of bits left to be send is lower then number of bits to send in one tone
                //use first two frequencies for 0 nd 1b transfer
                if(((currShift+j+1+(byteArray.length-(i+1))*8)<numberOfBitsInOneTone) && (!isDataModulo)){
                    int temp=getBit(tempByte, j);
                    if(temp==1){
                        resultList.add(startFrequency+padding);
                    }
                    else{
                        resultList.add(startFrequency);
                    }
                    continue;
                }
                //Shift bit and check if number of shifted bits is equal to number of bits send in one tone
                //if it is calculate frequency for current bits and reset counters
                int temp=getBit(tempByte, j);
                currByte<<=1;
                if(temp==1){
                    currByte+=0x01;
                }
                currShift++;
                if(currShift==numberOfBitsInOneTone){
                    int currFreq=specificFrequency(currByte);
                    resultList.add(currFreq);
                    currByte=0x00;
                    currShift=0;
                }
            }
        }
        return resultList;
    }

    public int getPadding() {
        return padding;
    }

    public int getHandshakeStartFreq() {
        return handshakeStartFreq;
    }

    public int getHandshakeEndFreq() {
        return handshakeEndFreq;
    }
}
