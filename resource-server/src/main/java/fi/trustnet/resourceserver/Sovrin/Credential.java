package fi.trustnet.resourceserver.Sovrin;

import com.github.jsonldjava.utils.JsonUtils;
import fi.trustnet.verifiablecredentials.VerifiableCredential;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.crypto.EC25519Provider;
import info.weboftrust.ldsignatures.signer.Ed25519Signature2018LdSigner;
import info.weboftrust.ldsignatures.validator.Ed25519Signature2018LdValidator;
import org.abstractj.kalium.NaCl;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.UUID;
import static fi.trustnet.resourceserver.configuration.IndyConfig.NETWORK_NAME;
import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_WALLETNAME;

public class Credential {

    public static String createCredential(String issuerDid, String subjectDid) throws Exception {
        String issuerSeed = "000000000000000000ResourceServer";
        byte[] issuerPrivateKey = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
        byte[] issuerPublicKey = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];

        EC25519Provider.get().generateEC25519KeyPairFromSeed(issuerPublicKey, issuerPrivateKey, issuerSeed.getBytes(StandardCharsets.UTF_8));

        VerifiableCredential verifiableCredential = new VerifiableCredential();
        verifiableCredential.getContext().add("https://energinet.dk/credentials/v1");
        verifiableCredential.getType().add("CustomerInfoCredential");
        verifiableCredential.setIssuer(URI.create(issuerDid));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));


        verifiableCredential.setIssued(dtf.format(now));

        verifiableCredential.setSubject(subjectDid);
        LinkedHashMap<String, Object> jsonLdClaimsObject = verifiableCredential.getJsonLdClaimsObject();
        LinkedHashMap<String, Object> jsonLdAccessScopeObject = new LinkedHashMap<String, Object> ();
        jsonLdAccessScopeObject.put("availablescopes", "electricity_metering_data");
        jsonLdAccessScopeObject.put("customerid", "192873465");
        jsonLdClaimsObject.put("claim", jsonLdAccessScopeObject);

        UUID uuid = UUID.randomUUID();

        LinkedHashMap<String, Object> jsonLdCredentialStatusObject = verifiableCredential.getJsonLdObject();
        LinkedHashMap<String, Object> jsonLdRevocatioObject = new LinkedHashMap<String, Object> ();
        jsonLdRevocatioObject.put("revocation", "http://51.15.82.243:8085/revocation/" + uuid.toString());
        jsonLdCredentialStatusObject.put("credentialStatus", jsonLdRevocatioObject);
        URI creator = URI.create(issuerDid + "#key1");

        dtf = DateTimeFormatter.ISO_INSTANT;
        String created = dtf.format(now.toInstant(ZoneOffset.UTC));
        String domain = null;

        String nonce = uuid.toString();

        // sign

        Ed25519Signature2018LdSigner signer = new Ed25519Signature2018LdSigner(creator, created, domain, nonce, issuerPrivateKey);
        LdSignature ldSignature = signer.sign(verifiableCredential.getJsonLdObject());
        return JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject());
        // output

            //  System.out.println("Signature Value: " + ldSignature.getSignatureValue());
            //return new ResponseEntity<String>(JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject()).toString(),HttpStatus.OK);
        }

    public static String getIssuer(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            return verifiableCredential.getIssuer().toString();
        }
        catch (Exception e){
            return "";
        }

    }

    public static String getSubject(String credential) {
        try {
        LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
        VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
        return verifiableCredential.getSubject();
    }
        catch (Exception e){
        return "";
    }

}

    public static String getRevocationUrl(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            //UGLY
            return ((LinkedHashMap)verifiableCredential.getJsonLdObject().get("credentialStatus")).get("revocation").toString();
        }
        catch (Exception e){
            return "";
        }

    }

    public static String getScope(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            LinkedHashMap claim = (LinkedHashMap)verifiableCredential.getJsonLdObject().get("claim");
            return ((LinkedHashMap)claim.get("claim")).get("authorizedscopes").toString();
        }
        catch (Exception e){
            return "";
        }

    }

    public static boolean verifyCredential(String credential){
            // parse verifiable credential
    try {
        LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
        VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);

        // discover issuer public key

        byte[] issuerPublicKey;
            URI issuer = verifiableCredential.getIssuer();
            if (!LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));
            Pool pool = Pool.openPoolLedger(NETWORK_NAME, "{}").get();
            Wallet wallet = Wallet.openWallet(RS_WALLETNAME, null, null).get();
            String key = Did.keyForDid(pool, wallet, issuer.toString()).get();
            wallet.closeWallet().get();
            pool.closePoolLedger().get();
            String issuerPublicKeyBase58 = key;
            issuerPublicKey = Base58.decode(issuerPublicKeyBase58);
            System.out.println("Issuer Public Key: " + Hex.encodeHexString(issuerPublicKey));
                    // verify verifiable credential

        Ed25519Signature2018LdValidator validator = new Ed25519Signature2018LdValidator(issuerPublicKey);
        boolean validate = validator.validate(verifiableCredential.getJsonLdObject());
        System.out.println(validate);
        return validate;
    }
    catch (Exception e) {
        System.out.println(e.getMessage());
        return false;
    }



    }

}
