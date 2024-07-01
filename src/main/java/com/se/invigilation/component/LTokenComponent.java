package com.se.invigilation.component;

import com.se.invigilation.exception.Code;
import com.se.invigilation.exception.XException;
import com.se.invigilation.vo.ResultVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@Slf4j
public class LTokenComponent {
    private final String KEY = "Ki98Yuhf2oEr9cDp";
    private final SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), "AES");
    private final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();

    public LTokenComponent() throws NoSuchPaddingException, NoSuchAlgorithmException {
    }

    @SneakyThrows
    public String encode(String account) {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return base64Encoder.encodeToString(cipher.doFinal(account.getBytes()));
    }

    public Mono<String> decode(String ltoken) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return Mono.just(new String(cipher.doFinal(base64Decoder.decode(ltoken))));
        } catch (Exception e) {
            throw XException.builder().code(Code.LOGIN_TOKEN_ERROR).build();
        }
    }
}
