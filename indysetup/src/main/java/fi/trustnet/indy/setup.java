package fi.trustnet.indy;
import java.io.File;
import java.util.Base64;

import org.hyperledger.indy.sdk.IndyConstants;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pairwise.Pairwise;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters.OpenPoolLedgerJSONParameter;
import org.hyperledger.indy.sdk.wallet.Wallet;

public class setup {

    public static final String TRUSTEE_DID = "V4SGRU86Z58d6TV7PBUe6f";
    public static final String TRUSTEE_SEED = "000000000000000000000000Trustee1";

    public static final String USER_SEED = "000000000000000000000000000Alice";
    public static final String RS_SEED = "000000000000000000ResourceServer";
    public static final String CLIENT_SEED = "00000000000000000000000000Client";

    public static final String WALLET_PATH = ".indy_client/wallet/";
    /*
    Genesis file needs to be in
    ~/.indy_client/pool/{NETWORK_NAME}/{NETWORK_NAME}.txn
     */
    public static final String NETWORK_NAME = "default_pool";

    public static final String TRUSTEE_WALLET_NAME = "trusteewallet";
    public static final String USER_WALLET_NAME = "alice";
    public static final String RS_WALLET_NAME = "rswallet";
    public static final String CLIENT_WALLET_NAME = "clientwallet";


    public static void main(String[] args) throws Exception {
        if (! LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));

        Pool pool = Pool.openPoolLedger(NETWORK_NAME, "{}").get();

        if (!walletExists(TRUSTEE_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, TRUSTEE_WALLET_NAME, "default", null, null).get();

        if (!walletExists(USER_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, USER_WALLET_NAME, "default", null, null).get();

        if (!walletExists(RS_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, RS_WALLET_NAME, "default", null, null).get();

        if (!walletExists(CLIENT_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, CLIENT_WALLET_NAME, "default", null, null).get();

        Wallet walletTrustee = Wallet.openWallet(TRUSTEE_WALLET_NAME, null, null).get();
        Wallet walletUser = Wallet.openWallet(USER_WALLET_NAME, null, null).get();
        Wallet walletRS = Wallet.openWallet(RS_WALLET_NAME, null, null).get();
        Wallet walletClient = Wallet.openWallet(CLIENT_WALLET_NAME, null, null).get();


        // create TRUSTEE DID

        CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameterTrustee = new CreateAndStoreMyDidJSONParameter(null, TRUSTEE_SEED, null, null);
        CreateAndStoreMyDidResult r = Did.createAndStoreMyDid(walletTrustee, createAndStoreMyDidJSONParameterTrustee.toJson()).get();

        // create USER DID

        CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, USER_SEED, null, null);
        CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletUser, createAndStoreMyDidJSONParameter.toJson()).get();

        String userDid = createAndStoreMyDidResult.getDid();
        String userVerkey = createAndStoreMyDidResult.getVerkey();

        System.out.println("User DID: " + userDid);

        // create RS DID

        CreateAndStoreMyDidJSONParameter createAndStoreRsDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, RS_SEED, null, null);
        CreateAndStoreMyDidResult createAndStoreRsDidResult = Did.createAndStoreMyDid(walletRS, createAndStoreRsDidJSONParameter.toJson()).get();

        String rsDid = createAndStoreRsDidResult.getDid();
        String rsVerkey = createAndStoreRsDidResult.getVerkey();

        System.out.println("RS DID: " + rsDid);


        // create Client DID

        CreateAndStoreMyDidJSONParameter createAndStoreClientDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, CLIENT_SEED, null, null);
        CreateAndStoreMyDidResult createAndStoreClientDidResult = Did.createAndStoreMyDid(walletClient, createAndStoreClientDidJSONParameter.toJson()).get();

        String clientDid = createAndStoreClientDidResult.getDid();
        String clientVerkey = createAndStoreClientDidResult.getVerkey();


        System.out.println("Client DID: " + clientDid);

        String userNymRequest = Ledger.buildNymRequest(TRUSTEE_DID, userDid, userVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
        Ledger.signAndSubmitRequest(pool, walletTrustee, TRUSTEE_DID, userNymRequest).get();

        String rsNymRequest = Ledger.buildNymRequest(TRUSTEE_DID, rsDid, rsVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
        Ledger.signAndSubmitRequest(pool, walletTrustee, TRUSTEE_DID, rsNymRequest).get();

        String clientNymRequest = Ledger.buildNymRequest(TRUSTEE_DID, clientDid, clientVerkey, /*"{\"alias\":\"b\"}"*/ null, IndyConstants.ROLE_TRUSTEE).get();
        Ledger.signAndSubmitRequest(pool, walletTrustee, TRUSTEE_DID, clientNymRequest).get();

        walletTrustee.closeWallet().get();
        walletUser.closeWallet().get();
        walletRS.closeWallet().get();
        walletClient.closeWallet().get();

        pool.closePoolLedger().get();

    }

    private static boolean walletExists(String walletName) {
        return (new File(System.getProperty("user.home") + "/" + WALLET_PATH + walletName + "/sqlite.db").isFile());
    }

}
