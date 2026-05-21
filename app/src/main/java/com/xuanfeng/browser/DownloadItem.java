package com.xuanfeng.browser;

public class DownloadItem {
    private String fileName;
    private String filePath;
    private long fileSize;
    private String downloadTime;
    private String fileType;
    private int status;
    private int progress;
    private boolean isForeign;
    private String taskId;
    
    
    
	public DownloadItem(String fileName, String filePath) {
   	 this.fileName = fileName;
	    this.filePath = filePath;
  	  this.taskId = String.valueOf(System.currentTimeMillis());
	    this.isForeign = true;
  	  this.status = 0;
	}

    // getters and setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    
    public String getDownloadTime() { return downloadTime; }
    public void setDownloadTime(String downloadTime) { this.downloadTime = downloadTime; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public boolean isForeign() { return isForeign; }
    public void setForeign(boolean foreign) { isForeign = foreign; }
    
    // 格式化文件大小
    public String getFormattedSize() {
        if (fileSize <= 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }
	public String getTaskId() {
    	return taskId;
	}

	public void setTaskId(String taskId) {
   	 this.taskId = taskId;
	}
}