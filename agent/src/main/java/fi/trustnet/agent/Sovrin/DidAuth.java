package fi.trustnet.agent.Sovrin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import static fi.trustnet.agent.configuration.IndyConfig.USER_WALLET_NAME;


//Simple dummy PoP token

public class DidAuth {



    public static boolean verifyToken(String token){
        //verify token
        System.out.println(token);
        String tkn = new String(Base64.getDecoder().decode(token));
        String[] splitToken = tkn.split("\\s+");
        try {
            if (Sovrin.verifySignature(USER_WALLET_NAME, splitToken[0], splitToken[1], Base64.getDecoder().decode(splitToken[2]))) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static String createToken(String user, String did){
        String token = "";
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
        String created = dtf.format(now.toInstant(ZoneOffset.UTC));
        try {
            token = Sovrin.createToken(user, did, created);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        String encodedToken = new String(Base64.getEncoder().encode((did + " " + created + " " + token).getBytes()));
        return encodedToken;
    }
}
