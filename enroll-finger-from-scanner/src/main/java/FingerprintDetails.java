import lombok.*;

@Value class FingerPrintDetails {
    byte[] imageBytes;
    byte[] templateBytes;
}