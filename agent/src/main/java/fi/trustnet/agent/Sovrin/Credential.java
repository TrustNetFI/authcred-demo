package fi.trustnet.agent.Sovrin;

import com.github.jsonldjava.utils.JsonUtils;
import fi.trustnet.verifiablecredentials.VerifiableCredential;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.crypto.EC25519Provider;
import info.weboftrust.ldsignatures.signer.Ed25519Signature2018LdSigner;
import info.weboftrust.ldsignatures.validator.Ed25519Signature2018LdValidator;
import org.abstractj.kalium.NaCl;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.UUID;

import static fi.trustnet.agent.configuration.IndyConfig.ISSUERSEED;


public class Credential {

    //Create credential, everything hardcoded :) Based on TrustNet Verifiable Credential issuer example
    public static String createCredential(String issuerDid, String subjectDid) throws Exception {

        byte[] issuerPrivateKey = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_SECRETKEYBYTES];
        byte[] issuerPublicKey = new byte[NaCl.Sodium.CRYPTO_SIGN_ED25519_PUBLICKEYBYTES];

        EC25519Provider.get().generateEC25519KeyPairFromSeed(issuerPublicKey, issuerPrivateKey, ISSUERSEED.getBytes(StandardCharsets.UTF_8));

        VerifiableCredential verifiableCredential = new VerifiableCredential();
        verifiableCredential.getContext().add("https://example.com/credentials/v1");
        verifiableCredential.getType().add("ResourceAccessCredential");
        verifiableCredential.getType().add("CustomeIdentityCredential");
        verifiableCredential.setIssuer(URI.create(issuerDid));


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));


        verifiableCredential.setIssued(dtf.format(now));
        verifiableCredential.setSubject(subjectDid);

        LinkedHashMap<String, Object> jsonLdClaimsObject = verifiableCredential.getJsonLdClaimsObject();
        LinkedHashMap<String, Object> jsonLdAccessScopeObject = new LinkedHashMap<String, Object>();
        jsonLdAccessScopeObject.put("authorizedscopes", "electricity_metering_data");
        jsonLdAccessScopeObject.put("customerid", "192873465");
        jsonLdClaimsObject.put("claim", jsonLdAccessScopeObject);


        LinkedHashMap<String, Object> jsonLdCredentialStatusObject = verifiableCredential.getJsonLdObject();
        LinkedHashMap<String, Object> jsonLdRevocationObject = new LinkedHashMap<String, Object>();

        //Dummy revocation checking service. Just for testing revocation
        UUID uuid = UUID.randomUUID();
        jsonLdRevocationObject.put("revocation", "http://51.15.82.243:8085/revocation/" + uuid.toString());

        jsonLdCredentialStatusObject.put("credentialStatus", jsonLdRevocationObject);
        URI creator = URI.create(issuerDid + "#key1");

        dtf = DateTimeFormatter.ISO_INSTANT;
        String created = dtf.format(now.toInstant(ZoneOffset.UTC));
        String domain = null;

        String nonce = uuid.toString();

        // sign

        Ed25519Signature2018LdSigner signer = new Ed25519Signature2018LdSigner(creator, created, domain, nonce, issuerPrivateKey);
        LdSignature ldSignature = signer.sign(verifiableCredential.getJsonLdObject());
        return JsonUtils.toPrettyString(verifiableCredential.getJsonLdObject());
    }

    public static String getIssuer(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            return verifiableCredential.getIssuer().toString();
        } catch (Exception e) {
            return "";
        }

    }

    public static String getSubject(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            return verifiableCredential.getSubject();
        } catch (Exception e) {
            return "";
        }

    }

    public static String getRevocationUrl(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            //UGLY
            return ((LinkedHashMap) verifiableCredential.getJsonLdObject().get("credentialStatus")).get("revocation").toString();
        } catch (Exception e) {
            return "";
        }

    }

    public static String getRevocationUrl(VerifiableCredential credential) {
        try {
            return ((LinkedHashMap) credential.getJsonLdObject().get("credentialStatus")).get("revocation").toString();
        } catch (Exception e) {
            return "";
        }

    }

    public static String getScope(String credential) {
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);
            LinkedHashMap claim = (LinkedHashMap) verifiableCredential.getJsonLdObject().get("claim");
            return ((LinkedHashMap) claim.get("claim")).get("authorizedscopes").toString();
        } catch (Exception e) {
            return "";
        }

    }

    public static boolean verifyCredential(String credential) {
        // parse verifiable credential
        try {
            LinkedHashMap<String, Object> jsonLdObject = (LinkedHashMap<String, Object>) JsonUtils.fromString(credential);
            VerifiableCredential verifiableCredential = VerifiableCredential.fromJsonLdObject(jsonLdObject);

            // discover issuer public key

            byte[] issuerPublicKey;
            URI issuer = verifiableCredential.getIssuer();
            if (!LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));
            Pool pool = Pool.openPoolLedger("badgernet", "{}").get();
            Wallet wallet = Wallet.openWallet("rswallet", null, null).get();
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

    }
}
