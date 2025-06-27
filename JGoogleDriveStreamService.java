import java.net.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;

public class JGoogleDriveStreamService {
   private String googleUrl = null;
   private String lastLink = null;
   private static final Logger logger = Logger.getLogger(JGoogleDriveStreamService.class.getName());
   private static final Duration CACHE_TIMEOUT = Duration.ofSeconds(900);
   private static final Path CACHE_DIR = Paths.get("cache");

   // set and get methods
   public void setGoogleUrl(String googleUrl) {
      this.googleUrl = googleUrl;
   }

   public String getLastLink() {
      return this.lastLink;
   }

   public void setLastLink(String lastLink) {
      this.lastLink = lastLink;
   }

   // overloading constructors
   JGoogleDriveStreamService() {
   }

   JGoogleDriveStreamService(String googleUrl) {
      this.googleUrl = googleUrl;
   }

   // build method
   void buildN() {
      if (this.googleUrl != null) {
         try {
            fetchDownloadLink(this.googleUrl);
         } catch (IOException ex) {
            JGoogleDriveStreamService.logger.log(Level.SEVERE, null, ex);
         }
      }
   }

   public String fetchDownloadLink(String originalLink) throws IOException {
      String cacheKey = generateCacheKey(originalLink);
      Path cacheFile = CACHE_DIR.resolve(cacheKey + ".cache");

      if (Files.isRegularFile(cacheFile)) {
         return loadFromCache(cacheFile)
               .orElseGet(() -> refreshCache(originalLink, cacheFile));
      }

      return refreshCache(originalLink, cacheFile);
   }

   private Optional<String> loadFromCache(Path cacheFile) {
      try {
         String content = Files.readString(cacheFile);
         String[] parts = content.split("@@", 2);
         Instant cachedAt = Instant.ofEpochSecond(Long.parseLong(parts[0]));
         String cachedLink = parts[1];

         if (Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TIMEOUT) < 0) {
            logger.info("Using cached link for " + cacheFile.getFileName() +
                  " (cached " + Duration.between(cachedAt, Instant.now()).getSeconds() + " seconds ago)");
            lastLink = cachedLink;
            return Optional.of(cachedLink);
         }
      } catch (Exception e) {
         logger.log(Level.WARNING, "Failed to read cache " + cacheFile + ": " + e.getMessage());
      }
      return Optional.empty();
   }

   private String refreshCache(String originalLink, Path cacheFile) {
      String driveId;
      try {
         driveId = extractGoogleDriveId(originalLink);
      } catch (UnsupportedEncodingException e) {
         throw new IllegalArgumentException("Invalid Google Drive link: " + originalLink, e);
      }
      logger.info("Extracted Drive ID: " + driveId);

      String downloadLink = Optional.ofNullable(generateDownloadLink(driveId))
            .map(String::trim)
            .orElse("");
      logger.info("Generated download link: " + downloadLink);

      saveToCache(cacheFile, downloadLink);
      lastLink = downloadLink;
      return downloadLink;
   }

   private void saveToCache(Path cacheFile, String link) {
      try {
         Files.createDirectories(cacheFile.getParent());
         String content = Instant.now().getEpochSecond() + "@@" + link;
         Files.writeString(cacheFile, content);
         logger.info("Saved new cache at {}" + cacheFile);
      } catch (IOException e) {
         logger.log(Level.WARNING, "Unable to write cache " + cacheFile + ": " + e.getMessage());
      }
   }

   private String generateCacheKey(String link) {
      // Example MD5-based key; implement getMd5 accordingly
      return getMd5("AA" + link + "A3Code");
   }

   public static String extractGoogleDriveId(String googleDriveUrl)
         throws UnsupportedEncodingException {
      String idFromQuery = extractIdFromQueryParameter(googleDriveUrl);
      if (!idFromQuery.isBlank()) {
         return idFromQuery;
      }

      String idFromPath = extractIdFromPath(googleDriveUrl);
      if (!idFromPath.isBlank()) {
         return idFromPath;
      }

      throw new IllegalArgumentException(
            "Could not extract a Google Drive ID from URL: " + googleDriveUrl);
   }

   private static String extractIdFromQueryParameter(String url)
         throws UnsupportedEncodingException {
      Pattern pattern = Pattern.compile("[?&]id=([^&]*)");
      Matcher matcher = pattern.matcher(url);

      if (matcher.find()) {
         String encodedId = matcher.group(1);
         return URLDecoder.decode(encodedId, "UTF-8");
      }
      return "";
   }

   private static String extractIdFromPath(String url) {
      // catch both /file/d/<id> and /folders/<id>
      Pattern pattern = Pattern.compile("/(?:file/d|folders)/([a-zA-Z0-9_-]+)");
      Matcher matcher = pattern.matcher(url);

      if (matcher.find()) {
         return matcher.group(1);
      }
      return "";
   }

   public static String generateDownloadLink(String fileId) {
      String baseUrl = "https://drive.google.com/uc?export=download&id=";

      String downloadUrl = baseUrl + fileId;
      return downloadUrl;
   }

   // Md5 hash creator
   public static String getMd5(String input) {
      try {
         // Static getInstance method is called with hashing MD5
         MessageDigest md = MessageDigest.getInstance("MD5");
         // digest() method is called to calculate message digest
         // of an input digest() return array of byte
         byte[] messageDigest = md.digest(input.getBytes());
         // Convert byte array into signum representation
         BigInteger no = new BigInteger(1, messageDigest);
         // Convert message digest into hex value
         String hashtext = no.toString(16);
         while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
         }
         return hashtext;
      }
      // For specifying wrong message digest algorithms
      catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
      }
   }

   // php explode method
   public static String[] explode(String separator, String stringToExplode) {
      return stringToExplode.split(separator);
   }

   // Get string format belated date param @delay
   public static String getDateStr(String format, long delay) {
      String timeStamp;
      timeStamp = new SimpleDateFormat(format).format(new Date((delay) + (Calendar.getInstance().getTime()).getTime()));
      return timeStamp;
   }

   // Get belated date param @delay
   public static Date getDate(String format, long delay) {
      Date timeStamp = new Date((delay) + (Calendar.getInstance().getTime()).getTime());
      return timeStamp;
   }

   // Write @data to @filename
   private static void fwrite(String filename, String data) {
      try {
         Files.write(Paths.get(filename), data.getBytes());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   // Read data from @filename
   public static String fileGetContents(String filename) throws IOException {
      String data = "";
      List<String> readList = Files.readAllLines(Paths.get(filename));
      for (String r : readList) {
         data += r;
      }
      return data;
   }

   // Select element of respond header @uri from given map data
   public static Map<String, String> splitQuery(String uri)
         throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
      URL url = new URI(uri).toURL();
      Map<String, String> query_pairs = new LinkedHashMap<String, String>();
      String query = url.getQuery();
      String[] pairs = query.split("&");
      for (String pair : pairs) {
         int idx = pair.indexOf("=");
         query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
               URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
      return query_pairs;
   }

   // New cache
   public static String saveGoogleDriveCache(String link, String source) {
      String msn = "";
      Date time = getDate("yyyy-MM-dd HH:mm:ss", 7 * 3600 * 1000);
      String filename = getMd5("AA" + link + "A3Code");
      String string = time.getTime() / 1000 + "@@" + source;
      File directory = new File("cache");
      if (!directory.exists()) {
         directory.mkdir();
      }
      fwrite("cache/" + filename + ".cache", string);
      File file = new File("cache/" + filename + ".cache");
      if (file.exists() && file.isFile()) {
         msn = string;
      } else {
         msn = string;
      }
      return msn;
   }

   public static String locheader(Map<String, List<String>> header) {
      // String[] temp = explode("\r\n", page);String location=null;
      Map<String, List<String>> infoheader = header;
      String location = null;
      // To get a map of all the fields of http header

      // print all the fields along with their value.
      for (Map.Entry<String, List<String>> mp : infoheader.entrySet()) {
         System.out.print(mp.getKey() + " : ");
         System.out.println(mp.getValue().toString());
      }
      List<String> loc1 = infoheader.get("Location");
      if (loc1 != null) {
         location = loc1.get(0);
      } else {
         // location = "";
      }
      return location;
   }

   public static String getElementList(String ele, Map<String, List<String>> mapList) {
      String listEle = "";
      listEle = (mapList.get(ele)).get(0);

      for (int i = 0; i < listEle.length(); i++) {
         char l = listEle.charAt(i);
         if (l == ';') {
            listEle = listEle.substring(0, i + 1);
            break;
         }
      }

      return listEle;
   }

   // Server request (Get header)
   public static String GetHTML(HttpURLConnection urlConn) throws IOException {
      String HTML = "";
      BufferedReader in;
      in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
      String stringbuffer = "";
      while ((stringbuffer = in.readLine()) != null) {
         HTML += stringbuffer;
      }
      return HTML;
   }
}