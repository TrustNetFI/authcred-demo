package fi.trustnet.resourceserver.Sovrin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.trustnet.resourceserver.token.Token;

import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_DID;
import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_WALLETNAME;


public class AccessToken {


    public static String createAccessToken(String userDid, String serviceDid, String revocationUrl, String scope) {
        Token token = new Token();
        token.setExp("-1");
        token.setUserdid(userDid);
        token.setRevocationurl(revocationUrl);
        token.setScope(scope);
        token.setIssuedto(serviceDid);

        String tokenString = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            tokenString = Sovrin.anonCrypt(RS_WALLETNAME, RS_DID, objectMapper.writeValueAsString(token));
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return tokenString;
    }




    public static Token decrytAccessToken(String encryptedToken) {
        try {
            String decryptedToken = Sovrin.anonDecrypt(RS_WALLETNAME, RS_DID, encryptedToken);
            ObjectMapper objectMapper = new ObjectMapper();
            Token token = objectMapper.readValue(decryptedToken, Token.class);
            return token;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
