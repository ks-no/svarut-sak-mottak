package no.ks.sakimport.fake;

import no.ks.svarut.sakimport.kryptering.CMSDataKryptering;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class KrypterFiler {

    @Test
    public void testKrypterFil() throws Exception {
        byte[] fil = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("sp-cert.pem"));
        X509Certificate sertifikat = lesSertifikat(fil);

        CMSDataKryptering kryptering = new CMSDataKryptering();

        byte[] kryptert = kryptering.krypterData(IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream("dokumenter.zip")), sertifikat);

        IOUtils.write(kryptert, new FileOutputStream("dokumenter.crypt"));

    }

    X509Certificate lesSertifikat(byte[] sertifikat) {
        try {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(sertifikat));
        } catch (CertificateException e) {
            throw new RuntimeException("Klarte ikke å lese sertifikat", e);
        }
    }
}
