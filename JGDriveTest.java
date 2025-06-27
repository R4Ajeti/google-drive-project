public class JGDriveTest {
   public static void main(String[] args) {
      if (args.length == 0 || args[0].isBlank()) {
         System.err.println("Usage: java JGDriveTest <google-drive-url>");
         System.exit(1);
      }
      String googleDriveUrl = args[0];
      JGoogleDriveStreamService jGoogleDriveStreamServiceObject = new JGoogleDriveStreamService(
            googleDriveUrl);
      jGoogleDriveStreamServiceObject.buildN();
      System.out.println(jGoogleDriveStreamServiceObject.getLastLink());
   }
}