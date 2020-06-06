package org.coralibre.android.sdk.internal.crypto;

import android.util.Log;
import android.util.Pair;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.coralibre.android.sdk.internal.crypto.ppcp.AssociatedEncryptedMetadata;
import org.coralibre.android.sdk.internal.crypto.ppcp.AssociatedEncryptedMetadataKey;
import org.coralibre.android.sdk.internal.crypto.ppcp.AssociatedMetadata;
import org.coralibre.android.sdk.internal.crypto.ppcp.CryptoModule;
import org.coralibre.android.sdk.internal.crypto.ppcp.ENNumber;
import org.coralibre.android.sdk.internal.crypto.ppcp.PaddedData;
import org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifier;
import org.coralibre.android.sdk.internal.crypto.ppcp.RollingProximityIdentifierKey;
import org.coralibre.android.sdk.internal.crypto.ppcp.TemporaryExposureKey;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CryptoModuleTests {
    private static final byte[] TEK_VAL1 = new byte[] {
            (byte) 0x12,
            (byte) 0x34,
            (byte) 0x56,
            (byte) 0x67,
            (byte) 0x78,
            (byte) 0x9A,
            (byte) 0xBC,
            (byte) 0xDE,
            (byte) 0xF1,
            (byte) 0x23,
            (byte) 0x45,
            (byte) 0x67,
            (byte) 0x89,
            (byte) 0xAB,
            (byte) 0xCD,
            (byte) 0xEF
    };

    //value generated by hkdf.py
    //hkdf code was taken from https://en.wikipedia.org/wiki/HKDF#Example:_Python_implementation
    private static final byte[] RPIK_VAL1 = new byte[] {
            (byte) 249,
            (byte) 156,
            (byte) 130,
            (byte) 43,
            (byte) 122,
            (byte) 202,
            (byte) 124,
            (byte) 152,
            (byte) 91,
            (byte) 88,
            (byte) 171,
            (byte) 163,
            (byte) 126,
            (byte) 114,
            (byte) 30,
            (byte) 19,
    };

    private static final byte[] AEMK_VAL1 = new byte[] {
            (byte) 165,
            (byte) 243,
            (byte) 80,
            (byte) 172,
            (byte) 127,
            (byte) 63,
            (byte) 77,
            (byte) 120,
            (byte) 194,
            (byte) 162,
            (byte) 245,
            (byte) 33,
            (byte) 108,
            (byte) 134,
            (byte) 125,
            (byte) 231,
    };

    private static final byte[] RPI_VAL1 = new byte[] {
            61,
            -78,
            -88,
            -126,
            104,
            -111,
            35,
            -19,
            51,
            72,
            120,
            -35,
            -45,
            -110,
            -111,
            -7
    };

    private static final byte[] AM_VAL_V3_2_MINUS16db = {(byte) 0b11100000, (byte) 0xF0, 0x00, 0x00};

    @Test
    public void testGenerateRPIK() throws Exception {
        TemporaryExposureKey tek = new TemporaryExposureKey(new Pair<>(0L, TEK_VAL1));
        RollingProximityIdentifierKey rpik = CryptoModule.generateRPIK(tek);
        assertArrayEquals(RPIK_VAL1, rpik.getKey());
    }

    @Test
    public void testGenerateAEMK() throws Exception {
        TemporaryExposureKey tek = new TemporaryExposureKey(new Pair<>(0L, TEK_VAL1));
        AssociatedEncryptedMetadataKey aemk = CryptoModule.generateAEMK(tek);
        assertArrayEquals(AEMK_VAL1, aemk.getKey());
    }

    @Test
    public void testEncryptDecryptRPI() throws Exception {
        RollingProximityIdentifierKey rpik = new RollingProximityIdentifierKey(RPIK_VAL1);
        //encrypt
        RollingProximityIdentifier rpi = CryptoModule.generateRPI(rpik);
        //decrypt
        PaddedData pd = CryptoModule.decryptRPI(rpi, rpik);
        assertTrue("RPI decryption failed", pd.isRPIInfoValid());
    }

    @Test
    public void testEncryptDecryptAEM() throws Exception {
        AssociatedMetadata am = new AssociatedMetadata(AM_VAL_V3_2_MINUS16db);
        RollingProximityIdentifier rpi = new RollingProximityIdentifier(RPI_VAL1,
                new ENNumber(0));
        AssociatedEncryptedMetadataKey aemk = new AssociatedEncryptedMetadataKey(AEMK_VAL1);

        AssociatedEncryptedMetadata aem = CryptoModule.encryptAM(am, rpi, aemk);
        AssociatedMetadata decryptedAM = CryptoModule.decryptAEM(aem, rpi, aemk);
        assertArrayEquals(am.getData(), decryptedAM.getData());
    }
}
