package com.zake.aicode.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectzDownloadService {
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
