package no.ks.svarut.sakimport.kryptering;

import no.ks.svarut.sakimport.GI.SakImportConfig;
import no.ks.svarut.sakimport.util.FileLoadUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class Kryptering {

    private final SakImportConfig config;
    CMSDataKryptering cmsDataKryptering;

    public Kryptering(SakImportConfig config) {
        this.config = config;
        cmsDataKryptering = new CMSDataKryptering();
    }

    public InputStream dekrypterForSvarUt(InputStream encryptedStream){
        final PrivateKey svarut;

        try {
            svarut = getPrivateKey();
            return cmsDataKryptering.dekrypterData(encryptedStream, svarut, new BouncyCastleProvider());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] dekrypterForSvarUt(byte[] encryptedData){
        final PrivateKey svarut;
        try {
            svarut = getPrivateKey();
            return cmsDataKryptering.dekrypterData(encryptedData, svarut, new BouncyCastleProvider());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );

        //bruk config
        final PemReader pemReader = new PemReader(new InputStreamReader(FileLoadUtil.getInputStreamForFileFromClasspath(config.getPrivateKeyFil())));
        final PemObject pemObject = pemReader.readPemObject();
        byte[] keyBytes = pemObject.getContent();

        // generate private key
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
}
