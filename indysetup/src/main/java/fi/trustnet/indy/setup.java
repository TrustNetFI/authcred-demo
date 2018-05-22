package fi.trustnet.indy;
import java.io.File;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters;
import org.hyperledger.indy.sdk.did.DidJSONParameters.CreateAndStoreMyDidJSONParameter;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;

public class setup {

    public static final String STEWARDSEED = "000000000000000000000000Steward1";
    public static final String STEWARDWALLET = "stewardwallet";

    public static final String USER_SEED = "000000000000000000000000000Alice";
    public static final String RS_SEED = "000000000000000000ResourceServer";
    public static final String CLIENT_SEED = "00000000000000000000000000Client";
    public static final String TRUST_ANCHOR_SEED = "100000000000000000000TrustAnchor";
    public static final String TRUST_ANCHOR_DID = "83jeafrrrzCBnAEF6NinrP";

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
    public static final String STEWARD_WALLET_NAME = "stewardwallet";
    public static final String TRUST_ANCHOR_WALLET_NAME = "trustanchor";

    public static final String DEFAULT_STEWARD_DID = "Th7MpTaRZVRYnPiabds81Y";

    public static void main(String[] args) throws Exception {
        if (! LibIndy.isInitialized()) LibIndy.init(new File("./lib/libindy.so"));

        Pool pool = Pool.openPoolLedger(NETWORK_NAME, "{}").get();
        if (!walletExists(STEWARD_WALLET_NAME)) {
            Wallet.createWallet(NETWORK_NAME, STEWARD_WALLET_NAME, "default", null, null).get();
            Wallet walletSteward = Wallet.openWallet(STEWARDWALLET, null, null).get();

            String did_json = "{\"seed\": \"" + STEWARDSEED + "\"}";
            DidResults.CreateAndStoreMyDidResult stewardResult = Did.createAndStoreMyDid(walletSteward, did_json).get();
            System.out.println("Steward did: " +stewardResult.getDid());
            walletSteward.closeWallet().get();
        }

        if (!walletExists(TRUST_ANCHOR_WALLET_NAME)) {
            Wallet.createWallet(NETWORK_NAME, TRUST_ANCHOR_WALLET_NAME, "default", null, null).get();
            Wallet trustAnchorWallet = Wallet.openWallet(TRUST_ANCHOR_WALLET_NAME, null, null).get();
            DidJSONParameters.CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, TRUST_ANCHOR_SEED, null, null);
            DidResults.CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(trustAnchorWallet, createAndStoreMyDidJSONParameter.toJson()).get();

            String taDid = createAndStoreMyDidResult.getDid();
            String taVerkey = createAndStoreMyDidResult.getVerkey();

            System.out.println("Trust Anchor DID is: " + taDid);
            System.out.println("Trust Anchor Verkey is " + taVerkey);
            Wallet walletSteward = Wallet.openWallet(STEWARD_WALLET_NAME, null, null).get();
            String taNymRequest = Ledger.buildNymRequest(DEFAULT_STEWARD_DID, taDid, taVerkey, null, "TRUST_ANCHOR").get();
            String result = Ledger.signAndSubmitRequest(pool, walletSteward, DEFAULT_STEWARD_DID, taNymRequest).get();
            walletSteward.closeWallet().get();
            trustAnchorWallet.closeWallet().get();
        }

        if (!walletExists(USER_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, USER_WALLET_NAME, "default", null, null).get();

        if (!walletExists(RS_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, RS_WALLET_NAME, "default", null, null).get();

        if (!walletExists(CLIENT_WALLET_NAME))
            Wallet.createWallet(NETWORK_NAME, CLIENT_WALLET_NAME, "default", null, null).get();


        Wallet walletTrustAnchor = Wallet.openWallet(TRUST_ANCHOR_WALLET_NAME, null, null).get();
        Wallet walletUser = Wallet.openWallet(USER_WALLET_NAME, null, null).get();
        Wallet walletRS = Wallet.openWallet(RS_WALLET_NAME, null, null).get();
        Wallet walletClient = Wallet.openWallet(CLIENT_WALLET_NAME, null, null).get();


        // create USER DID

        CreateAndStoreMyDidJSONParameter createAndStoreMyDidJSONParameter = new CreateAndStoreMyDidJSONParameter(null, USER_SEED, null, null);
        CreateAndStoreMyDidResult createAndStoreMyDidResult = Did.createAndStoreMyDid(walletUser, createAndStoreMyDidJSONParameter.toJson()).get();

        String userDid = createAndStoreMyDidResult.getDid();

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

        String rsNymRequest = Ledger.buildNymRequest(TRUST_ANCHOR_DID, rsDid, rsVerkey, /*"{\"alias\":\"b\"}"*/ null, null).get();
        String result = Ledger.signAndSubmitRequest(pool, walletTrustAnchor, TRUST_ANCHOR_DID, rsNymRequest).get();

        String clientNymRequest = Ledger.buildNymRequest(TRUST_ANCHOR_DID, clientDid, clientVerkey, /*"{\"alias\":\"b\"}"*/ null, null).get();
        result = Ledger.signAndSubmitRequest(pool, walletTrustAnchor, TRUST_ANCHOR_DID, clientNymRequest).get();

        walletTrustAnchor.closeWallet().get();
        walletUser.closeWallet().get();
        walletRS.closeWallet().get();
        walletClient.closeWallet().get();

        pool.closePoolLedger().get();

    }

    //works in Linux
    private static boolean walletExists(String walletName) {
        return (new File(System.getProperty("user.home") + "/" + WALLET_PATH + walletName + "/sqlite.db").isFile());
    }

}
