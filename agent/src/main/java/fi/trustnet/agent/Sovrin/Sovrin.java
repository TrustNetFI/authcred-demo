package fi.trustnet.agent.Sovrin;

import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.util.Base64;

import static fi.trustnet.agent.configuration.IndyConfig.NETWORK_NAME;

public class Sovrin {


    public static String createToken(String walletName, String did, String payload) throws Exception{
        if (! LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));
        Pool pool = Pool.openPoolLedger(NETWORK_NAME, "{}").get();
        Wallet wallet = Wallet.openWallet(walletName, null, null).get();
        String key = Did.keyForDid(pool, wallet, did).get();
        byte[] signed = Crypto.cryptoSign(wallet, key, payload.getBytes()).get();
        byte[] encoded = Base64.getEncoder().encode(signed);
        wallet.closeWallet().get();
        pool.closePoolLedger().get();
        return new String(encoded);

    }

    public static boolean verifySignature(String walletName, String did, String message, byte[] signature) throws Exception {
        if (! LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));
        Pool pool = Pool.openPoolLedger(NETWORK_NAME, "{}").get();
        Wallet wallet = Wallet.openWallet(walletName, null, null).get();
        String key = Did.keyForDid(pool, wallet, did).get();
        Boolean result = Crypto.cryptoVerify(key,message.getBytes(), signature).get();
        wallet.closeWallet().get();
        pool.closePoolLedger().get();
        return result;
    }
}
