package tool;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IMGFileUtils {
    public static File[] getAllImages(File directory) {
        String[] extensions = new String[]{".jpg", ".jpeg"};    // add more extensions in case of renaming additional formats
        return directory.listFiles((dir, name) -> {
            for (String ext : extensions) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        });
    }

    // check if a file contains EXIF information
    public static boolean hasEXIF(File image) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(image);
        } catch (ImageProcessingException | IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return metadata.containsDirectoryOfType(ExifSubIFDDirectory.class);
    }

    // creation date format YYYY-MM-DD hh:ss (year-month-day hour:second)
    public static String extractCreationDate(File image) throws ImageProcessingException, IOException {
        if (hasEXIF(image)) {
            Metadata metadata = ImageMetadataReader.readMetadata(image);
            Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            return formattedDate(date);
        }
        return "";
    }

    private static String formattedDate(Date date) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH꞉ss");
        return formatter.format(date);
    }

    public static void renameImage(File image, String newName) {
        String extension = getExtension(image);
        image.renameTo(new File(image.getParent() + "\\" + newName + extension));
    }

    public static boolean isRenamed(File image, String oldName, String newName) {
        int extensionPos = image.getName().lastIndexOf(".");
        return oldName.substring(0, extensionPos).equals(newName);
    }

    public static String getExtension(File image) {
        String name = image.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf);
    }
}
