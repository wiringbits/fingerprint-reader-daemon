import static spark.Spark.*;
import org.json.JSONObject;
import java.util.Base64;
import java.util.concurrent.*;

public final class EnrollFingerFromScanner {
  public static void main(String[] args) {

    final EnrollFingerFromScannerBiometric enrollFromScanner;
    
    try {
      enrollFromScanner = new EnrollFingerFromScannerBiometric();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return;
    }
    
    port(1212);
    post("/fingerprints", (req, res) -> {
      
      res.type("application/json");
      
      JSONObject jsonResponse = new JSONObject();
      final ScanThread scanThread = new ScanThread(enrollFromScanner);

      try {

        final FingerPrintDetails fingerPrintDetails = scanFingerPrint(scanThread);

        jsonResponse 
          .put("data", new JSONObject()
            .put("image", new String(fingerPrintDetails.getImageBytes()))
            .put("template", new String(fingerPrintDetails.getTemplateBytes()))
        );

      } catch (BScannerException e) {

        jsonResponse 
          .put("error", e.getMessage());
        res.status(e.getCode());

      } catch (TimeoutException e) {

        scanThread.stopScan();
        jsonResponse 
          .put("error", "Timeout waiting for scan");
        res.status(413);

      } catch (Exception e) {

        jsonResponse 
          .put("error", "Unknown error: [" + e.getMessage() + "]");
        res.status(500);

      } finally {

        scanThread.stopThread();

      }

      return jsonResponse.toString();
    });
  }

  private static FingerPrintDetails scanFingerPrint(
    ScanThread scanThread) throws Exception {
      
      Future<FingerPrintDetails> future = scanThread.start();
      
      FingerPrintDetails fingerPrintDetails = future.get(3000, TimeUnit.MILLISECONDS);
      // if internal exception
      if (fingerPrintDetails == null) {
        throw scanThread.getException();
      }

      byte[] imageBytes = fingerPrintDetails.getImageBytes();
      byte[] templateBytes = fingerPrintDetails.getTemplateBytes();

      byte[] imageEncoded = Base64
        .getEncoder()
        .encode(imageBytes);
      byte[] templateEncoded = Base64
        .getEncoder()
        .encode(templateBytes);

      return new FingerPrintDetails(imageEncoded, templateEncoded);
  }
}
