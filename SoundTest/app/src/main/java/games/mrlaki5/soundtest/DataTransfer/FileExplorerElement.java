package games.mrlaki5.soundtest.DataTransfer;

public class FileExplorerElement {
    private String FileName;
    private String FileSize;
    private boolean isFile;
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
