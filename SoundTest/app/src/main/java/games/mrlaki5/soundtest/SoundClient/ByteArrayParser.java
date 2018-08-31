package games.mrlaki5.soundtest.SoundClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteArrayParser {

    //Array used when merging
    private byte[] outputByteArray=null;

    private byte[] concatenateTwoArrays(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    //Called to divide given byte array into byte arrays of size 256-errorDetNum
    public ArrayList<byte[]> divideInto256Chunks(byte[] inputArray, int errorDetBNum){
        ArrayList<byte[]> tempList=new ArrayList<byte[]>();
        int startPos=0;
        int endPos=256-errorDetBNum;
        int bytesLeft=inputArray.length;
        while((bytesLeft+errorDetBNum)>256){
            byte[] tempArr= Arrays.copyOfRange(inputArray, startPos, endPos);
            tempList.add(tempArr);
            startPos=endPos;
            endPos=startPos+256-errorDetBNum;
            bytesLeft-=(256-errorDetBNum);
        }
        byte[] tempArr=Arrays.copyOfRange(inputArray, startPos, inputArray.length);
        tempList.add(tempArr);
        return tempList;
    }

    //Called to add given array into one
    public void mergeArray(byte[] inputArray){
        if(outputByteArray==null){
            outputByteArray=inputArray;
        }
        else{
            outputByteArray=concatenateTwoArrays(outputByteArray, inputArray);
        }
    }

    //Called to return all given arrays as one and reset it
    public byte[] getAndResetOutputByteArray(){
        byte[] tempArr=outputByteArray;
        outputByteArray=null;
        return tempArr;
    }
}
