# fingerprint-reader-daemon

A daemon service allowing web apps to read data from biometric devices, currently working with fingerprint readers, tested with [Digital Persona u.are.u 4500](https://www.neurotechnology.com/fingerprint-scanner-digitalpersona-u-are-u-4500.html) and the [Neurotechnology Standard SDK](https://www.neurotechnology.com/).


## Disclaimer
**THIS IS NOT READY FOR PRODUCTION**, use it ar your own risk.

This work was done as a PoC, being a piece for a bigger project, there are several things that aren't implemented on the security side, like checking which websites are allowed to read the fingerprints, dealing with several devices connected, etc.

But, this can certainly help you to get started.

**NOTE**: Avoid using Windows VMs, we spent lots of time trying that but back then, there was a known bug on the SDK that prevented us from doing that, the workaround is to setup a license server which is tricky.


## Run
While the project should work on linux (tried in ubuntu 18.04), we only have instructions for Windows.

You will require:
- Java 8 (it may work with newer versions but we haven't tested them).
- Gradle

### Run for windows
First, you need to install the SDK from [Neurotechnology Standard SDK](https://www.neurotechnology.com/), we tried [Neurotec_Biometric_11_1_SDK_2019-07-07](https://download.neurotechnology.com/Neurotec_Biometric_11_1_SDK_2019-07-07.zip) but should likely work with newer versions and we suggest you to use the latest one.

Everything must be run the as an **administrator**.

Save in `$SDKDIR` your `Neurotec_Biometric_11_1_SDK` path, for example (replace your SDK path):
- For powershell, run `Set-Variable -name "SDKDIR" -Value "C:\Users\jonsa\Desktop\neurotec\Neurotec_Biometric_11_1_SDK"`
- For cmd, run `set $SDKDIR="C:\Users\youruser\Desktop\neurotec\Neurotec_Biometric_11_1_SDK"`


#### Run the SDK activation service
It is necessary to run the activation licenses in the background, once the SDK is downloaded and uncompressed:

```sh
cd $SDKDIR/Bin/Win64_x64/Activation
./pg.exe -install
```

You can stop the service with `./pg.exe -uninstall`


#### Run the daemon
Be sure to keep your fingerprint reader connected, and make sure that the necessary drivers are installed, you can try the SDK examples to make sure that your computer can read the fingerprints.

We assume you already cloned our project, then, `gradle run` should be enough.


#### Troubleshooting
If you get a License error problem you must copy the dependencies from the SDK, for example:

```sh
cp $SDKDIR/Bin/Win64_x64/*.dll enroll-finger-from-scanner/build/classes/java/main
cp $SDKDIR/Bin/Win64_x64/FScanners/NdmDigitalPersonaUareU/*.dll enroll-finger-from-scanner/build/classes/java/main
cp -r $SDKDIR/Bin/Data enroll-finger-from-scanner/build/classes/java/main
```

## Get a fingerprint
You can use any http client (like postman, httpie, or even your webapp) to execute the following request `POST localhost:1212/fingerprints`, which will read a fingerprint from the connected device.

Which initializes the fingerprint reader and waits until you capture a fingerprint, once done, it returns the base64-enconded image and it's template, like:

```json
{
    "data": {
        "image": "aabbcc......",
        "template": "aabbccdd...."
    }
}
```

When there is an error, you should get a response like:
```json
{
    "error": "You are already trying to read a fingerprint, try after completing that one"
}
```

The handled errors are:
- Trying to read a fingerprint when there aren't devices available.
- Trying to read a fingerprint when you are already reading another fingerprint.
- Reading a fingerprint timed out, you took too long.
