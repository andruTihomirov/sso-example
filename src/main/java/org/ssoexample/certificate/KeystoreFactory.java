package org.ssoexample.certificate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Slf4j
public class KeystoreFactory {

    private ResourceLoader resourceLoader;

    public KeystoreFactory() {
        resourceLoader = new DefaultResourceLoader();
    }

    public KeystoreFactory(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @SneakyThrows
    public KeyStore loadKeystore(String keyStorePath, String pass) {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("keystore.jks");
        keyStore.load(inputStream, pass.toCharArray());
        return keyStore;
    }

    @SneakyThrows
    public KeyStore loadKeystore(String certResourceLocation, String privateKeyResourceLocation,
                                 String alias, String keyPassword) {
        KeyStore keystore = createEmptyKeystore();
        X509Certificate cert = loadCert(certResourceLocation);
        RSAPrivateKey privateKey = loadPrivateKey(privateKeyResourceLocation);
        addKeyToKeystore(keystore, cert, privateKey, alias, keyPassword);
        return keystore;
    }

    @SneakyThrows
    public void addKeyToKeystore(KeyStore keyStore, X509Certificate cert, RSAPrivateKey privateKey,
                                 String alias, String password) {
        KeyStore.PasswordProtection pass = new KeyStore.PasswordProtection(password.toCharArray());
        Certificate[] certificateChain = {cert};
        try {
            keyStore.setEntry(alias, new KeyStore.PrivateKeyEntry(privateKey, certificateChain), pass);
        } catch (KeyStoreException e) {
            log.warn(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public KeyStore createEmptyKeystore() {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, new char[0]);
        return keyStore;
    }

    @SneakyThrows
    public X509Certificate loadCert(String certLocation) {
        CertificateFactory cf = CertificateFactory.getInstance("X509");
        Resource certRes = resourceLoader.getResource(certLocation);
        return (X509Certificate) cf.generateCertificate(certRes.getInputStream());
    }

    @SneakyThrows
    public RSAPrivateKey loadPrivateKey(String privateKeyLocation) {
        Resource keyRes = resourceLoader.getResource(privateKeyLocation);
        byte[] keyBytes = StreamUtils.copyToByteArray(keyRes.getInputStream());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    public void setResourceLoader(DefaultResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
