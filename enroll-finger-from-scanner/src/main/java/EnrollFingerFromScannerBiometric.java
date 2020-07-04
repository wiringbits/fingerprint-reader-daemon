import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFinger;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplateSize;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.devices.NFScanner;
import com.neurotec.devices.NDeviceManager.DeviceCollection;
import com.neurotec.licensing.NLicense;
import com.neurotec.licensing.NLicenseManager;
import com.neurotec.tutorials.util.LibraryManager;
import com.neurotec.tutorials.util.Utils;

public final class EnrollFingerFromScannerBiometric {
	private NBiometricClient biometricClient = null;
	private final AtomicBoolean currentWorking = new AtomicBoolean(false);

	public EnrollFingerFromScannerBiometric() throws Exception {
		LibraryManager.initLibraryPath();

		// other licenses: FingerClient, FingerFastExtractor
		final String license = "FingerExtractor";

		boolean trialMode = Utils.getTrialModeFlag();
		NLicenseManager.setTrialMode(trialMode);
		System.out.println("\tTrial mode: " + trialMode);

		if (!NLicense.obtain("/local", 5000, license)) {
			System.err.format("Could not obtain license: %s%n", license);
			throw new Exception("Failed to obtain licenses");
		}

		biometricClient = new NBiometricClient();
		biometricClient.setUseDeviceManager(true);
	}

	public void cancelScanner() {
		biometricClient.cancel();
		currentWorking.set(false);
	}

	public FingerPrintDetails scanFingerPrint() throws Exception {
		if ( ! currentWorking.compareAndSet(false, true)) {
			throw new BScannerException("You are already trying to read a fingerprint, try after completing that one", 409);
		}

		NFinger finger = new NFinger();
		NSubject subject = new NSubject();

		try {
			selectScanner();

			subject.getFingers().add(finger);

			System.out.println("Capturing....");
			NBiometricStatus status = biometricClient.capture(subject);
			if (status != NBiometricStatus.OK) {
				System.out.format("Failed to capture: %s\n", status);
				throw new Exception("Failed to capture");
			}

			biometricClient.setFingersTemplateSize(NTemplateSize.LARGE);

			status = biometricClient.createTemplate(subject);

			if (status == NBiometricStatus.OK) {
				System.out.println("Template extracted");
			} else {
				System.out.format("Extraction failed: %s\n", status);
				throw new Exception("Extraction failed");
			}

			final Image imageFinger = subject.getFingers()
					.get(0)
					.getImage()
					.toImage();
			BufferedImage bufferedImageFinger = toBufferedImage(imageFinger);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImageFinger, "png", outputStream);
			byte[] bytesImageFinger = outputStream.toByteArray();
			System.out.println("Fingerprint image saved successfully...");
			
			byte[] bytesTemplate = subject
				.getTemplate()
				.save()
				.toByteArray();
			System.out.println("Template file saved successfully...");

			return new FingerPrintDetails(bytesImageFinger, bytesTemplate);
		} finally {
			if (finger != null) finger.dispose();
			if (subject != null) subject.dispose();
			currentWorking.set(false);
		}
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		return bimage;
	}

	private void selectScanner() throws Exception {
		
		NDeviceManager deviceManager = biometricClient.getDeviceManager();

		deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.FINGER_SCANNER));

		deviceManager.initialize();

		DeviceCollection devices = deviceManager.getDevices();

		if (devices.size() > 0) {
			System.out.format("Found %d fingerprint scanner\n", devices.size());
		} else {
			System.out.format("No scanners found\n");
			throw new BScannerException("There are no devices connected to read the fingerprint", 500);
		}

		if (devices.size() > 1) {

			System.out.println("Multiple detected scanners");
			for (int i = 0; i < devices.size(); i++) {
        System.out.format("\t%d. %s\n", i + 1, devices.get(i).getDisplayName());
      }

			System.out.println("\nThe first one will be selected automatically");
		}

		int selectedScanner = 0;
		biometricClient.setFingerScanner((NFScanner) devices.get(selectedScanner));
	}
}
