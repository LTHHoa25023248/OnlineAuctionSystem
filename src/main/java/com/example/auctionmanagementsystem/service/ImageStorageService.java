package com.example.auctionmanagementsystem.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageStorageService {

   //lay thu muc trong home cua user +dau phan cach(separatoy)+ten thu muc luu anh
    private static final String IMAGE_FOLDER =System.getProperty("user.home") + File.separator + "auction_images";

    public static String save(File imageFile) throws IOException {
        //tao folder neu chua ton tai
        Path folder = Paths.get(IMAGE_FOLDER);
        // kiem tra chua ton tai thi tao moiu
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        // lay file bang UUID de tranh trung ten, giu nguyen duoi file (png,..) bang cach lay extention
        String extension = getExtension(imageFile.getName());
        String newFileName = UUID.randomUUID().toString() + extension;
        Path destination = folder.resolve(newFileName);
        Files.copy(imageFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }

    // Lay duong dan day du de JavaFX load anh len ImageView.
    // DB chi luu ten file ngan (abc.png), lay full path (C:\Users\Username\auction_images\abc.png), ham nay ghep voi thu muc goc khi can hien thi.
    public static String getFullPath(String fileName) {
        if (fileName == null) 
            return null;
        return IMAGE_FOLDER + File.separator + fileName;
    }
   private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if(dot >= 0){
        return fileName.substring(dot);
        }else{
         return "";
     }
    } 
}
