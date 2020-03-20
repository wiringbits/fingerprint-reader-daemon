import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;;

class ScanThread {
  private Exception exception = null;
  private EnrollFingerFromScannerBiometric enrollFromScanner = null;
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  ScanThread(EnrollFingerFromScannerBiometric enrollFromScanner) {
    this.enrollFromScanner = enrollFromScanner;
  }

  public Future<FingerPrintDetails> start() {
    return executor.submit(() -> {
        try {
          return enrollFromScanner.scanFingerPrint();
        } catch (BScannerException e) {
          exception = e;
        } catch (Exception e) {
          exception = e;
        }
        
        return null;
    });
  }

  public void stopScan() {
    enrollFromScanner.cancelScanner();
  }

  public void stopThread() {
    executor.shutdown();
  }

  public synchronized Exception getException() {
    return exception;
  }
}
