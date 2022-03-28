package tool;

import com.drew.imaging.ImageProcessingException;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    final static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws ImageProcessingException, IOException {
        // a message that asks to provide a path to the directory
        providePathMessage();

        // the directory that contains the images
        File dir = getValidDir();

        File[] imageFiles = IMGFileUtils.getAllImages(dir);

        while (imageFiles.length == 0) {
            System.out.println("\nNo JPG/JPEG images found!");
            System.out.println("""
                    Provide a different path?
                    1 - Yes
                    2 - No""");

            String answer = sc.nextLine();

            while (!answer.equals("1") && !answer.equals("2")) {
                System.out.println("\nInvalid answer");
                System.out.println("""
                        Provide a different path?
                        1 - Yes
                        2 - No""");

                answer = sc.nextLine();
            }

            if (answer.equals("1")) {
                System.out.print("Path: ");
                dir = getValidDir();
                imageFiles = IMGFileUtils.getAllImages(dir);
            } else {
                System.out.println("\nExiting...");
                System.exit(0);
            }
        }

        boolean skipConfirmation = false;            // skip the confirmation prompt on each step?
        int renamedImagesCount = 0;                  // how many images will be renamed, used for outputting custom text to the user
        int alreadyRenamedImagesCount = 0;           // images that have previously been renamed, used for outputting custom text to the user
        for (File image : imageFiles) {
            // if a file does not contain EXIF info to extract (no creation date)
            if (!IMGFileUtils.hasEXIF(image)) {
                System.out.printf("Image [%s] does not contain EXIF information. The creation date could not be extracted!\n", image.getAbsoluteFile());
                if (imageFiles.length > 1) {
                    System.out.println("Continuing to the next image...");
                    continue;
                }
                break;  // if there was only 1 image in the dir, the date of which could not be extracted, break the loop
            }

            // new name
            String creationDate = IMGFileUtils.extractCreationDate(image);

            // skip images that have already been renamed
            if (IMGFileUtils.isRenamed(image, image.getName(), creationDate)) {
                alreadyRenamedImagesCount++;
                continue;
            }

            if (!skipConfirmation) {
                printRenameOptions(image.getName(), creationDate);
                String answer = sc.nextLine();
                if (answer.equals("1")) {
                    IMGFileUtils.renameImage(image, creationDate);
                    renamedImagesCount++;
                } else if (answer.equals("3")) {
                    skipConfirmation = true;
                    IMGFileUtils.renameImage(image, creationDate);
                    renamedImagesCount++;
                } else {
                    System.out.println("Image renaming skipped!");
                }
            } else {
                IMGFileUtils.renameImage(image, creationDate);
                renamedImagesCount++;
            }
        }

        if (renamedImagesCount >= 1) {
            System.out.printf("Successfully renamed %d images!", renamedImagesCount);
        } else if (alreadyRenamedImagesCount == imageFiles.length) {
            System.out.println("""
                    All the images are already renamed!
                    Exiting...""");
        } else {
            System.out.println("""
                    No images have been renamed!
                    Exiting...""");
        }
    }

    public static void printRenameOptions(String currentName, String creationDate) {
        System.out.printf("""
                                 
                Do you want to rename [%s] to [%s.jpeg]?
                1 - Yes
                2 - No
                3 - Yes to all
                """, currentName, creationDate);
    }

    public static void providePathMessage() {
        System.out.println("Please provide a full (absolute) path to the directory.");
        System.out.println("Example usage: C:\\Directory1\\Directory2\\Directory3");
        System.out.println("- Type 0 and press enter to exit -");
    }

    public static File getValidDir() {
        File dir;

        // loop until a valid path is provided
        while (true) {
            String path = sc.nextLine();
            if (path.equals("0")) {
                System.out.println("Exiting...");
                System.exit(0);
            }
            dir = new File(path);
            if (dir.isDirectory()) {
                break;
            }
            providePathMessage();
        }
        return dir;
    }
}