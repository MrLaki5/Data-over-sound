package games.mrlaki5.soundtest.DataTransfer;

public class FileExplorerElement {
    private String FileName;
    private String FileSize;
    private boolean isFile;

    public FileExplorerElement(String fileName, String fileSize, boolean isFile) {
        FileName = fileName;
        FileSize = fileSize;
        this.isFile = isFile;
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
}
