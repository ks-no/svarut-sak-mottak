package no.ks.svarut.sakimport.kryptering;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAESOAEPparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;

public class CMSDataKryptering {

    private final ASN1ObjectIdentifier cmsEncryptionAlgorithm;
    private final AlgorithmIdentifier keyEncryptionScheme;

    public CMSDataKryptering() {
        this.keyEncryptionScheme = this.rsaesOaepIdentifier();
        this.cmsEncryptionAlgorithm = CMSAlgorithm.AES256_CBC;
    }

    private AlgorithmIdentifier rsaesOaepIdentifier() {
        AlgorithmIdentifier hash = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE);
        AlgorithmIdentifier mask = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hash);
        AlgorithmIdentifier pSource = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(new byte[0]));
        RSAESOAEPparams parameters = new RSAESOAEPparams(hash, mask, pSource);
        return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP, parameters);
    }

    public byte[] dekrypterData(byte[] data, PrivateKey key, Provider p) {

        try {
            // Initialise parser
            CMSEnvelopedDataParser envDataParser = new CMSEnvelopedDataParser(data);
            RecipientInformationStore recipients = envDataParser.getRecipientInfos();

            RecipientInformation recipient = (RecipientInformation) recipients.getRecipients().iterator().next();

            byte[] envelopedData = recipient.getContent(new JceKeyTransEnvelopedRecipient(key).setProvider(p));

            return envelopedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] dekrypterData(byte[] data, PrivateKey key) {
        byte[] cleardata = null;
        try {
            // Initialise parser
            CMSEnvelopedDataParser envDataParser = new CMSEnvelopedDataParser(data);
            RecipientInformationStore recipients = envDataParser.getRecipientInfos();

            RecipientInformation recipient = (RecipientInformation) recipients.getRecipients().iterator().next();

            byte[] envelopedData = recipient.getContent(new JceKeyTransEnvelopedRecipient(key).setProvider("BC"));

            return envelopedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] krypterData(byte[] bytes, X509Certificate sertifikat) {
        try {
            JceKeyTransRecipientInfoGenerator e = (new JceKeyTransRecipientInfoGenerator(sertifikat, this.keyEncryptionScheme)).setProvider("BC");
            CMSEnvelopedDataGenerator envelopedDataGenerator = new CMSEnvelopedDataGenerator();
            envelopedDataGenerator.addRecipientInfoGenerator(e);
            OutputEncryptor contentEncryptor = (new JceCMSContentEncryptorBuilder(this.cmsEncryptionAlgorithm)).build();

            CMSEnvelopedData cmsData = envelopedDataGenerator.generate(new CMSProcessableByteArray(bytes), contentEncryptor);
            return cmsData.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Kunne ikke kryptere", e);
        }
    }

    public InputStream dekrypterData(InputStream encryptedStream, PrivateKey key, Provider p) {
        try {
            // Initialise parser
            CMSEnvelopedDataParser envDataParser = new CMSEnvelopedDataParser(encryptedStream);
            RecipientInformationStore recipients = envDataParser.getRecipientInfos();

            RecipientInformation recipient = (RecipientInformation) recipients.getRecipients().iterator().next();

            CMSTypedStream envelopedData = recipient.getContentStream(new JceKeyTransEnvelopedRecipient(key).setProvider(p));
            return envelopedData.getContentStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
