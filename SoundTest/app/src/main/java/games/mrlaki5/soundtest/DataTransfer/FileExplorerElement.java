package games.mrlaki5.soundtest.DataTransfer;

//Bean for files in file explorer
public class FileExplorerElement {

    //Name of file
    private String FileName;
    //ize of file (if its not folder or back)
    private String FileSize;
    //True if file, False if folder or back
    private boolean isFile;
    //True if back, False if folder or file
    private boolean isBack;

    public FileExplorerElement(String fileName, String fileSize, boolean isFile, boolean isBack) {
        FileName = fileName;
        FileSize = fileSize;
        this.isFile = isFile;
        this.isBack=isBack;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public String getFileSize() {
        return FileSize;
    }

    public void setFileSize(String fileSize) {
        FileSize = fileSize;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public boolean isBack() {
        return isBack;
    }

    public void setBack(boolean back) {
        isBack = back;
    }
}
