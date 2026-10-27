package com.clinica.seguro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Token assinado (HMAC-SHA256) com expiracao.
 * Substitui o "token" previsivel da versao vulneravel. Qualquer alteracao ao
 * conteudo invalida a assinatura, por isso o cliente nao o pode forjar.
 */
@Service
public class TokenService {

    private final byte[] segredo;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final long VALIDADE_SEG = 3600; // 1 hora

    public TokenService(@Value("${app.jwt.segredo}") String segredo) {
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
    }

    public String gerar(Long id, String perfil) {
        try {
            long exp = System.currentTimeMillis() / 1000 + VALIDADE_SEG;
            String payload = mapper.writeValueAsString(Map.of("id", id, "perfil", perfil, "exp", exp));
            String b64 = base64(payload.getBytes(StandardCharsets.UTF_8));
            return b64 + "." + base64(hmac(b64));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Devolve os dados do token se valido e nao expirado; senao null. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> validar(String token) {
        try {
            if (token == null) return null;
            String[] partes = token.split("\\.");
            if (partes.length != 2) return null;
            if (!base64(hmac(partes[0])).equals(partes[1])) return null; // assinatura invalida
            String payload = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
            Map<String, Object> dados = mapper.readValue(payload, Map.class);
            long exp = ((Number) dados.get("exp")).longValue();
            if (System.currentTimeMillis() / 1000 > exp) return null; // expirado
            return dados;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] hmac(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
